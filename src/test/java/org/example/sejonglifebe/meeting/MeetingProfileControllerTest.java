package org.example.sejonglifebe.meeting;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.service.MeetingOpenCountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MeetingProfileControllerTest {

    private static final Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeetingProfileRepository meetingProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MeetingOpenCountService meetingOpenCountService;

    private MeetingProfile profile1;
    private MeetingProfile profile2;

    @BeforeEach
    void setUp() {
        meetingOpenCountService.clearCooldown("kakao-1");
        meetingOpenCountService.clearCooldown("kakao-2");
        meetingOpenCountService.clearCooldown("kakao-3");
        meetingProfileRepository.deleteAll();

        profile1 = meetingProfileRepository.save(
                MeetingProfile.builder()
                        .kakaoId("kakao-1")
                        .gender(Gender.MALE)
                        .faceType(FaceType.DOG)
                        .birthYear(2000)
                        .hobby("축구")
                        .dateStyle("활동적인 데이트")
                        .contact("insta_1")
                        .build()
        );

        profile2 = meetingProfileRepository.save(
                MeetingProfile.builder()
                        .kakaoId("kakao-2")
                        .gender(Gender.FEMALE)
                        .faceType(FaceType.CAT)
                        .birthYear(2001)
                        .hobby("영화")
                        .dateStyle("조용한 데이트")
                        .contact("insta_2")
                        .build()
        );
    }

    @Test
    @DisplayName("전체 미팅 프로필이 조회된다")
    void getAllMeetingProfiles_success() throws Exception {
        String token = jwtTokenProvider.createMeetingToken("kakao-1");

        mockMvc.perform(get("/api/meeting/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].kakaoId").value("kakao-2"))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$[0].faceType").value("CAT"))
                .andExpect(jsonPath("$[0].contact").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("열람권이 있을 때 연락처가 반환되고 열람권이 차감된다")
    void openContact_success() throws Exception {
        String token = jwtTokenProvider.createMeetingToken("kakao-1");
        Long targetProfileId = profile2.getId();

        mockMvc.perform(post("/api/meeting/profiles/{profileId}/open", targetProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contact").value("insta_2"))
                .andDo(print());
    }

    @Test
    @DisplayName("열람권이 없을 때 403을 반환한다")
    void openContact_fail_insufficientOpenCount() throws Exception {
        meetingProfileRepository.save(
                MeetingProfile.builder()
                        .kakaoId("kakao-3")
                        .gender(Gender.MALE)
                        .faceType(FaceType.BEAR)
                        .birthYear(1999)
                        .hobby("독서")
                        .dateStyle("집에서 데이트")
                        .contact("insta_3")
                        .build()
        );
        meetingOpenCountService.startCooldown("kakao-3");

        String token = jwtTokenProvider.createMeetingToken("kakao-3");

        mockMvc.perform(post("/api/meeting/profiles/{profileId}/open", profile2.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("자신의 프로필 열람 시 400을 반환한다")
    void openContact_fail_selfProfile() throws Exception {
        String token = jwtTokenProvider.createMeetingToken("kakao-1");

        mockMvc.perform(post("/api/meeting/profiles/{profileId}/open", profile1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 프로필 연락처 열람 시 예외를 반환한다")
    void openContact_fail_notFound() throws Exception {
        String token = jwtTokenProvider.createMeetingToken("kakao-1");

        mockMvc.perform(post("/api/meeting/profiles/{profileId}/open", NON_EXISTENT_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("미팅 프로필이 정상적으로 수정된다")
    void updateMeetingProfile_success() throws Exception {
        Long profileId = profile1.getId();

        MeetingProfileUpdateRequest request = new MeetingProfileUpdateRequest(
                Gender.FEMALE,
                FaceType.FOX,
                1999,
                "산책",
                "맛집 데이트",
                "updated_contact"
        );

        mockMvc.perform(put("/api/meeting/profiles/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        MeetingProfile updated = meetingProfileRepository.findById(profileId).orElseThrow();
        assertThat(updated.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(updated.getFaceType()).isEqualTo(FaceType.FOX);
        assertThat(updated.getBirthYear()).isEqualTo(1999);
        assertThat(updated.getHobby()).isEqualTo("산책");
        assertThat(updated.getDateStyle()).isEqualTo("맛집 데이트");
        assertThat(updated.getContact()).isEqualTo("updated_contact");

    }

    @Test
    @DisplayName("존재하지 않는 미팅 프로필 수정 시 예외를 반환한다")
    void updateMeetingProfile_fail_notFound() throws Exception {
        MeetingProfileUpdateRequest request = new MeetingProfileUpdateRequest(
                Gender.FEMALE,
                FaceType.FOX,
                1999,
                "산책",
                "맛집 데이트",
                "updated_contact"
        );

        mockMvc.perform(put("/api/meeting/profiles/{id}", NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("미팅 프로필이 정상적으로 삭제된다")
    void deleteMeetingProfile_success() throws Exception {
        Long profileId = profile1.getId();
        long beforeCount = meetingProfileRepository.count();

        mockMvc.perform(delete("/api/meeting/profiles/{id}", profileId))
                .andExpect(status().isNoContent())
                .andDo(print());

        assertThat(meetingProfileRepository.count()).isEqualTo(beforeCount - 1);
        assertThat(meetingProfileRepository.findById(profileId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 미팅 프로필 삭제 시 예외를 반환한다")
    void deleteMeetingProfile_fail_notFound() throws Exception {
        mockMvc.perform(delete("/api/meeting/profiles/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
