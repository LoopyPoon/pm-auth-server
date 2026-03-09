package com.pm.pmauthservice.mapper;

import com.pm.pmauthservice.dto.RegisterResponse;
import com.pm.pmauthservice.entity.Role;
import com.pm.pmauthservice.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "message", constant = "Регистрация успешна!")
    RegisterResponse toRegisterResponse(User user);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
