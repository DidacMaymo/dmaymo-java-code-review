package schwarz.jobs.interview.coupon.core.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import schwarz.jobs.interview.coupon.core.domain.Coupon;
import schwarz.jobs.interview.coupon.core.exception.DuplicateCouponException;
import schwarz.jobs.interview.coupon.core.repository.CouponRepository;
import schwarz.jobs.interview.coupon.core.services.model.Basket;
import schwarz.jobs.interview.coupon.web.dto.CouponDTO;
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    public Optional<Coupon> getCoupon(final String code) {
        return couponRepository.findByCode(code);
    }

    public Optional<Basket> apply(final Basket basket, final String code) {

        return getCoupon(code).map(coupon -> {

            final BigDecimal minRequired = coupon.getMinBasketValue() != null
                    ? coupon.getMinBasketValue()
                    : BigDecimal.ZERO;

            final boolean qualifies = basket.getValue().compareTo(BigDecimal.ZERO) > 0
                    && basket.getValue().compareTo(minRequired) >= 0;

            if (qualifies) {
                basket.applyDiscount(coupon.getDiscount());
            }

            return basket;
        });
    }

    public Coupon createCoupon(final CouponDTO couponDTO) {

        if (couponRepository.findByCode(couponDTO.getCode()).isPresent()) {
            throw new DuplicateCouponException(couponDTO.getCode());
        }

        return couponRepository.save(toCoupon(couponDTO));
    }

    private Coupon toCoupon(final CouponDTO couponDTO) {
        return Coupon.builder()
                .code(couponDTO.getCode())
                .discount(couponDTO.getDiscount())
                .minBasketValue(couponDTO.getMinBasketValue())
                .build();
    }

    public List<Coupon> getCoupons(final CouponRequestDTO couponRequestDTO) {

        final ArrayList<Coupon> foundCoupons = new ArrayList<>();

        couponRequestDTO.getCodes().forEach(code -> foundCoupons.add(couponRepository.findByCode(code).get()));

        return foundCoupons;
    }
}
