package com.manu.beyondchat.service;

import com.manu.beyondchat.dto.UserAuthView;
import com.manu.beyondchat.sql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        UserAuthView userView = userRepository.findAuthInfoByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in database"));

        return User.builder()
                .username(userView.username())
                .password(userView.password())
                .build();
    }
}
