package org.example.sejonglifebe.place.favorite;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.user.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "favorite_place",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorite_place_user_place",
                        columnNames = {"user_id", "place_id"}
                )
        }
)
public class FavoritePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private FavoritePlace(User user, Place place) {
        this.user = user;
        this.place = place;
    }

    public static FavoritePlace of(User user, Place place) {
        return new FavoritePlace(user, place);
    }
}
