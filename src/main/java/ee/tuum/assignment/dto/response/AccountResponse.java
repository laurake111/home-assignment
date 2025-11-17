package ee.tuum.assignment.dto.response;

import java.util.List;

public record AccountResponse(
		Long accountId,
		Long customerId,
		List<BalanceResponse> balances
) {}