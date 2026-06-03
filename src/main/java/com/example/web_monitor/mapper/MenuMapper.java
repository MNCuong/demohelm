package com.example.web_monitor.mapper;

import com.example.web_monitor.model.entities.MenuEntity;
import com.example.web_monitor.dto.MenuDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class MenuMapper {

    @Mapping(target = "children", ignore = true)
    @Mapping(source = "parent.id", target = "parentId") 
    public abstract MenuDto toDto(MenuEntity entity);
}