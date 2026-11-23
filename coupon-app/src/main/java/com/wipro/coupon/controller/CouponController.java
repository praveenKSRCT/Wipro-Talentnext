package com.wipro.coupon.controller;

import com.wipro.coupon.dto.request.Coupon;
import com.wipro.coupon.service.CouponService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /*@PostMapping("/save")
    public Coupon save(@RequestBody Coupon coupon){
        return couponService.save(coupon);
    }*/
    
    @PostMapping("/save")
    public Coupon save(@RequestBody Coupon coupon) {

        System.out.println("Coupon Code : " + coupon.getCouponCode());
        System.out.println("Description : " + coupon.getDescription());
        System.out.println("Discount : " + coupon.getDiscountPercentage());

        return couponService.save(coupon);
    }

    @GetMapping("/{couponCode}")
    public Coupon getCouponByCode(
            @PathVariable String couponCode){

        return couponService.findByCouponCode(couponCode);
    }
}