package ee.tuum.assignment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.tuum.assignment.dto.response.AccountResponse;
import ee.tuum.assignment.model.Account;
import ee.tuum.assignment.model.Balance;
import ee.tuum.assignment.persistence.AccountMapper;
import ee.tuum.assignment.persistence.BalanceMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
		properties = "spring.rabbitmq.listener.direct.auto-startup=false"

)
@AutoConfigureMockMvc
public class AccountIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private BalanceMapper balanceMapper;

	private Long createAccount(String mockAccountReqJson) throws Exception {
		MvcResult result = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON)
						.content(mockAccountReqJson))
				.andExpect(status().isOk())
				.andReturn();

		AccountResponse created = objectMapper.readValue(result.getResponse()
				.getContentAsString(), AccountResponse.class);
		return created.accountId();
	}

	@Test
	void createAccountAndBalances_successfully() throws Exception {
		// when
		Long accountId = createAccount("""
				{
				  "customerId": 1234,
				  "country": "EE",
				  "currencies": ["EUR", "USD"]
				}
				""");

		// then
		Account account = accountMapper.findById(accountId);
		assertThat(account).isNotNull();
		assertThat(account.getId()).isEqualTo(accountId);
		assertThat(account.getCustomerId()).isEqualTo(1234L);
		assertThat(account.getCountry()).isEqualTo("EE");

		List<Balance> balances = balanceMapper.findByAccountId(accountId);
		assertThat(balances).hasSize(2);
		assertThat(balances).extracting(Balance::getCurrency)
				.containsExactlyInAnyOrder("EUR", "USD");
		assertThat(balances).extracting(Balance::getAvailableAmount)
				.allMatch(amount -> BigDecimal.ZERO.compareTo(amount) == 0);
	}

	@Test
	void getAccountById_returnsExistingAccount() throws Exception {
		// given
		Long accountId = createAccount("""
				{
				  "customerId": 123,
				  "country": "EE",
				  "currencies": ["EUR", "SEK"]
				}
				""");

		// when + then
		mockMvc.perform(get("/account/{id}", accountId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountId").value(accountId))
				.andExpect(jsonPath("$.balances.length()").value(2))
				.andExpect(jsonPath("$.balances[0].currency").value("EUR"))
				.andExpect(jsonPath("$.balances[1].currency").value("SEK"))
				.andExpect(jsonPath("$.balances[0].availableAmount").value(0))
				.andExpect(jsonPath("$.balances[1].availableAmount").value(0));

	}

	@Test
	void getAccountById_nonExistingAccount_returnsException() throws Exception {
		Long nonExistingId = -1L;

		mockMvc.perform(get("/account/{id}", nonExistingId))
				.andExpect(status().isNotFound());
	}

	@Test
	void createAccount_invalidCurrency_returnsException() throws Exception {
		String requestJson = """
            {
              "customerId": 123,
              "country": "EE",
              "currencies": ["EURR"]
            }
            """;

		mockMvc.perform(post("/account")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid currency"));
	}
}
