package ee.tuum.assignment.dto.response;

import java.math.BigDecimal;

public record BalanceResponse(
        String currency,
        BigDecimal availableAmount
) {}
