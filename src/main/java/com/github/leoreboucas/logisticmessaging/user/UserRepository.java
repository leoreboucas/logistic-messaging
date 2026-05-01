package com.github.leoreboucas.logisticmessaging.user;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByDocument(String document);
}
