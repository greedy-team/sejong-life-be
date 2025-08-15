package org.example.sejonglifebe.place;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.tag.Tag;

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

    @Column(unique = true, nullable = false)
    private String address;

    @Convert(converter = MapLinkConverter.class)
    @Column(columnDefinition = "text")
    private MapLinks mapLinks;

    @OneToMany(mappedBy = "place", cascade = CascadeType.PERSIST)
    private List<PlaceImage> placeImages = new ArrayList<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceTag> placeTags = new ArrayList<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceCategory> placeCategories = new ArrayList<>();

    @Builder
    public Place(String name, String address, MapLinks mapLinks) {
        this.name = name;
        this.address = address;
        this.mapLinks = mapLinks;
    }

    public void addImage(String imageUrl, Boolean isThumbnail) {
        if (isThumbnail) {
            this.placeImages.removeIf(PlaceImage::getIsThumbnail);
        }
        PlaceImage placeImage = new PlaceImage(this, imageUrl, isThumbnail);
        this.placeImages.add(placeImage);
    }

    public void addTag(Tag tag) {
        PlaceTag.createPlaceTag(this, tag);
    }

    public void addCategory(Category category) {
        PlaceCategory.createPlaceCategory(this, category);
    }

    public String getThumbnailImageUrl() {
        if (placeImages == null || placeImages.isEmpty()) {
            return null; // 이미지가 아예 없는 경우
        }

        return placeImages.stream()
                .filter(image -> image.getIsThumbnail() != null && image.getIsThumbnail())
                .findFirst() // isThumbnail이 true인 첫 번째 이미지를 찾고
                .map(PlaceImage::getUrl) // 그 이미지의 URL을 반환
                .orElse(placeImages.get(0).getUrl()); // 만약 대표 이미지가 없다면, 그냥 첫 번째 이미지 반환
    }
}
