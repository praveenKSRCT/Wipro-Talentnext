package com.wipro.product.controller;

import com.wipro.product.dto.request.Product;
import com.wipro.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService) {

        this.productService = productService;
    }

    @PostMapping("/save")
    public Product save(
            @RequestBody Product product){

        return productService.save(product);
    }

    @GetMapping("/{id}")
    public Product getProduct(
            @PathVariable int id){

        return productService.findByID(id);
    }
}