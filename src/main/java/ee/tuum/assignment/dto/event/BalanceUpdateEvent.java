package ee.tuum.assignment.dto.event;

import ee.tuum.assignment.dto.response.BalanceResponse;

public record BalanceUpdateEvent(
		Long accountId,
		BalanceResponse balance
) {}
