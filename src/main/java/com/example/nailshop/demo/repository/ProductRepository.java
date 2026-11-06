package com.example.nailshop.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nailshop.demo.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);
// JPA & Hibernate: mappes Java Objects & executes SQL
    boolean existsByCategoryId(Long categoryId);
}