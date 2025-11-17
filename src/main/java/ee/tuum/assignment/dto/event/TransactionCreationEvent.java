package ee.tuum.assignment.dto.event;

import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.enums.Direction;

import java.math.BigDecimal;

public record TransactionCreationEvent (
		Long accountId,
		Long transactionId,
		BigDecimal amount,
		Currency currency,
		Direction direction
) {
}
