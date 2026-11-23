package com.wipro.coupon.service;

import com.wipro.coupon.dto.request.Coupon;
import com.wipro.coupon.exception.CouponNotFoundException;
import com.wipro.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon findByCouponCode(String couponCode) {
        return couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() ->
                        new CouponNotFoundException("Coupon Not Found"));
    }
}