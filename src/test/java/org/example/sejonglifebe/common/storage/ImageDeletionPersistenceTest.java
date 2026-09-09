package org.example.sejonglifebe.common.storage;

import jakarta.persistence.EntityManager;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.TokenIssuer;
import org.example.sejonglifebe.common.config.QueryDslConfig;
import org.example.sejonglifebe.place.PlaceService;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.favorite.FavoritePlace;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewService;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({PlaceService.class, ReviewService.class, UserService.class, QueryDslConfig.class, TransactionalImageStorage.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ImageDeletionPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean(name = "s3Service")
    private S3Service imageStorage;

    @MockitoBean
    private PlaceViewService placeViewService;

    @MockitoBean
    private TokenIssuer tokenIssuer;

    private User user;
    private Place place;
    private Review review;
    private final AuthUser authUser = new AuthUser("21011111", Role.ADMIN);

    @BeforeEach
    void setUp() {
        user = User.builder().studentId("21011111").nickname("테스트 사용자").createdAt(LocalDateTime.now()).build();
        entityManager.persist(user);
        place = Place.createPlace("장소", "주소", null, null, new MapLinks("", "", ""), false, null);
        place.addImage("admin.webp", true);
        entityManager.persist(place);
        review = Review.createReview(place, user, 5, "사진 리뷰");
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());
        review.addImage("review.webp");
        entityManager.persist(review);
        entityManager.flush();
    }

    @Test
    @DisplayName("장소 삭제가 롤백되면 파일과 DB 데이터를 유지한다")
    void failedPlaceDeletion_preservesBothFilesAndDatabaseRows() {
        // given
        entityManager.persist(FavoritePlace.of(user, place));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when & then
        assertThatThrownBy(() -> placeService.deletePlace(place.getId(), authUser))
                .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);
        verify(imageStorage, never()).deleteImages(anyList());
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(entityManager.find(Place.class, place.getId()).getPlaceImages()).hasSize(2);
            assertThat(entityManager.find(Review.class, review.getId())).isNotNull();
        });
    }

    @Test
    @DisplayName("장소 삭제가 커밋된 이후에만 파일을 삭제한다")
    void successfulPlaceDeletion_deletesFilesOnlyAfterCommit() {
        // given
        entityManager.clear();

        // when
        placeService.deletePlace(place.getId(), authUser);
        entityManager.flush();

        // then
        verify(imageStorage, never()).deleteImages(anyList());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        verify(imageStorage).deleteImages(List.of("admin.webp", "review.webp"));
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertThat(entityManager.find(Place.class, place.getId())).isNull());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("리뷰 삭제가 롤백되면 파일을 유지한다")
    void reviewDeletion_preservesFilesOnRollback(boolean myPage) {
        // given
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            deleteReview(myPage);
            entityManager.flush();
            verify(imageStorage, never()).deleteImages(anyList());
            status.setRollbackOnly();
        });

        // then
        verify(imageStorage, never()).deleteImages(anyList());
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(entityManager.find(Review.class, review.getId())).isNotNull();
            assertThat(entityManager.find(Place.class, place.getId()).getPlaceImages()).hasSize(2);
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("리뷰 삭제가 커밋되면 해당 리뷰 파일만 삭제한다")
    void reviewDeletion_deletesOnlyReviewFileAfterCommit(boolean myPage) {
        // given
        entityManager.clear();

        // when
        deleteReview(myPage);
        entityManager.flush();

        // then
        verify(imageStorage, never()).deleteImages(anyList());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        verify(imageStorage).deleteImages(List.of("review.webp"));
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(entityManager.find(Review.class, review.getId())).isNull();
            assertThat(entityManager.find(Place.class, place.getId()).getThumbnailImage()).isEqualTo("admin.webp");
        });
    }

    @Test
    @DisplayName("회원 삭제가 롤백되면 리뷰 파일을 유지한다")
    void failedUserDeletion_preservesReviewFile() {
        // given
        entityManager.persist(FavoritePlace.of(user, place));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(authUser))
                .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);
        verify(imageStorage, never()).deleteImages(anyList());
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(entityManager.find(User.class, user.getId())).isNotNull();
            assertThat(entityManager.find(Review.class, review.getId())).isNotNull();
        });
    }

    private void deleteReview(boolean myPage) {
        if (myPage) {
            reviewService.deleteMyPageReview(review.getId(), authUser);
        } else {
            reviewService.deleteReview(review.getId(), place.getId(), authUser);
        }
    }
}
