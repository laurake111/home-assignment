package ee.tuum.assignment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.tuum.assignment.dto.response.AccountResponse;
import ee.tuum.assignment.dto.response.TransactionResponse;
import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.enums.Direction;
import ee.tuum.assignment.model.Balance;
import ee.tuum.assignment.model.Transaction;
import ee.tuum.assignment.persistence.BalanceMapper;
import ee.tuum.assignment.persistence.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BalanceMapper balanceMapper;

	@Autowired
	private TransactionMapper transactionMapper;

	private Long createTestAccount(String mockAccountJson) throws Exception {
		MvcResult result = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON)
						.content(mockAccountJson))
				.andExpect(status().isOk())
				.andReturn();

		AccountResponse created = objectMapper.readValue(result.getResponse()
				.getContentAsString(), AccountResponse.class);
		return created.accountId();
	}

	@Test
	void getTransactionById_returnsExistingTransaction() throws Exception {
		// when
		Long accountId = createTestAccount("""
				{
				  "customerId": 9876,
				  "country": "EE",
				  "currencies": ["EUR"]
				}
				""");

		String transactionIncreaseReqJson = """
				{
				  "accountId": %d,
				  "amount": "100",
				  "currency": "EUR",
				  "direction": "IN",
				  "description": "Add deposit"
				}
				""".formatted(accountId);


		MvcResult postResult = mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionIncreaseReqJson))
				.andExpect(status().isOk())
				.andReturn();

		String postResponseJson = postResult.getResponse()
				.getContentAsString();
		TransactionResponse created = objectMapper.readValue(postResponseJson, TransactionResponse.class);
		Long transactionId = created.transactionId();

		// then
		mockMvc.perform(get("/transaction/{id}", transactionId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionId").value(transactionId))
				.andExpect(jsonPath("$.accountId").value(accountId))
				.andExpect(jsonPath("$.amount").value(100))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.direction").value("IN"))
				.andExpect(jsonPath("$.description").value("Add deposit"));
	}

	@Test
	void getById_whenTransactionNotFound_returnsException() throws Exception {
		Long nonExistingId = -1L;

		mockMvc.perform(get("/transaction/{id}", nonExistingId))
				.andExpect(status().isNotFound());
	}


	@Test
	@Transactional
	void createTransaction_increasesBalance() throws Exception {
		// given
		Long accountId = createTestAccount("""
				{
				  "customerId": 9876,
				  "country": "EE",
				  "currencies": ["EUR", "USD"]
				}
				""");

		String transactionAmount = "100";
		String transactionReqJson = """
				{
				  "accountId": %d,
				  "amount": "%s",
				  "currency": "EUR",
				  "direction": "IN",
				  "description": "Add deposit"
				}
				""".formatted(accountId, transactionAmount);
		;

		// when
		var transactionRequest = mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionReqJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountId").value(accountId))
				.andExpect(jsonPath("$.amount").value(transactionAmount))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.direction").value("IN"))
				.andExpect(jsonPath("$.description").value("Add deposit"))
				.andExpect(jsonPath("$.balanceAfter").value(100))
				.andReturn();

		// then
		Balance balance = balanceMapper.findByAccountIdAndCurrency(accountId, Currency.EUR.name());
		assertThat(balance).isNotNull();
		assertThat(balance.getAvailableAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));

		TransactionResponse transactionResponse = objectMapper.readValue(transactionRequest.getResponse()
				.getContentAsString(), TransactionResponse.class);
		Long transactionId = transactionResponse.transactionId();

		Transaction transaction = transactionMapper.findById(transactionId);
		assertThat(transaction).isNotNull();
		assertThat(transaction.getId()).isEqualTo(transactionId);
		assertThat(transaction.getAccountId()).isEqualTo(accountId);
		assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
		assertThat(transaction.getCurrency()).isEqualTo(Currency.EUR);
		assertThat(transaction.getDirection()).isEqualTo(Direction.IN);
		assertThat(transaction.getDescription()).isEqualTo("Add deposit");
	}

	@Test
	@Transactional
	void createTransaction_decreaseBalance() throws Exception {
		// given
		Long accountId = createTestAccount("""
				{
				  "customerId": 9876,
				  "country": "EE",
				  "currencies": ["EUR"]
				}
				""");

		String transactionIncreaseReqJson = """
				{
				  "accountId": %d,
				  "amount": "100",
				  "currency": "EUR",
				  "direction": "IN",
				  "description": "Add deposit"
				}
				""".formatted(accountId);

		mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
				.content(transactionIncreaseReqJson));

		String transactionAmount = "40";
		String transactionReqJson = """
				{
				  "accountId": %d,
				  "amount": %s,
				  "currency": "EUR",
				  "direction": "OUT",
				  "description": "Decrease deposit"
				}
				""".formatted(accountId, transactionAmount);
		;

		// when
		MvcResult transactionRequest = mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionReqJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountId").value(accountId))
				.andExpect(jsonPath("$.amount").value(transactionAmount))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.direction").value("OUT"))
				.andExpect(jsonPath("$.description").value("Decrease deposit"))
				.andExpect(jsonPath("$.balanceAfter").value(60))
				.andReturn();

		// then
		Balance balance = balanceMapper.findByAccountIdAndCurrency(accountId, Currency.EUR.name());
		assertThat(balance).isNotNull();
		assertThat(balance.getAvailableAmount()).isEqualByComparingTo(BigDecimal.valueOf(60));


		TransactionResponse transactionResponse = objectMapper.readValue(transactionRequest.getResponse()
				.getContentAsString(), TransactionResponse.class);
		Long transactionId = transactionResponse.transactionId();

		Transaction transaction = transactionMapper.findById(transactionId);
		assertThat(transaction).isNotNull();
		assertThat(transaction.getId()).isEqualTo(transactionId);
		assertThat(transaction.getAccountId()).isEqualTo(accountId);
		assertThat(transaction.getAmount()).isEqualByComparingTo(transactionAmount);
		assertThat(transaction.getCurrency()).isEqualTo(Currency.EUR);
		assertThat(transaction.getDirection()).isEqualTo(Direction.OUT);
		assertThat(transaction.getDescription()).isEqualTo("Decrease deposit");
	}

	@Test
	@Transactional
	void createTransactionOut_insufficientFunds_returnsException() throws Exception {
		Long accountId = createTestAccount("""
				{
				  "customerId": 1111,
				  "country": "EE",
				  "currencies": ["EUR", "USD"]
				}
				""");

		String transactionReqJson = """
				{
				  "accountId": %d,
				  "currency": "EUR",
				  "amount": 100,
				  "direction": "OUT",
				  "description": "Attempt overdraft"
				}
				""".formatted(accountId);

		// when
		mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionReqJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Insufficient funds"));

		// then
		Balance balance = balanceMapper.findByAccountIdAndCurrency(accountId, Currency.EUR.name());
		assertThat(balance).isNotNull();
		assertThat(balance.getAvailableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@Transactional
	void createTransaction_missingBalanceForCurrency_returnsException() throws Exception {
		// given
		Long accountId = createTestAccount("""
				{
				  "customerId": 1111,
				  "country": "EE",
				  "currencies": ["EUR"]
				}
				""");

		String missingCurrency = "USD";
		String transactionReqJson = """
				{
				  "accountId": %d,
				  "currency": "%s",
				  "amount": 50,
				  "direction": "IN",
				  "description": "USD deposit"
				}
				""".formatted(accountId, missingCurrency);

		// when + then
		mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionReqJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(
						"Missing balance for account: " + accountId + " for currency: " + missingCurrency));
	}

	@Test
	void createTransaction_missingAccount_returnsException() throws Exception {
		long nonExistingAccountId = -1L;

		String transactionRequestJson = """
				{
				  "accountId": %d,
				  "currency": "EUR",
				  "amount": 10,
				  "direction": "IN",
				  "description": "Deposit to missing account"
				}
				""".formatted(nonExistingAccountId);

		mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionRequestJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Account missing"));
	}

	@Test
	void createTransaction_invalidDirection_returnsException() throws Exception {
		// given
		Long accountId = createTestAccount("""
				{
				  "customerId": 1111,
				  "country": "EE",
				  "currencies": ["EUR"]
				}
				""");

		String missingCurrency = "USD";
		String transactionReqJson = """
				{
				  "accountId": %d,
				  "currency": "%s",
				  "amount": 50,
				  "direction": "SPLIT",
				  "description": "USD deposit"
				}
				""".formatted(accountId, missingCurrency);

		// when + then
		mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionReqJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid direction"));
	}

	@Test
	void createTransaction_invalidDescription_returnsException() throws Exception {
		// given
		Long accountId = createTestAccount("""
				{
				  "customerId": 1111,
				  "country": "EE",
				  "currencies": ["EUR"]
				}
				""");

		String missingCurrency = "USD";
		String transactionReqJson = """
				{
				  "accountId": %d,
				  "currency": "%s",
				  "amount": 50,
				  "direction": "IN",
				  "description": ""
				}
				""".formatted(accountId, missingCurrency);

		// when + then
		mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON)
						.content(transactionReqJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Description is missing"));
	}
}