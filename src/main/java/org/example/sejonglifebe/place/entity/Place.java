package org.example.sejonglifebe.place.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.util.MapLinkConverter;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.tag.Tag;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(name = "place_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Convert(converter = MapLinkConverter.class)
    @Column(columnDefinition = "text")
    private MapLinks mapLinks;

    @Column(unique = true)
    private String mainImageUrl;

    @Column(nullable = false)
    private Long viewCount;

    @Setter
    @Column(nullable = false)
    private Long weeklyViewCount;

    @Column(nullable = false)
    private boolean isPartnership;

    private String partnershipContent;

    @OrderBy("id ASC")
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImage> placeImages = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceTag> placeTags = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceCategory> placeCategories = new ArrayList<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Builder
    public Place(String name, String address, MapLinks mapLinks, String mainImageUrl) {
        this.name = name;
        this.address = address;
        this.mapLinks = mapLinks;
        this.viewCount = 0L;
        this.weeklyViewCount = 0L;
    }

    public static Place createPlace(
            String name,
            String address,
            Double latitude,
            Double longitude,
            MapLinks mapLinks,
            boolean isPartnership,
            String partnershipContent
    ) {
        Place place = Place.builder()
                .name(name)
                .address(address)
                .mapLinks(mapLinks)
                .mainImageUrl(null)
                .build();

        place.latitude = latitude;
        place.longitude = longitude;
        place.isPartnership = isPartnership;
        place.partnershipContent = partnershipContent;
        place.viewCount = 0L;
        place.weeklyViewCount = 0L;

        return place;
    }

    public void addImage(String imageUrl, Boolean isThumbnail) {
        if (isThumbnail) {
            this.placeImages.removeIf(image -> Boolean.TRUE.equals(image.getIsThumbnail()));
        }
        PlaceImage placeImage = new PlaceImage(this, imageUrl, isThumbnail);
        this.placeImages.add(placeImage);
    }

    public List<String> replaceThumbnail(String imageUrl) {
        List<String> previousThumbnailUrls = removeThumbnail();
        placeImages.add(new PlaceImage(this, imageUrl, true));
        return previousThumbnailUrls;
    }

    public List<String> removeThumbnail() {
        List<String> thumbnailUrls = placeImages.stream()
                .filter(image -> image.getReview() == null && Boolean.TRUE.equals(image.getIsThumbnail()))
                .map(PlaceImage::getUrl)
                .toList();
        placeImages.removeIf(image -> image.getReview() == null && Boolean.TRUE.equals(image.getIsThumbnail()));
        return thumbnailUrls;
    }

    public void removeImage(PlaceImage image) {
        placeImages.remove(image);
    }

    public void addTag(Tag tag) {
        boolean exists = placeTags.stream()
                .anyMatch(placeTag -> placeTag.getTag().equals(tag));
        if (!exists) {
            PlaceTag.createPlaceTag(this, tag);
        }
    }

    public void addCategory(Category category) {
        boolean exists = placeCategories.stream()
                .anyMatch(placeCategory -> placeCategory.getCategory().equals(category));
        if (!exists) {
            PlaceCategory.createPlaceCategory(this, category);
        }
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
    }

    public String getThumbnailImage() {
        if (placeImages == null || placeImages.isEmpty()) {
            return null; // 이미지가 아예 없는 경우
        }

        return placeImages.stream()
                .filter(image -> image.getIsThumbnail() != null && image.getIsThumbnail())
                .findFirst() // isThumbnail이 true인 첫 번째 이미지를 찾고
                .map(PlaceImage::getUrl) // 그 이미지의 URL을 반환
                .orElse(placeImages.get(0).getUrl()); // 만약 대표 이미지가 없다면, 그냥 첫 번째 이미지 반환
    }

    public void update(
            String name,
            String address,
            Double latitude,
            Double longitude,
            MapLinks mapLinks,
            boolean partnership,
            String partnershipContent,
            List<Category> categories,
            List<Tag> tags
    ) {
        updateDetails(name, address, latitude, longitude);
        updateMapLinks(mapLinks);
        updatePartnership(partnership, partnershipContent);
        replaceCategories(categories);
        replaceTags(tags);
    }

    public void updateMapLinks(MapLinks mapLinks) {
        this.mapLinks = mapLinks;
    }

    public void updateDetails(String name, String address, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updatePartnership(boolean partnership, String content) {
        this.isPartnership = partnership;
        this.partnershipContent = partnership ? content : null;
    }

    public void replaceTags(List<Tag> tags) {
        for (PlaceTag pt : new ArrayList<>(this.placeTags)) {
            if (!tags.contains(pt.getTag())) {
                pt.getTag().getPlaceTags().remove(pt);
                this.placeTags.remove(pt);
            }
        }
        tags.forEach(this::addTag);
    }

    public void replaceCategories(List<Category> categories) {
        for (PlaceCategory pc : new ArrayList<>(this.placeCategories)) {
            if (!categories.contains(pc.getCategory())) {
                pc.getCategory().getPlaceCategories().remove(pc);
                this.placeCategories.remove(pc);
            }
        }
        categories.forEach(this::addCategory);
    }
}
