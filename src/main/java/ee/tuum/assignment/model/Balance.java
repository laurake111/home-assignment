package ee.tuum.assignment.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Balance {
	Long id;
	Long accountId;
	String currency;
	BigDecimal availableAmount;
	OffsetDateTime createdAt;
	OffsetDateTime updatedAt;

	public Balance(Long accountId, String currency, BigDecimal availableAmount) {
		this.accountId = accountId;
		this.currency = currency;
		this.availableAmount = availableAmount;
	}
}
