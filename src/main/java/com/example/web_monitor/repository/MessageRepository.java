package com.example.web_monitor.repository;

import com.example.web_monitor.dto.StatisticMessageDto;
import com.example.web_monitor.dto.TxDateDto;
import com.example.web_monitor.model.entities.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long>, JpaSpecificationExecutor<MessageEntity> {

    Optional<MessageEntity> findByAutoId(Long autoId);

    @Query("""
SELECT new com.example.web_monitor.dto.StatisticMessageDto(
    CASE 
        WHEN m.refCode = 'UNDEFINED' THEN
            CASE
                WHEN SUBSTRING(m.refSender,8,1)='X'
                    THEN SUBSTRING(m.refSender,5,3)
                ELSE SUBSTRING(m.refSender,5,4)
            END
        ELSE m.refCode
    END,

    SUM(CASE WHEN m.inOutFlag = 'I' THEN 1 ELSE 0 END),
    SUM(CASE WHEN m.inOutFlag = 'E' THEN 1 ELSE 0 END),
    COUNT(m)
)
FROM MessageEntity m
WHERE m.txDate >= :fromDate
  AND m.txDate <  :toDate
  AND (:keyword IS NULL OR
       LOWER(m.refCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
GROUP BY
    CASE 
        WHEN m.refCode = 'UNDEFINED' THEN
            CASE
                WHEN SUBSTRING(m.refSender,8,1)='X'
                    THEN SUBSTRING(m.refSender,5,3)
                ELSE SUBSTRING(m.refSender,5,4)
            END
        ELSE m.refCode
    END

ORDER BY 
    SUM(CASE WHEN m.inOutFlag = 'I' THEN 1 ELSE 0 END) DESC
""")
    Page<StatisticMessageDto> statisticForDashboard(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    @Query("SELECT COUNT(m) FROM MessageEntity m " +
            "WHERE m.inOutFlag = 'I' AND m.txDate >= :start AND m.txDate < :end")
    Long countTotalInBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(m) FROM MessageEntity m " +
            "WHERE m.inOutFlag = 'E' AND m.txDate >= :start AND m.txDate < :end")
    Long countTotalOutBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT
            m.TXDATE,
            m.CREATETST,
            TRIM(m.TRFCODE) AS TRFCODE,
            m.REFSENDER,
            m.REFRECEIVER,
            -- Chuyển đổi hiển thị
            CASE 
                WHEN m.STATUS = 'P' THEN 'ACK'
                WHEN m.STATUS = 'E' THEN 'NAK'
            END AS DB_STATUS,
            m.REFSEQID,
            m.STPREFID,
            m.INOUTFLAG,
            m.ORGSEQID
        FROM TXMSGLOG m
        WHERE (
            (m.USRNAME = :refSender AND m.INOUTFLAG = 'I' AND m.STATUS IN ('P', 'E')
)
            OR
            (m.REFRECEIVER = :refSenderBIC AND m.INOUTFLAG = 'E' AND m.STATUS ='P'
)
        )
        AND m.TXDATE BETWEEN :start AND :end ORDER BY m.TXDATE desc
    """, nativeQuery = true)
    List<Object[]> findByRefSenderAndTxDateBetweenWithPagination(
            @Param("refSender") String refSender,
            @Param("refSenderBIC") String refSenderBIC,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );


    @Query("SELECT COUNT(m) " +
            "FROM MessageEntity m " +
            "WHERE m.status = 'E' " +
            "AND m.txDate >= :fromDate " +
            "AND m.txDate < :toDate")
    int countErrorsBetween(@Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate);


    @Query("""
            select m.txDate as txDate
            from MessageEntity m
            where m.txDate >= :start
              and m.txDate < :end
            order by m.txDate
            """)
    List<TxDateDto> findTxDateOnly(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
                SELECT
                    CAST(m.txDate AS date),
                    COUNT(m.autoId) 
                FROM MessageEntity m 
                WHERE m.txDate >= :startDate 
                  AND m.txDate < :endDate 
                GROUP BY CAST(m.txDate AS date) 
                ORDER BY CAST(m.txDate AS date)
            """)
    List<Object[]> countElectricByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Modifying
    @Query("delete from MessageEntity m where m.orgSeqId = :orgSeqId")
    void deleteByOrgSeqId(@Param("orgSeqId") String orgSeqId);

    @Query("""
               select m from MessageEntity m
               where m.orgSeqId = :orgSeqId
            """)
    List<MessageEntity> findLiteByOrgSeqId(@Param("orgSeqId") String orgSeqId);

    Optional<MessageEntity> findBySeqId(String seqId);

    @Query("""
            SELECT t
            FROM MessageEntity t
            LEFT JOIN SentMsgEntity s
                 ON s.coreRefId = t.orgSeqId AND s.refSender = t.refSender
            WHERE (:sentStatus IS NULL
                   OR (s.status = :sentStatus AND s.coreRefId IS NOT NULL)
                   OR (:sentStatus = 'unknown' AND s.coreRefId IS NULL))
              AND (:refSeqId IS NULL OR t.orgSeqId = :refSeqId)
              AND (:relatedRef IS NULL OR t.stpRefId = :relatedRef)
              AND (:trfCode IS NULL OR t.trfCode = :trfCode)
              AND (:refCode IS NULL OR t.refCode = :refCode)
              AND (:refReceiver IS NULL OR t.refReceiver = :refReceiver)
              AND (:fromDate IS NULL OR t.txDate >= :fromDate)
              AND (:toDate IS NULL OR t.txDate <= :toDate)
              AND t.inOutFlag = 'E'
              AND t.status = 'P'
            """)
    Page<MessageEntity> searchWithSentStatus(
            @Param("sentStatus") String sentStatus,
            @Param("refSeqId") String refSeqId,
            @Param("relatedRef") String relatedRef,
            @Param("trfCode") String trfCode,
            @Param("refCode") String refCode,
            @Param("refReceiver") String refReceiver,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM MessageEntity t
            WHERE t.status = 'P'
              AND t.inOutFlag = 'I'
              AND (:refSeqId IS NULL OR t.orgSeqId = :refSeqId)
              AND (:relatedRef IS NULL OR t.stpRefId = :relatedRef)
              AND (:trfCode IS NULL OR t.trfCode = :trfCode)
              AND (:refCode IS NULL OR t.refCode = :refCode)
              AND (:refReceiver IS NULL OR t.refReceiver = :refReceiver)
              AND (:fromDate IS NULL OR t.txDate >= :fromDate)
              AND (:toDate IS NULL OR t.txDate <= :toDate)
            """)
    Page<MessageEntity> findPendingInboundWithoutSent(
            @Param("refSeqId") String refSeqId,
            @Param("relatedRef") String relatedRef,
            @Param("trfCode") String trfCode,
            @Param("refCode") String refCode,
            @Param("refReceiver") String refReceiver,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );




    void deleteByOrgSeqIdIn(Collection<String> orgSeqIds);

    List<MessageEntity> findLiteByOrgSeqIdIn(Collection<String> orgSeqIds);

    List<MessageEntity> findByOrgSeqIdInAndInOutFlag(Collection<String> orgSeqIds, String inOutFlag);

    @Query("""
    SELECT
        SUM(CASE WHEN t.status = 'N' THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = 'E' THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = 'P' AND t.inOutFlag = 'I' THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = 'P' AND t.inOutFlag = 'E'
                  AND s.status = 'N'
             THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = 'P' AND t.inOutFlag = 'E'
                  AND s.status = 'S'
             THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = 'P' AND t.inOutFlag = 'E'
                  AND s.coreRefId IS NULL
             THEN 1 ELSE 0 END)
    FROM MessageEntity t
    LEFT JOIN SentMsgEntity s
        ON s.coreRefId = t.orgSeqId
        AND s.refSender = t.refSender
    WHERE
    ( :fromDate IS NULL OR t.txDate >= :fromDate )
AND ( :toDate IS NULL OR t.txDate <= :toDate )
""")
    List<Object[]> countByBusinessStatus(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    @Query("""
        SELECT m
        FROM MessageEntity m
        WHERE m.stpRefId = :orgSeqId
        AND m.inOutFlag = 'E'
        AND (
              UPPER(m.trfCode) LIKE '%PACK'
           OR UPPER(m.trfCode) LIKE '%REJT'
        )
        ORDER BY m.createTst DESC
    """)
    Optional<MessageEntity> findFirstResponseMessage(
            @Param("orgSeqId") String orgSeqId
    );
}