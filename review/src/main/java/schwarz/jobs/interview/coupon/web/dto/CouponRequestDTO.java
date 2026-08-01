package schwarz.jobs.interview.coupon.web.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponRequestDTO {

    @NotEmpty
    private List<String> codes;

}
