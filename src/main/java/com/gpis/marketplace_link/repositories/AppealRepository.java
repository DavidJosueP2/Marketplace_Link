package com.gpis.marketplace_link.repositories;

import com.gpis.marketplace_link.entities.Appeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppealRepository extends JpaRepository<Appeal,Long> {

    boolean existsByIncidenceId(Long incidenceId);

    @Query(value = """
        SELECT a 
        FROM Appeal a 
        JOIN FETCH a.incidence i
        JOIN FETCH i.moderator
        WHERE a.status = com.gpis.marketplace_link.enums.AppealStatus.FAILED_NO_MOD AND 
              a.newModerator IS NULL
    """)
    List<Appeal> findAllPendingWithModerator();

}
