package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.MessageEntity;
import com.example.web_monitor.model.entities.TxMsgLogDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TxMsgLogDetailRepository extends JpaRepository<TxMsgLogDetail, Long> {
    List<TxMsgLogDetail> findByOrgSeqIdOrderByTxDateAsc(String orgSeqId);

    Optional<TxMsgLogDetail> findByOrgSeqIdAndAutoId(String orgSeqId, Long autoId);

    TxMsgLogDetail findTopByOrderByAutoIdDesc();

    List<TxMsgLogDetail> findByAutoIdGreaterThanAndStatusInAndTxDateBetween(
            long lastAutoId,
            List<String> statuses,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    TxMsgLogDetail findByAutoIdAndOrgSeqId(Long autoId, String orgSeqId);

    List<TxMsgLogDetail> findAllByOrgSeqIdAndAutoId(String orgSeqId, Long autoId);

    List<TxMsgLogDetail> findAllByOrgSeqId(String orgSeqId);
    @Modifying
    @Query("delete from TxMsgLogDetail d where d.orgSeqId = :orgSeqId")
    void deleteByOrgSeqId(@Param("orgSeqId") String orgSeqId);

    List<TxMsgLogDetail> findByAutoIdGreaterThanAndStatusInOrderByAutoIdAsc(Long autoIdIsGreaterThan, Collection<String> statuses);

    @Query("""
    SELECT d
    FROM TxMsgLogDetail d
    JOIN MessageEntity m
      ON d.autoId = m.autoId AND d.orgSeqId = m.orgSeqId
    WHERE d.autoId > :lastAutoId
      AND d.status IN :statuses
      AND m.inOutFlag = 'I'
    ORDER BY d.autoId ASC
""")
    List<TxMsgLogDetail> findInMsgDetailForLatency(
            @Param("lastAutoId") Long lastAutoId,
            @Param("statuses") Collection<String> statuses
    );



    List<TxMsgLogDetail> findByAutoIdGreaterThanAndStatusIn(Long autoIdIsGreaterThan, Collection<String> statuses);

    List<TxMsgLogDetail> findAllByOrgSeqIdIn(Collection<String> orgSeqIds);

    void deleteByOrgSeqIdIn(Collection<String> orgSeqIds);
}
