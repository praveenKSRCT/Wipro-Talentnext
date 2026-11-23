package com.wipro.coupon.service;

import com.wipro.coupon.dto.request.Coupon;

public interface CouponService {

    Coupon save(Coupon coupon);

    Coupon findByCouponCode(String couponCode);
}