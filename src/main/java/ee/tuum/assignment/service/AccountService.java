package ee.tuum.assignment.service;

import ee.tuum.assignment.dto.event.AccountCreationActionEvent;
import ee.tuum.assignment.dto.event.BalanceCreationActionEvent;
import ee.tuum.assignment.dto.request.AccountRequest;
import ee.tuum.assignment.dto.response.AccountResponse;
import ee.tuum.assignment.dto.response.BalanceResponse;
import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.model.Account;
import ee.tuum.assignment.model.Balance;
import ee.tuum.assignment.persistence.AccountMapper;
import ee.tuum.assignment.persistence.BalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static ee.tuum.assignment.rabbitConfig.RabbitConfig.*;

@RequiredArgsConstructor
@Service
public class AccountService {
	private final AccountMapper accountMapper;
	private final BalanceMapper balanceMapper;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	public AccountResponse createAccount(AccountRequest request) {
		Account account = new Account(request.customerId(), request.country());
		accountMapper.insert(account);

		List<Balance> balancesToDb = new ArrayList<>();
		for (Currency currency : request.currencies()) {
			Balance balance = new Balance(account.getId(), currency.toString(), BigDecimal.ZERO);
			balanceMapper.insertBalance(balance);
			balancesToDb.add(balance);
		}

		List<BalanceResponse> balances = balancesToDb.stream()
				.map(b -> new BalanceResponse(b.getCurrency(), b.getAvailableAmount()))
				.toList();

		rabbitTemplate.convertAndSend(ASSIGNMENT_EXCHANGE, ACCOUNT_CREATED_KEY,
				new AccountCreationActionEvent(account.getId(), account.getCountry()));

		rabbitTemplate.convertAndSend(ASSIGNMENT_EXCHANGE, BALANCE_CREATED_KEY,
				new BalanceCreationActionEvent(account.getId(), balances));

		return new AccountResponse(account.getId(), account.getCustomerId(), balances);
	}

	public AccountResponse getAccountById(Long accountId) {
		Account account = accountMapper.findById(accountId);
		if (account == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
		}

		List<Balance> balances = balanceMapper.findByAccountId(accountId);
		List<BalanceResponse> balanceResponses = balances.stream()
				.map(b -> new BalanceResponse(b.getCurrency(), b.getAvailableAmount()))
				.toList();

		return new AccountResponse(account.getId(), account.getCustomerId(), balanceResponses);
	}
}
