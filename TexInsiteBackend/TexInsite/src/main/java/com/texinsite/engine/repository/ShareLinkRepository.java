package com.texinsite.engine.repository;

import com.texinsite.engine.model.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    Optional<ShareLink> findByToken(String token);

    java.util.List<ShareLink> findByDocument_User_Username(String username);
}
