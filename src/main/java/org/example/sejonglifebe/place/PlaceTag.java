package org.example.sejonglifebe.place;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.tag.Tag;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"place_id", "tag_id"}))
public class PlaceTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private PlaceTag(Place place, Tag tag) {
        this.place = place;
        this.tag = tag;
    }

    public static PlaceTag createPlaceTag(Place place, Tag tag) {
        PlaceTag placeTag = new PlaceTag(place, tag);
        place.getPlaceTags().add(placeTag);
        tag.getPlaceTags().add(placeTag);

        return placeTag;
    }
}
