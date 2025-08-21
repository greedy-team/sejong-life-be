package org.example.sejonglifebe.review;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private Double rating;

    @Column(nullable = false)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    Place place;

    @OneToMany(mappedBy = "review", cascade = CascadeType.PERSIST)
    List<PlaceImage> placeImages = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReviewTag> reviewTags = new ArrayList<>();

    @Builder
    private Review(Place place, Double rating, String content) {
        this.place = place;
        this.rating = rating;
        this.content = content;
    }

    public static Review createReview(Place place, Double rating, String content, List<Tag> tags, List<String> imageUrls) {

        Review review = Review.builder()
                .place(place)
                .rating(rating)
                .content(content)
                .build();

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                review.addTag(tag);
            }
        }

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                review.addImage(place, imageUrl);
            }
        }

        return review;
    }

    public void addImage(Place place, String imageUrl) {
        PlaceImage placeImage = new PlaceImage(place, this, imageUrl, false);
        placeImages.add(placeImage);
    }

    public void addTag(Tag tag) {
        ReviewTag reviewTag = ReviewTag.createReviewTag(this, tag);
        reviewTags.add(reviewTag);
    }
}
