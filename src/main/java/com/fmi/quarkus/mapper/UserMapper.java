package com.fmi.quarkus.mapper;

import com.fmi.quarkus.dto.UserDto;
import com.fmi.quarkus.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(mapRole(user))")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    default String mapRole(User user) {
        return user.role.name();
    }
}
