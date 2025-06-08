package com.hoz.back.repository;

import com.hoz.back.model.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    Categories findByName(String name);
}
