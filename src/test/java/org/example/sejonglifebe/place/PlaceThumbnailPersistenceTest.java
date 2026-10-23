package org.example.sejonglifebe.place;

import jakarta.persistence.EntityManager;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.common.config.QueryDslConfig;
import org.example.sejonglifebe.common.storage.TransactionalImageStorage;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceUpdateRequest;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.entity.PlaceCategory;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.place.entity.PlaceTag;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.session_factory.statement_inspector="
        + "org.example.sejonglifebe.place.PlaceThumbnailPersistenceTest$ImageQueryInspector")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PlaceService.class, QueryDslConfig.class, TransactionalImageStorage.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlaceThumbnailPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlaceService placeService;

    @MockitoBean(name = "s3Service")
    private S3Service imageStorage;

    @MockitoBean
    private PlaceViewService placeViewService;

    private Long placeId;
    private Long reviewId;
    private Long thumbnailId;
    private Long categoryLinkId;
    private Long tagLinkId;
    private PlaceUpdateRequest request;

    @BeforeEach
    void setUp() {
        Category category = new Category("식당");
        Tag tag = new Tag("맛집");
        entityManager.persist(category);
        entityManager.persist(tag);
        Place place = Place.builder().name("장소").address("주소").build();
        place.addCategory(category);
        place.addTag(tag);
        place.addImage("admin.webp", true);
        entityManager.persist(place);
        Review review = Review.builder().place(place).rating(5).content("리뷰").build();
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());
        review.addImage("review.webp");
        entityManager.persist(review);
        entityManager.flush();
        placeId = place.getId();
        reviewId = review.getId();
        thumbnailId = place.getPlaceImages().get(0).getId();
        categoryLinkId = place.getPlaceCategories().get(0).getId();
        tagLinkId = place.getPlaceTags().get(0).getId();
        request = new PlaceUpdateRequest("수정 장소", "수정 주소", 37.55, 127.07,
                List.of(category.getId()), List.of(tag.getId()), new MapLinks("", "", ""), false, "");
        entityManager.clear();
    }

    @Test
    @DisplayName("커밋하면 관리자 썸네일을 교체하고 리뷰 사진은 유지한다")
    void commit_replacesAdminThumbnailAndPreservesReviewImage() {
        // given
        given(imageStorage.uploadImage(anyString(), any())).willReturn("new.webp");

        // when
        placeService.updatePlace(placeId, request,
                new MockMultipartFile("thumbnail", new byte[]{1}), false);
        entityManager.flush();
        entityManager.clear();

        Place saved = entityManager.find(Place.class, placeId);

        // then
        assertThat(saved.getPlaceCategories()).extracting(PlaceCategory::getId).containsExactly(categoryLinkId);
        assertThat(saved.getPlaceTags()).extracting(PlaceTag::getId).containsExactly(tagLinkId);
        assertThat(saved.getThumbnailImage()).isEqualTo("new.webp");
        assertThat(saved.getPlaceImages()).extracting(PlaceImage::getUrl).containsExactlyInAnyOrder("new.webp", "review.webp");
        assertThat(entityManager.find(PlaceImage.class, thumbnailId)).isNull();
        assertThat(entityManager.find(Review.class, reviewId).getPlaceImages()).hasSize(1);
        PlaceDetailResponse detail = PlaceDetailResponse.from(saved);
        assertThat(detail.thumbnail().url()).isEqualTo("new.webp");
        assertThat(detail.address()).isEqualTo("수정 주소");
        verify(imageStorage, never()).deleteImages(anyList());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        verify(imageStorage).deleteImages(List.of("admin.webp"));
    }

    @Test
    @DisplayName("관리자 썸네일만 삭제하고 리뷰 사진을 대표 이미지로 사용한다")
    void delete_removesOnlyAdminThumbnailAndFallsBackToReviewImage() {
        // given
        Review review = entityManager.find(Review.class, reviewId);
        review.addImage("second-review.webp");
        entityManager.flush();
        entityManager.clear();

        // when
        placeService.updatePlace(placeId, request, null, true);
        entityManager.flush();
        entityManager.clear();
        Place saved = entityManager.find(Place.class, placeId);

        // then
        assertThat(saved.getThumbnailImage()).isEqualTo("review.webp");
        assertThat(ImageQueryInspector.lastImageQuery).containsPattern("order by \\w+\\.image_id(?: asc)?$");
        assertThat(PlaceDetailResponse.from(saved).thumbnail()).isNull();
        assertThat(saved.getPlaceImages()).extracting(PlaceImage::getUrl)
                .containsExactly("review.webp", "second-review.webp");
        assertThat(entityManager.find(Review.class, reviewId).getPlaceImages()).hasSize(2);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        verify(imageStorage).deleteImages(List.of("admin.webp"));
    }

    @Test
    @DisplayName("롤백하면 신규 업로드만 정리하고 기존 파일은 유지한다")
    void rollback_doesNotDeleteOriginalFile() {
        // given
        given(imageStorage.uploadImage(anyString(), any())).willReturn("rollback.webp");

        // when
        placeService.updatePlace(placeId, request,
                new MockMultipartFile("thumbnail", new byte[]{1}), false);
        entityManager.flush();
        TestTransaction.flagForRollback();
        TestTransaction.end();

        // then
        verify(imageStorage).deleteImages(List.of("rollback.webp"));
        verify(imageStorage, never()).deleteImages(List.of("admin.webp"));
    }

    @Test
    @DisplayName("같은 수정 요청을 반복해도 기존 카테고리와 태그 연결을 유지한다")
    void update_keepsUnchangedRelationshipsOnRepeatedRequests() {
        // when
        placeService.updatePlace(placeId, request, null, false);
        entityManager.flush();
        entityManager.clear();
        placeService.updatePlace(placeId, request, null, false);
        entityManager.flush();
        entityManager.clear();

        Place saved = entityManager.find(Place.class, placeId);

        // then
        assertThat(saved.getPlaceCategories()).extracting(PlaceCategory::getId).containsExactly(categoryLinkId);
        assertThat(saved.getPlaceTags()).extracting(PlaceTag::getId).containsExactly(tagLinkId);
        verify(imageStorage, never()).deleteImages(anyList());
    }

    @Test
    @DisplayName("요청에서 변경된 카테고리와 태그 연결만 수정한다")
    void update_changesOnlyRequestedRelationships() {
        // given
        Category removedCategory = new Category("삭제 카테고리");
        Tag removedTag = new Tag("삭제 태그");
        Category addedCategory = new Category("추가 카테고리");
        Tag addedTag = new Tag("추가 태그");
        entityManager.persist(removedCategory);
        entityManager.persist(removedTag);
        entityManager.persist(addedCategory);
        entityManager.persist(addedTag);
        Place place = entityManager.find(Place.class, placeId);
        place.addCategory(removedCategory);
        place.addTag(removedTag);
        entityManager.flush();
        Long removedCategoryLinkId = place.getPlaceCategories().get(1).getId();
        Long removedTagLinkId = place.getPlaceTags().get(1).getId();
        entityManager.clear();

        PlaceUpdateRequest changed = new PlaceUpdateRequest(request.placeName(), request.address(),
                request.latitude(), request.longitude(),
                List.of(request.categoryIds().get(0), addedCategory.getId()),
                List.of(request.tagIds().get(0), addedTag.getId()), request.mapLinks(), false, "");

        // when
        placeService.updatePlace(placeId, changed, null, false);

        // then
        assertThat(entityManager.find(Category.class, removedCategory.getId()).getPlaceCategories()).isEmpty();
        assertThat(entityManager.find(Tag.class, removedTag.getId()).getPlaceTags()).isEmpty();
        entityManager.flush();
        entityManager.clear();

        Place saved = entityManager.find(Place.class, placeId);
        assertThat(saved.getPlaceCategories()).hasSize(2).extracting(PlaceCategory::getId).contains(categoryLinkId);
        assertThat(saved.getPlaceTags()).hasSize(2).extracting(PlaceTag::getId).contains(tagLinkId);
        assertThat(saved.getPlaceCategories()).extracting(pc -> pc.getCategory().getId())
                .containsExactlyInAnyOrder(request.categoryIds().get(0), addedCategory.getId());
        assertThat(saved.getPlaceTags()).extracting(pt -> pt.getTag().getId())
                .containsExactlyInAnyOrder(request.tagIds().get(0), addedTag.getId());
        assertThat(entityManager.find(PlaceCategory.class, removedCategoryLinkId)).isNull();
        assertThat(entityManager.find(PlaceTag.class, removedTagLinkId)).isNull();
        assertThat(entityManager.find(Category.class, addedCategory.getId()).getPlaceCategories())
                .extracting(pc -> pc.getPlace().getId()).containsExactly(placeId);
        assertThat(entityManager.find(Tag.class, addedTag.getId()).getPlaceTags())
                .extracting(pt -> pt.getPlace().getId()).containsExactly(placeId);
    }
    public static class ImageQueryInspector implements StatementInspector {
        private static String lastImageQuery;

        @Override
        public String inspect(String sql) {
            String normalized = sql.replaceAll("\\s+", " ").trim();
            if (normalized.startsWith("select ") && normalized.contains(" from place_image ")) {
                lastImageQuery = normalized;
            }
            return sql;
        }
    }
}
