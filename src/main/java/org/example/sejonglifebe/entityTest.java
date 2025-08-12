package org.example.sejonglifebe;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.sejonglifebe.place.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class entityTest implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) throws Exception {
        Place place = new Place("장소1", "ㅇㅇㅇㅇㅇ");
        Tag tag = new Tag();
        tag.setName("맛집");
        em.persist(tag);
        em.persist(place);

        place.addTags(tag);
    }
}
