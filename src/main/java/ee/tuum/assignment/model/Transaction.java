package ee.tuum.assignment.model;

import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.enums.Direction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Transaction {
	Long id;
	Long accountId;
	Currency currency;
	BigDecimal amount;
	Direction direction;
	String description;
	OffsetDateTime createdAt;
	OffsetDateTime updatedAt;

	public Transaction(Long accountId, Currency currency, BigDecimal amount, Direction direction) {
		this.accountId = accountId;
		this.currency = currency;
		this.amount = amount;
		this.direction = direction;
	}
}
