package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.RegUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RegUsersRepository extends JpaRepository<RegUserEntity,Long> {

    RegUserEntity findByParticipantId(Long participantId);

    RegUserEntity findByVsdcode(String vsdcode);

    @Query("SELECT p.biccode " +
            "FROM RegUserEntity r JOIN ParticipantEntity p ON r.vsdcode = p.vsdcode " +
            "WHERE r.usrname = :username")
    String findBiccode(@Param("username") String username);

    @Query("SELECT DISTINCT usrname FROM RegUserEntity ORDER BY usrname ASC")
    List<String> findAllUsrname();
}
