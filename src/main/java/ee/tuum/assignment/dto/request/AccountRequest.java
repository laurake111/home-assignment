package ee.tuum.assignment.dto.request;

import ee.tuum.assignment.enums.Country;
import ee.tuum.assignment.enums.Currency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AccountRequest(
		@NotNull(message = "customerId is required")
		Long customerId,
		@NotNull(message = "country is required")
		Country country,
		@NotNull(message = "currencies is required")
		@NotEmpty(message = "currencies cannot be empty")
		List<Currency> currencies
) {}
