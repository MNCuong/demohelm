package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.SentMsgEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SentMsgRepository extends JpaRepository<SentMsgEntity, Long> {

    void deleteByCoreRefId(String coreRefId);

    Optional<SentMsgEntity> findByCoreRefIdOrderByAutoIdDesc(String coreRefId);

    @Query("""
                SELECT s
                FROM SentMsgEntity s
                RIGHT JOIN MessageEntity m
                     ON   s.coreRefId=m.orgSeqId
                WHERE m.autoId = :txAutoId
                AND s.refSender=:refSender
                  AND s.actionTime >= m.txDate
                ORDER BY s.actionTime DESC
            """)
    Optional<SentMsgEntity> findByAutoId(@Param("txAutoId") String txAutoId, @Param("refSender") String refSender);

    List<SentMsgEntity> findByCoreRefIdIn(List<String> orgseqids);

    Optional<SentMsgEntity> findByCoreRefIdAndRefSender(String coreRefId, String refSender);

    List<SentMsgEntity> findByCoreRefIdInAndStatus(Collection<String> coreRefIds, String status);

    void deleteByCoreRefIdIn(Collection<String> coreRefIds);
    @Query("""
               select count(distinct s.coreRefId)
               from SentMsgEntity s
               where s.coreRefId in :orgSeqIds
                 and s.status = 'N'
            """)
    long countDistinctCoreRefIdStatusN(
            @Param("orgSeqIds") List<String> orgSeqIds
    );
}

