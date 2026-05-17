package org.example.sejonglifebe.meeting.repository;

import org.example.sejonglifebe.meeting.entity.ContactViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactViewHistoryRepository extends JpaRepository<ContactViewHistory, Long> {

    boolean existsByViewerIdAndTargetId(Long viewerId, Long targetId);
}
