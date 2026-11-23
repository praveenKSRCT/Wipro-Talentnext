package com.wipro.product.repository;

import com.wipro.product.dto.request.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product,Integer> {
}
