package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.ParticipantEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {
//    OR LOWER(p.shortname)   LIKE LOWER(CONCAT('%', :keyword, '%'))

    @Query("""
        SELECT p FROM ParticipantEntity p
        WHERE (:keyword IS NULL OR :keyword = '')
           OR LOWER(p.biccode)     LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.fullnameen)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.fullnamevn)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.shortname)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.vsdcode)  LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<ParticipantEntity> searchParticipants(@Param("keyword") String keyword,
                                               Pageable pageable);

    ParticipantEntity findByVsdcode(String vsdcode);

    @Query("SELECT p FROM ParticipantEntity p WHERE p.vsdcode = :vsd OR p.biccode = :bic OR p.shortname = :shortname")
    List<ParticipantEntity> findExisting(@Param("vsd") String vsd,
                                         @Param("bic") String bic,
                                         @Param("shortname") String shortname);

    List<ParticipantEntity> findByBiccodeNotInOrderByVsdcode(List<String> biccodes);


    Optional<ParticipantEntity> findById(Long id);

    boolean existsByVsdcodeAndIdNot(String vsdcode, Long id);

    boolean existsByBiccodeAndIdNot(String biccode,Long id);

    ParticipantEntity findByBiccode(String biccode);
    @Query("SELECT p.biccode FROM ParticipantEntity p WHERE p.biccode IS NOT NULL ORDER BY p.biccode ASC ")
    List<String> getAllBiccode();
    @Query("SELECT p.vsdcode FROM ParticipantEntity p WHERE p.vsdcode IS NOT NULL ORDER BY p.vsdcode ASC")
    List<String> getAllVsdCode();
}
