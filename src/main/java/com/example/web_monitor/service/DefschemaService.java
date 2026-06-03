package com.example.web_monitor.service;

import com.example.web_monitor.repository.DefschemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefschemaService {
    private final DefschemaRepository defschemaRepository;
    public DefschemaService(DefschemaRepository defschemaRepository) {
        this.defschemaRepository = defschemaRepository;
    }
    public List<String> getAllSchName(){
        return defschemaRepository.findAllSchNames();
    }

}
