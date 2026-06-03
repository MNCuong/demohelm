package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.UserDto;
import com.example.web_monitor.model.entities.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T15:36:19+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setEmail( userDto.getEmail() );
        userEntity.setNotes( userDto.getNotes() );
        userEntity.setUsername( userDto.getUsername() );

        return userEntity;
    }

    @Override
    public UserDto toDto(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setRole( roleEntityToString( userEntity.getRole() ) );
        userDto.setEmail( userEntity.getEmail() );
        userDto.setEnabled( userEntity.isEnabled() );
        userDto.setNotes( userEntity.getNotes() );
        userDto.setUsername( userEntity.getUsername() );

        return userDto;
    }
}
