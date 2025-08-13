package org.example.sejonglifebe;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.MapLinks;
import org.example.sejonglifebe.place.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataLoader implements CommandLineRunner {

    @PersistenceContext
    EntityManager em;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Tag tag1 = new Tag("가성비");
        Tag tag2 = new Tag("집밥");
        Tag tag3 = new Tag("콘센트");
        Tag tag4 = new Tag("학정대체");
        em.persist(tag1);
        em.persist(tag2);
        em.persist(tag3);
        em.persist(tag4);

        Category category1 = new Category("식당");
        Category category2 = new Category("카페");
        em.persist(category1);
        em.persist(category2);

        Place place1 = Place.builder()
                .name("또래끼리")
                .address("세종대 후문")
                .mapLinks(new MapLinks("네이버1","카카오1","구글1"))
                .build();

        Place place2 = Place.builder()
                .name("스타벅스")
                .address("세종대 정문")
                .mapLinks(new MapLinks("네이버2","카카오2","구글2"))
                .build();

        em.persist(place1);
        em.persist(place2);

        em.flush();
        em.clear();

        Place savedPlace1 = em.find(Place.class, 1L);
        Place savedPlace2 = em.find(Place.class, 2L);

        savedPlace1.addTag(tag1);
        savedPlace1.addTag(tag2);
        savedPlace1.addCategory(category1);

        savedPlace2.addTag(tag3);
        savedPlace2.addTag(tag4);
        savedPlace2.addCategory(category2);

    }
}
