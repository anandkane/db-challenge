package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
@Slf4j
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transfer(Transfer transfer) {
		Account from = accountsRepository.getAccount(transfer.getFromAccountId());
		Account to = accountsRepository.getAccount(transfer.getToAccountId());

		AccountNotFoundException exception = new AccountNotFoundException();
		exception.addAccountId(() -> from == null ? transfer.getFromAccountId() : null);
		exception.addAccountId(() -> to == null ? transfer.getToAccountId() : null);
		exception.throwMe();

		transfer(from, to, transfer.getAmount());
	}

	public void transfer(@NotNull Account from, @NotNull Account to, double amount) {
		// To be called withing a transaction and following code be pushed to data access layer
		log.info(String.format("Amount transfer initiated: [From: %s, To: %s, Amt: %f", from.getAccountId(), to.getAccountId(), amount));

		if (amount <= 0) {
			throw new IllegalArgumentException("Invalid amount " + amount);
		}

		Account bigger = from;
		Account smaller = to;
		int compareTo;
		if ((compareTo = from.getAccountId().compareTo(to.getAccountId())) == 0) {
			throw new IllegalArgumentException("Cannot transfer amount to the same account");
		}

		if (compareTo < 1) {
			bigger = to;
			smaller = from;
		}

		bigger.lockBalance();
		smaller.lockBalance();

		// Move this check into separate class so that balance check can be externalised.
		double balance = from.readBalanceSynchronized();
		if (balance < amount) {
			log.error("Insufficient funds: Requested: " + amount + ", Balance: " + balance);
			throw new InsufficientFundsException(from);
		}

		try {
			from.decreaseBalance(amount);
			to.increaseBalance(amount);

			// Logic to persist changes under an active transaction
		} catch (Exception e) {
			log.error("Amount transfer failed", e);
		} finally {
			bigger.unlockBalance();
			smaller.unlockBalance();
		}

		log.info("Amount transfer successful");
	}
}
