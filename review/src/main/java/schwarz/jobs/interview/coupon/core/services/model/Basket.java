package schwarz.jobs.interview.coupon.core.services.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Basket {

    @NotNull
    private BigDecimal value;

    private BigDecimal appliedDiscount;

    private boolean applicationSuccessful;

    public void applyDiscount(final BigDecimal discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        this.appliedDiscount = discount;
        this.value = value.subtract(discount);
        this.applicationSuccessful = true;
    }

}
