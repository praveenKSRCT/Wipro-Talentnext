package com.wipro.product.client;

import com.wipro.product.dto.request.Coupon;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "COUPON-APP")
public interface CouponFeignClient {

    @GetMapping("/api/v1/coupons/{couponCode}")
    Coupon getCoupon(
            @PathVariable("couponCode")
            String couponCode);
}