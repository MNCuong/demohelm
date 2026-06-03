package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.UserDto;
import com.example.web_monitor.model.entities.RoleEntity;
import com.example.web_monitor.model.entities.UserEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring") // Báo cho MapStruct tạo Spring Bean
public interface UserMapper {
    @Mapping(target = "id", ignore = true)       // Bỏ qua id, vì đây là entity mới
    @Mapping(target = "password", ignore = true) // Bỏ qua, Service sẽ mã hóa
    @Mapping(target = "role", ignore = true)    // Bỏ qua, Service sẽ tìm RoleEntity từ String
    @Mapping(target = "enabled", ignore = true)  // Bỏ qua, Service sẽ gán giá trị mặc định
    UserEntity toEntity(UserDto userDto);


    @Mapping(target = "password", ignore = true)
    @Mapping(source = "role", target = "role", qualifiedByName = "roleEntityToString")
    UserDto toDto(UserEntity userEntity);

    @Named("roleEntityToString")
    default String roleEntityToString(RoleEntity role) {
        if (role == null) {
            return null;
        }
        
        String roleName = role.getName(); 
        
        if (roleName.startsWith("ROLE_")) {
            return roleName.substring(5);
        }
        return roleName;
    }
}