package org.example.sejonglifebe.place;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.category.Category;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"place_id", "category_id"}))
public class PlaceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private PlaceCategory(Place place, Category category) {
        this.place = place;
        this.category = category;
    }

    public static PlaceCategory createPlaceCategory(Place place, Category category) {
        PlaceCategory placeCategory = new PlaceCategory(place, category);
        place.getPlaceCategories().add(placeCategory);
        category.getPlaceCategories().add(placeCategory);

        return placeCategory;
    }
}
