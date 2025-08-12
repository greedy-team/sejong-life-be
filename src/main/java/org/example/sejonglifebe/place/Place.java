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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.tag.Tag;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
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
    @Column(columnDefinition = "json")
    private MapLinks map_links;

    @Column(unique = true, nullable = true)
    private String main_image_url;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceTag> placeTags = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    private List<PlaceCategory> placeCategories;

    public Place(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public void addTags(Tag...tags){
        for (Tag tag : tags) {
            PlaceTag placeTag = new PlaceTag();
            placeTag.setTag(tag);
            tag.getPlaceTags().add(placeTag);
            placeTag.setPlace(this);
            placeTags.add(placeTag);
        }
    }

}
