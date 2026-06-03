package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.MenuDto;
import com.example.web_monitor.model.entities.MenuEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T15:36:18+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MenuMapperImpl extends MenuMapper {

    @Override
    public MenuDto toDto(MenuEntity entity) {
        if ( entity == null ) {
            return null;
        }

        MenuDto.MenuDtoBuilder menuDto = MenuDto.builder();

        menuDto.parentId( entityParentId( entity ) );
        menuDto.displayOrder( entity.getDisplayOrder() );
        menuDto.id( entity.getId() );
        menuDto.isActive( entity.getIsActive() );
        menuDto.isLast( entity.getIsLast() );
        menuDto.lvel( entity.getLvel() );
        menuDto.name( entity.getName() );
        menuDto.objectName( entity.getObjectName() );

        return menuDto.build();
    }

    private Long entityParentId(MenuEntity menuEntity) {
        if ( menuEntity == null ) {
            return null;
        }
        MenuEntity parent = menuEntity.getParent();
        if ( parent == null ) {
            return null;
        }
        Long id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
