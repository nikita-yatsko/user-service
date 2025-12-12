package com.userservise.app.repository;

import com.userservise.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    // Named method
    Boolean existsByEmail(String email);

    Boolean existsByUserId(Integer userId);

    Void deleteUserByUserId(Integer userId);

    // JPQL
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findUserByUserId(Integer userId);

    // Native Sql
    @Query(value = "SELECT * FROM users u WHERE u.email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);



}
