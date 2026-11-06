package com.example.nailshop.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nailshop.demo.model.Category;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    
}

