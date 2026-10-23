package org.example.sejonglifebe.common.logging;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * AccessLogFilter는 chain.doFilter()가 끝난 뒤 response.getStatus()를 읽어 access log를 남김.
 * 컨트롤러에서 예외가 던져진 경우에도(GlobalExceptionHandler가 처리한 뒤) 이 값이
 * 실제 최종 상태 코드로 정확히 확정되어 있는지 확인함.
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccessLogFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("정상 응답이면 200이 확정된 상태로 필터 체인을 빠져나온다")
    void normalRequest_statusIsFinalized() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SejongLifeException으로 처리된 요청도 404가 정확히 확정된다")
    void sejongLifeException_statusIsFinalized() throws Exception {
        mockMvc.perform(get("/api/places/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException으로 처리된 요청도 400이 정확히 확정된다")
    void validationException_statusIsFinalized() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .param("partnershipOnly", "false")
                        .param("sortType", "DISTANCE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
