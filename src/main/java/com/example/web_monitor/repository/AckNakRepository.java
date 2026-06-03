package com.example.web_monitor.repository;

import com.example.web_monitor.dto.AckNakDto;
import com.example.web_monitor.model.entities.AckNakEntity;
import com.example.web_monitor.model.entities.MessageEntity;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AckNakRepository extends JpaRepository<AckNakEntity, Long> {
    Optional<AckNakEntity> findBySeqid(String seqId);


    @Query("""
    SELECT new com.example.web_monitor.dto.AckNakDto(
        s.autoid,
        s.seqid,
        p.fullnameen,
        p.shortname,
        p.biccode,
        s.status,
        s.msg,
        s.parentmsg,
        s.actiontime,
        s.updatetime,
        s.corerefid
    )
    FROM AckNakEntity s
    LEFT JOIN ParticipantEntity p ON s.biccode = p.biccode
    WHERE (:sendingPlace IS NULL 
           OR (:sendingPlace = 'UNKNOWN' AND p.biccode IS NULL)
           OR (:sendingPlace != 'UNKNOWN' AND p.biccode IS NOT NULL AND p.shortname = :sendingPlace))
      AND (:refSeqId IS NULL OR s.seqid LIKE %:refSeqId%)
      AND (:status IS NULL OR s.status = :status)
      AND (:txDateFrom IS NULL OR s.actiontime >= :txDateFrom)
      AND (:txDateTo IS NULL OR s.actiontime <= :txDateTo)
    ORDER BY s.actiontime DESC
    """)
    Page<AckNakDto> searchAckNakWithParticipant(
            @Param("sendingPlace") String sendingPlace,
            @Param("refSeqId") String refSeqId,
            @Param("status") String status,
            @Param("txDateFrom") LocalDateTime txDateFrom,
            @Param("txDateTo") LocalDateTime txDateTo,
            Pageable pageable
    );

    Page<AckNakEntity> findAll(Specification<AckNakEntity> spec, Pageable pageable);

    AckNakDto.Detail findByAutoid(Long autoid);

    void deleteBySeqid(@Size(max = 35) String seqid);

    void deleteAllBySeqid(@Size(max = 35) String seqid);

    Optional<AckNakEntity> findFirstBySeqidAndBiccodeOrderByAutoidDesc(@Size(max = 35) String seqid, String biccode);

    void deleteAllBySeqidIn(Collection<String> seqids);
    @Query("""
    SELECT r.status as status, COUNT(r) as total
    FROM AckNakEntity r
    GROUP BY r.status
""")
    List<Object[]>   countByStatus();
}
