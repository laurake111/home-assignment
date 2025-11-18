package ee.tuum.assignment.dto.event;

import ee.tuum.assignment.enums.Country;

public record AccountCreationActionEvent(
		Long accountId,
		Country country
) {}