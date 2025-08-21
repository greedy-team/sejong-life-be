package org.example.sejonglifebe.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.review.Review;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "image_url", unique = true, nullable = false)
    private String url;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;

    public PlaceImage(Place place, String imageUrl, Boolean isThumbnail) {
        this.place = place;
        this.url = imageUrl;
        this.isThumbnail = isThumbnail;
    }

    public PlaceImage(Place place, Review review, String imageUrl, Boolean isThumbnail) {
        this.place = place;
        this.review = review;
        this.url = imageUrl;
        this.isThumbnail = isThumbnail;
    }
}
