package com.texinsite.engine.repository;

import com.texinsite.engine.model.Document;
import com.texinsite.engine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // 基础的 save, findById, delete 等方法已经内置了
}