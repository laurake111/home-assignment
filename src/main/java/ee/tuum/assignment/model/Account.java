package ee.tuum.assignment.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Account {
	Long id;
	Long customerId;
	String country; // country should also be an ENUM
	OffsetDateTime createdAt;
	OffsetDateTime updatedA;

	public Account(Long customerId, String country) {
		this.customerId = customerId;
		this.country = country;
	}
}
