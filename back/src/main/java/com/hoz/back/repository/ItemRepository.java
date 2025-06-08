package com.hoz.back.repository;

import com.hoz.back.model.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ItemRepository extends JpaRepository<Items, Long> {
    Items findByName(String name);
    List<Items> findByUserId(Long id);


}
