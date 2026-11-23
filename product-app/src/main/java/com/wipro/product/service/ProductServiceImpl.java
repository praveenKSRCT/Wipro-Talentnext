package com.wipro.product.service;

import com.wipro.product.client.CouponFeignClient;
import com.wipro.product.dto.request.Coupon;
import com.wipro.product.dto.request.Product;
import com.wipro.product.exception.ProductNotFoundException;
import com.wipro.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl
        implements ProductService {

    private final ProductRepository productRepository;
    private final CouponFeignClient couponFeignClient;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CouponFeignClient couponFeignClient) {

        this.productRepository = productRepository;
        this.couponFeignClient = couponFeignClient;
    }

    @Override
    public Product save(Product product) {

        return productRepository.save(product);
    }

    @Override
    public Product findByID(int id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product Not Found"));

        Coupon coupon =
                couponFeignClient.getCoupon(
                        product.getCouponCode());

        int discount =
                coupon.getDiscountPercentage();

        double finalPrice =
                product.getPrice()
                        - ((product.getPrice() * discount)/100);

        product.setDiscount(discount);
        product.setFinalPrice(finalPrice);

        return product;
    }
}