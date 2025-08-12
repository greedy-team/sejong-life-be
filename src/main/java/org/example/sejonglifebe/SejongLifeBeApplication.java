package org.example.sejonglifebe;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.sejonglifebe.place.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SejongLifeBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SejongLifeBeApplication.class, args);
    }



}
