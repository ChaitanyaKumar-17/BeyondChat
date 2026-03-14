package com.manu.beyondchat.mapper;

import com.manu.beyondchat.dto.UserRegistrationDto;
import com.manu.beyondchat.sql.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateOfJoining", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    UserEntity toEntity(UserRegistrationDto dto);
}
