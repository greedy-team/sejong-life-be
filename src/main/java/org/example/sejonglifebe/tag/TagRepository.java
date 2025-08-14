package org.example.sejonglifebe.tag;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {

    public List<Tag> findByNameIn(List<String> names);
}
