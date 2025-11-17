package ee.tuum.assignment.dto.request;

import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.enums.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
		@NotNull(message = "accountId is required")
		Long accountId,
		@NotNull(message = "Amount is required")
		@Positive(message = "Invalid amount")
		BigDecimal amount,
		@NotNull(message = "Currency is required")
		Currency currency,
		@NotNull(message = "Direction is required")
		Direction direction,
		@NotNull(message = "Description is required")
		@NotBlank(message = "Description is missing")
		String description
) {}
