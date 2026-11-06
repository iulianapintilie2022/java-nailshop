package com.example.nailshop.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nailshop.demo.model.Customer;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {
                Optional<Customer> findByEmail(String email);
}