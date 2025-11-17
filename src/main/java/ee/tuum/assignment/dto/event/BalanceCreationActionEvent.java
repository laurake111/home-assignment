package ee.tuum.assignment.dto.event;

import ee.tuum.assignment.dto.response.BalanceResponse;

import java.util.List;

public record BalanceCreationActionEvent (
	Long accountId,
	List<BalanceResponse> balances
){}
