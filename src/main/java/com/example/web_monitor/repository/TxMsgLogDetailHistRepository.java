package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.TxMsgLogDetailHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxMsgLogDetailHistRepository extends JpaRepository<TxMsgLogDetailHistEntity, Long>
{
}
