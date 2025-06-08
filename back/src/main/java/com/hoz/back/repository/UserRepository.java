package com.hoz.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hoz.back.model.Users;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
    Users findByEmail(String email);

}