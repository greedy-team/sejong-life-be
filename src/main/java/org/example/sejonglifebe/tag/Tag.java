package org.example.sejonglifebe.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.place.entity.PlaceTag;

@Getter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "tag_name", unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "tag")
    private List<PlaceTag> placeTags = new ArrayList<>();

    public Tag(String name) {
        this.name = name;
    }
}
