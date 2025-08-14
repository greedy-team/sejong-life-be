package org.example.sejonglifebe.place.entity;

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
import org.example.sejonglifebe.place.util.MapLinkConverter;
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

    @Column(unique = true)
    private String mainImageUrl;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceTag> placeTags = new ArrayList<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceCategory> placeCategories = new ArrayList<>();

    @Builder
    public Place(String name, String address, MapLinks mapLinks, String mainImageUrl) {
        this.name = name;
        this.address = address;
        this.mapLinks = mapLinks;
        this.mainImageUrl = mainImageUrl;
    }

    public void addTag(Tag tag) {
        PlaceTag.createPlaceTag(this, tag);
    }

    public void addCategory(Category category) {
        PlaceCategory.createPlaceCategory(this, category);
    }

    public List<Category> getCategories() {
        return placeCategories
                .stream().map(PlaceCategory::getCategory)
                .toList();
    }

    public List<Tag> getTags() {
        return placeTags
                .stream()
                .map(PlaceTag::getTag).toList();
    }

}
