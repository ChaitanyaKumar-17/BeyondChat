package com.manu.beyondchat.sql.repository;

import com.manu.beyondchat.dto.UserAuthView;
import com.manu.beyondchat.sql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,String> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCountryCodeAndPhoneNumber(String countryCode, String contactNumber);

    @Query("SELECT new com.manu.beyondchat.dto.UserAuthView(u.id, u.username, u.passwordHash) " +
            "FROM UserEntity u WHERE u.username = :username")
    Optional<UserAuthView> findAuthInfoByUsername(@Param("username") String username);
}
