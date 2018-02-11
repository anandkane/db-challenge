package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@Slf4j
public class AccountsServiceTest {

	private AccountsService service = new AccountsService(new MockAccountRepository());

	@Before
	public void setup() {
		service.createAccount(new MockAccount("from", 100.0));
		service.createAccount(new MockAccount("to", 100.0));
	}

	@Test
	public void testTransfer() {
		Account from = service.getAccount("from");
		Account to = service.getAccount("to");

		service.transfer(from, to, 50);

		assertEquals(50.0, from.readBalance(), 0.0);
		assertEquals(150.0, to.readBalance(), 0.0);
	}

	@Test(expected = AccountNotFoundException.class)
	public void testInvalidTransferMissingFromAccount() {
		Transfer transfer = new Transfer("invalid", "to", 100);
		service.transfer(transfer);
	}

	@Test(expected = AccountNotFoundException.class)
	public void testInvalidTransferMissingToAccount() {
		Transfer transfer = new Transfer("from", "invalid", 100);
		service.transfer(transfer);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTransferInvalidAmount1() {
		Transfer transfer = new Transfer("from", "to", 0);
		service.transfer(transfer);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTransferInvalidAmount2() {
		Transfer transfer = new Transfer("from", "to", -1);
		service.transfer(transfer);
	}

	@Test(expected = InsufficientFundsException.class)
	public void testInvalidTransferInvalidAmount3() {
		Transfer transfer = new Transfer("from", "to", 110);
		service.transfer(transfer);
	}

	@Test
	public void testParallelReverseTransfer() throws InterruptedException {
		// Test case to show that simultaneous transfer between two accounts in opposite direction does not land in
		// deadlock.
		Account from = service.getAccount("from");
		Account to = service.getAccount("to");

		Thread t1 = new Thread(() -> service.transfer(from, to, 50), "T-1");
		Thread t2 = new Thread(() -> service.transfer(to, from, 30), "T-2");

		t1.start();
		// Make sure that first transfer takes place first.
		Thread.sleep(100);
		t2.start();

		t1.join();
		t2.join();

		assertEquals(80.0, from.readBalance(), 0.0);
		assertEquals(120.0, to.readBalance(), 0.0);
	}

	//	@Test
	public void testParallelTransferDeadlock() throws InterruptedException {
		// Test method to show that not sorting accounts in transfer results into dead lock. Dead lock detection code
		// is required to pass this test. Commenting @Test for now.
		MockAccountService service1 = new MockAccountService(new MockAccountRepository());
		Account from = new MockAccount("from", 100);
		Account to = new MockAccount("to", 100);

		Thread t1 = new Thread(() -> service1.transfer(from, to, 50), "T-1");
		Thread t2 = new Thread(() -> service1.transfer(to, from, 30), "T-2");

		t1.start();
		t2.start();

		t1.join();
		t2.join();
	}

	private static class MockAccountRepository extends AccountsRepositoryInMemory {

	}

	private static class MockAccountService extends AccountsService {

		public MockAccountService(AccountsRepository accountsRepository) {
			super(accountsRepository);
		}

		@Override
		public void transfer(Account from, Account to, double amount) {
			from.lockBalance();
			to.lockBalance();

			from.decreaseBalance(amount);
			to.increaseBalance(amount);

			from.unlockBalance();
			to.unlockBalance();
		}
	}

	private static class MockAccount extends Account {

		public MockAccount(String accountId) {
			super(accountId);
		}

		public MockAccount(String accountId, BigDecimal balance) {
			super(accountId, balance);
		}

		public MockAccount(String accountId, double balance) {
			super(accountId, balance);
		}

		@Override
		public Account lockBalance() {
			super.lockBalance();

			log.info("Thread " + Thread.currentThread().getName() + " locked " + getAccountId());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return this;
		}

		@Override
		public Account unlockBalance() {
			super.unlockBalance();

			log.info("Thread " + Thread.currentThread().getName() + " released " + getAccountId());
			return this;
		}
	}
}