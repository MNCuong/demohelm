package com.example.web_monitor.service;

import com.example.web_monitor.dto.MenuDto;
import com.example.web_monitor.mapper.MenuMapper;
import com.example.web_monitor.model.entities.MenuEntity;
import com.example.web_monitor.repository.MenuRepository;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    private static final Logger log = getLogger(lookup().lookupClass());

    @Autowired
    public MenuService(MenuRepository menuRepository, MenuMapper menuMapper) {
        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
    }

    @Cacheable(value = "menuTreeDto", key = "#authentication.name")
    @Transactional(readOnly = true) // Tối ưu cho việc đọc
    public List<MenuDto> getActiveMenuTree(Authentication authentication) {
        log.info("Cache MISS cho 'menuTreeDto' cua user: {}", authentication.getName());

        // 1. Lấy danh sách tên Role từ 'authentication'
        Set<String> roleNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
         log.info("roleNames cua user: {}", roleNames);
        //Nếu user không có role, trả về danh sách rỗng
        if (roleNames.isEmpty()) {
            return new ArrayList<>();
        }

        //Tải tất cả menu active CỦA USER NÀY trong 1 câu query
        List<MenuEntity> allActiveMenus = menuRepository.findActiveMenusByRole(
                MenuEntity.IS_ACTIVE,
                roleNames
        );

        //Chuyển đổi tất cả sang DTO và đưa vào Map để tra cứu
        Map<Long, MenuDto> menuDtoMap = allActiveMenus.stream()
                .map(menuMapper::toDto)
                .collect(Collectors.toMap(MenuDto::getId, dto -> dto));

        //Xây dựng cây menu
        List<MenuDto> rootMenus = new ArrayList<>();
        for (MenuDto currentDto : menuDtoMap.values()) {
            Long parentId = currentDto.getParentId();

            if (parentId == null) {
                rootMenus.add(currentDto);
            } else {
                MenuDto parentDto = menuDtoMap.get(parentId);
                if (parentDto != null) {
                    parentDto.getChildren().add(currentDto);
                }
            }
        }
        
        return rootMenus;
    }

    /**
     * Dùng để xóa cache thủ công (nếu cần).
     * allEntries = true là bắt buộc để xóa cache của TẤT CẢ user.
     */
//    @CacheEvict(value = "menuTreeDto", allEntries = true)
//    public void evictAllMenuCaches() {
//        log.info("Đã xóa toàn bộ cache 'menuTreeDto'.");
//    }
    @CacheEvict(value = "menuTreeDto", key = "#authentication.name")
    public void evictUserMenuCache(Authentication authentication) {
        log.info("Da xoa cache menu cua user: {}", authentication.getName());
    }
}