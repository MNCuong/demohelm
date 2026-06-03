package com.example.web_monitor.service;

import com.example.web_monitor.model.entities.RegGroupUserEntity;
import com.example.web_monitor.repository.RegGroupUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegGroupUserService {
    private final RegGroupUserRepository regGroupUserRepository;

    public RegGroupUserService(RegGroupUserRepository regGroupUserRepository) {
        this.regGroupUserRepository = regGroupUserRepository;
    }
    public List<RegGroupUserEntity> getAllGroupUser() {
        return regGroupUserRepository.findAll();
    }
}
