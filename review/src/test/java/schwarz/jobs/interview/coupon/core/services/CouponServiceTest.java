package schwarz.jobs.interview.coupon.core.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import schwarz.jobs.interview.coupon.core.domain.Coupon;
import schwarz.jobs.interview.coupon.core.repository.CouponRepository;
import schwarz.jobs.interview.coupon.core.services.model.Basket;
import schwarz.jobs.interview.coupon.web.dto.CouponDTO;
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDTO;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Test
    public void createCoupon() {
        CouponDTO dto = CouponDTO.builder()
            .code("12345")
            .discount(BigDecimal.TEN)
            .minBasketValue(BigDecimal.valueOf(50))
            .build();

        couponService.createCoupon(dto);

        verify(couponRepository, times(1)).save(any());
    }

    @Test
    void should_apply_discount_when_basket_qualifies() {
        final Basket basket = Basket.builder()
                .value(BigDecimal.valueOf(100))
                .build();

        when(couponRepository.findByCode("1111")).thenReturn(Optional.of(Coupon.builder()
                .code("1111")
                .discount(BigDecimal.TEN)
                .minBasketValue(BigDecimal.valueOf(50))
                .build()));

        final Optional<Basket> optionalBasket = couponService.apply(basket, "1111");

        assertThat(optionalBasket).hasValueSatisfying(b -> {
            assertThat(b.getAppliedDiscount()).isEqualTo(BigDecimal.TEN);
            assertThat(b.isApplicationSuccessful()).isTrue();
        });
    }

    @Test
    void should_not_apply_discount_when_basket_value_is_zero() {
        final Basket basket = Basket.builder()
                .value(BigDecimal.ZERO)
                .build();

        when(couponRepository.findByCode("1111")).thenReturn(Optional.of(Coupon.builder()
                .code("1111")
                .discount(BigDecimal.TEN)
                .minBasketValue(BigDecimal.valueOf(50))
                .build()));

        final Optional<Basket> result = couponService.apply(basket, "1111");

        assertThat(result).hasValueSatisfying(b -> {
            assertThat(b).isEqualTo(basket);
            assertThat(b.isApplicationSuccessful()).isFalse();
            assertThat(b.getAppliedDiscount()).isNull();
        });
    }

    @Test
    void should_not_apply_discount_when_basket_below_minimum() {
        final Basket basket = Basket.builder()
                .value(BigDecimal.valueOf(30))
                .build();

        when(couponRepository.findByCode("1111")).thenReturn(Optional.of(Coupon.builder()
                .code("1111")
                .discount(BigDecimal.TEN)
                .minBasketValue(BigDecimal.valueOf(50))
                .build()));

        final Optional<Basket> result = couponService.apply(basket, "1111");

        assertThat(result).hasValueSatisfying(b -> {
            assertThat(b.isApplicationSuccessful()).isFalse();
            assertThat(b).isEqualTo(basket);
            assertThat(b.getAppliedDiscount()).isNull();
        });
    }

    @Test
    void should_apply_discount_when_coupon_has_no_minimum() {
        final Basket basket = Basket.builder()
                .value(BigDecimal.valueOf(10))
                .build();

        when(couponRepository.findByCode("1111")).thenReturn(Optional.of(Coupon.builder()
                .code("1111")
                .discount(BigDecimal.TEN)
                .minBasketValue(null)
                .build()));

        final Optional<Basket> result = couponService.apply(basket, "1111");

        assertThat(result).hasValueSatisfying(b -> assertThat(b.isApplicationSuccessful()).isTrue());
    }

    @Test
    public void should_test_get_Coupons() {

        CouponRequestDTO dto = CouponRequestDTO.builder()
            .codes(Arrays.asList("1111", "1234"))
            .build();

        when(couponRepository.findByCode(any()))
            .thenReturn(Optional.of(Coupon.builder()
                .code("1111")
                .discount(BigDecimal.TEN)
                .minBasketValue(BigDecimal.valueOf(50))
                .build()))
            .thenReturn(Optional.of(Coupon.builder()
                .code("1234")
                .discount(BigDecimal.TEN)
                .minBasketValue(BigDecimal.valueOf(50))
                .build()));

        List<Coupon> returnedCoupons = couponService.getCoupons(dto);

        assertThat(returnedCoupons.get(0).getCode()).isEqualTo("1111");

        assertThat(returnedCoupons.get(1).getCode()).isEqualTo("1234");
    }
}
