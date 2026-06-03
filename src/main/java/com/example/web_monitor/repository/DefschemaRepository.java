package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.DefschemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefschemaRepository extends JpaRepository<DefschemaEntity, Long> {

    @Query("select d.schname from DefschemaEntity d order by d.schname asc")
    List<String> findAllSchNames();
}
