package com.wipro.product.service;

import com.wipro.product.dto.request.Product;

public interface ProductService {

    Product save(Product product);

    Product findByID(int id);
}