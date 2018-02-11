package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	public void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	public void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
				.andExpect(status().isOk())
				.andExpect(
						content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	@Test
	public void testTransferSuccess() throws Exception {
		Account from = new Account("from", 100.0);
		Account to = new Account("to", 100.0);

		accountsService.createAccount(from);
		accountsService.createAccount(to);

		mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\": \"from\", \"toAccountId\": \"to\", \"amount\": \"50.0\"}"))
				.andExpect(status().isOk());

		from = accountsService.getAccount("from");
		to = accountsService.getAccount("to");

		assertThat(from.readBalance()).isEqualTo(50.0);
		assertThat(to.readBalance()).isEqualTo(150.0);
	}

	@Test
	public void testTransferInvalidFrom() throws Exception {
		Account from = new Account("from", 100.0);
		Account to = new Account("to", 100.0);

		accountsService.createAccount(from);
		accountsService.createAccount(to);

		mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\": \"invalid\", \"toAccountId\": \"to\", \"amount\": \"50.0\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testTransferInvalidTo() throws Exception {
		Account from = new Account("from", 100.0);
		Account to = new Account("to", 100.0);

		accountsService.createAccount(from);
		accountsService.createAccount(to);

		mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\": \"from\", \"toAccountId\": \"invalid\", \"amount\": \"50.0\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testTransferInvalidAmount() throws Exception {
		Account from = new Account("from", 100.0);
		Account to = new Account("to", 100.0);

		accountsService.createAccount(from);
		accountsService.createAccount(to);

		mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\": \"from\", \"toAccountId\": \"to\", \"amount\": \"0.0\"}"))
				.andExpect(status().isBadRequest());
	}
}
