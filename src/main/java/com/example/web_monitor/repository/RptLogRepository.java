package com.example.web_monitor.repository;

import com.example.web_monitor.dto.RptLogDto;
import com.example.web_monitor.model.entities.RptlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface RptLogRepository extends JpaRepository<RptlogEntity, Long>, JpaSpecificationExecutor<RptlogEntity> {
    RptlogEntity findByCorerefid(String corerefid);

    RptlogEntity findByAutoid(Long autoid);

    void deleteAllByCorerefid(String corerefid);

    void deleteAllByCorerefidIn(Collection<String> corerefids);
    @Query("""
    SELECT r.status as status, COUNT(r) as total
    FROM RptlogEntity r
    WHERE
        ( :txDateFrom IS NULL OR r.createdate >= :txDateFrom )
    AND ( :txDateTo IS NULL OR r.createdate < :txDateTo )
    GROUP BY r.status
""")
    List<Object[]> countByStatus(
            @Param("txDateFrom") LocalDateTime txDateFrom,
            @Param("txDateTo") LocalDateTime txDateTo
    );
}
