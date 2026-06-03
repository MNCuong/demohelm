package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.StaticDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StaticDataRepository extends JpaRepository<StaticDataEntity,Integer> {
    @Transactional
    @Modifying
    @Query("""
        DELETE FROM StaticDataEntity s
        WHERE s.datacap = :vsdcode
           OR s.dataval = :vsdcode
           OR s.datacap = :biccode
           OR s.dataval = :biccode
    """)
    void deleteByVsdCodeOrBicCode(@Param("vsdcode") String vsdcode,
                                  @Param("biccode") String biccode);


    @Modifying
    @Query("UPDATE StaticDataEntity s SET s.datacap = :newVal WHERE s.datacap = :oldVal")
    void updateDatacap(@Param("oldVal") String oldVal, @Param("newVal") String newVal);

    @Modifying
    @Query("UPDATE StaticDataEntity s SET s.dataval = :newVal WHERE s.dataval = :oldVal")
    void updateDataval(@Param("oldVal") String oldVal, @Param("newVal") String newVal);

}
