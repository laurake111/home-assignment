package ee.tuum.assignment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.enums.Direction;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
		Long accountId,
		Long transactionId,
		BigDecimal amount,
		Currency currency,
		Direction direction,
		String description,
		BigDecimal balanceAfter
) {}
