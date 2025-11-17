package ee.tuum.assignment.dto.event;

public record AccountCreationActionEvent(
		Long accountId,
		String country
) {}