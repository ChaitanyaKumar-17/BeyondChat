package com.manu.beyondchat.service;

import com.manu.beyondchat.dto.EmailVerificationDto;
import com.manu.beyondchat.dto.UserRegistrationDto;
import com.manu.beyondchat.mapper.UserMapper;
import com.manu.beyondchat.sql.entity.UserEntity;
import com.manu.beyondchat.sql.entity.enums.UserStatus;
import com.manu.beyondchat.sql.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public void createUser(UserRegistrationDto userDto){

        if (userRepository.existsByUsername(userDto.getUsername())){
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber())){
            throw new RuntimeException("Phone Number already exists");
        }

        UserEntity user = userMapper.toEntity(userDto);
        user.setDateOfJoining(LocalDate.now());
        user.setStatus(UserStatus.OFFLINE);

        userRepository.save(user);
    }

    public boolean verifyUser(EmailVerificationDto emailDto){
        return userRepository.existsByEmail(emailDto.getEmail());
    }

}
