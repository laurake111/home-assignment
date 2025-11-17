package ee.tuum.assignment.service;

import ee.tuum.assignment.dto.event.BalanceUpdateEvent;
import ee.tuum.assignment.dto.event.TransactionCreationEvent;
import ee.tuum.assignment.dto.request.TransactionRequest;
import ee.tuum.assignment.dto.response.BalanceResponse;
import ee.tuum.assignment.dto.response.TransactionResponse;
import ee.tuum.assignment.enums.Direction;
import ee.tuum.assignment.model.Account;
import ee.tuum.assignment.model.Balance;
import ee.tuum.assignment.model.Transaction;
import ee.tuum.assignment.persistence.AccountMapper;
import ee.tuum.assignment.persistence.BalanceMapper;
import ee.tuum.assignment.persistence.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static ee.tuum.assignment.rabbitConfig.RabbitConfig.*;

@RequiredArgsConstructor
@Service
public class TransactionService {
	private final AccountMapper accountMapper;
	private final BalanceMapper balanceMapper;
	private final TransactionMapper transactionMapper;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	public TransactionResponse createTransaction(TransactionRequest request) {
		Account account = accountMapper.findById(request.accountId());
		if (account == null) {
			throw new IllegalArgumentException("Account missing");
		}

		Balance balance =
				balanceMapper.findByAccountIdAndCurrency(request.accountId(), String.valueOf(request.currency()));
		if (balance == null) {
			throw new IllegalArgumentException(
					"Missing balance for account: " + request.accountId() + " for currency: " + request.currency());
		}

		BigDecimal balanceAfter =
				calculateNewBalance(balance.getAvailableAmount(), request.amount(), request.direction());
		balanceMapper.updateBalance(request.accountId(), String.valueOf(request.currency()), balanceAfter);

		Transaction newTransaction =
				new Transaction(request.accountId(), request.currency(), request.amount(), request.direction());

		newTransaction.setAccountId(request.accountId());
		newTransaction.setAmount(request.amount());
		newTransaction.setCurrency(request.currency());
		newTransaction.setDirection(request.direction());
		newTransaction.setDescription(request.description());

		transactionMapper.insert(newTransaction);

		rabbitTemplate.convertAndSend(ASSIGNMENT_EXCHANGE, BALANCE_UPDATED_KEY, new BalanceUpdateEvent(account.getId(),
				new BalanceResponse(balance.getCurrency(), balance.getAvailableAmount())));

		rabbitTemplate.convertAndSend(ASSIGNMENT_EXCHANGE, TRANSACTION_CREATED_KEY,
				new TransactionCreationEvent(newTransaction.getAccountId(), newTransaction.getId(),
						newTransaction.getAmount(), newTransaction.getCurrency(), newTransaction.getDirection()));

		return new TransactionResponse(request.accountId(), newTransaction.getId(), request.amount(),
				request.currency(), request.direction(), request.description(), balanceAfter);
	}

	public TransactionResponse getById(Long id) {
		Transaction transaction = transactionMapper.findById(id);
		if (transaction == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
		}
		return new TransactionResponse(transaction.getAccountId(), transaction.getId(), transaction.getAmount(),
				transaction.getCurrency(), transaction.getDirection(), transaction.getDescription(), null);
	}

	private BigDecimal calculateNewBalance(BigDecimal current, BigDecimal amount, Direction direction) {
		if (direction == Direction.IN) {
			return current.add(amount);
		} else if (direction == Direction.OUT) {
			if (current.compareTo(amount) < 0) {
				throw new IllegalArgumentException("Insufficient funds");
			}
			return current.subtract(amount);
		}
		throw new IllegalArgumentException("Invalid direction");
	}
}
