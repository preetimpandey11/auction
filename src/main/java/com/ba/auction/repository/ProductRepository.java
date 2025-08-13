package com.ba.auction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ba.auction.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByProductName(String name);
}