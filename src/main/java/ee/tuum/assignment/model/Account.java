package ee.tuum.assignment.model;

import ee.tuum.assignment.enums.Country;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Account {
	Long id;
	Long customerId;
	Country country;
	OffsetDateTime createdAt;
	OffsetDateTime updatedA;

	public Account(Long customerId, Country country) {
		this.customerId = customerId;
		this.country = country;
	}
}
