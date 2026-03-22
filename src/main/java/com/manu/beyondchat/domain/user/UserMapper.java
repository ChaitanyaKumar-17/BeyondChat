package com.manu.beyondchat.domain.user;

import com.manu.beyondchat.domain.registration.dto.UserRegistrationDto;
import com.manu.beyondchat.domain.user.repository.sql.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    UserEntity toEntity(UserRegistrationDto dto);
}
