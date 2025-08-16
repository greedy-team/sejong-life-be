package org.example.sejonglifebe.tag;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByNameIn(List<String> names);

}
