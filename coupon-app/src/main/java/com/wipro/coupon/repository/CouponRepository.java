package com.wipro.coupon.repository;

import com.wipro.coupon.dto.request.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository
        extends JpaRepository<Coupon,Integer> {

    Optional<Coupon> findByCouponCode(String couponCode);
}