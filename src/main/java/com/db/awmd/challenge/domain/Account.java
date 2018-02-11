package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class Account {

	@NotNull
	@NotEmpty
	private final String accountId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal balance;
	private Lock balanceLock = new ReentrantLock();
	private Thread balanceLockOwner;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}

	@JsonCreator
	public Account(@JsonProperty("accountId") String accountId,
				   @JsonProperty("balance") BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}

	public Account(String accountId, double balance) {
		this(accountId, new BigDecimal(balance));
	}

	public double readBalance() {
		return balance.floatValue();
	}

	public double readBalanceSynchronized() {
		try {
			balanceLock.lock();
			return readBalance();
		} finally {
			balanceLock.unlock();
		}
	}

	public Account lockBalance() {
		balanceLock.lock();
		balanceLockOwner = Thread.currentThread();
		return this;
	}

	public Account unlockBalance() {
		balanceLockOwner = null;
		balanceLock.unlock();
		return this;
	}

	public Account decreaseBalance(double amount) {
		checkBalanceLock();
		balance = balance.subtract(new BigDecimal(amount));
		return this;
	}

	public Account increaseBalance(double amount) {
		checkBalanceLock();
		balance = balance.add(new BigDecimal(amount));
		return this;
	}

	private void checkBalanceLock() {
		if (!Thread.currentThread().equals(balanceLockOwner)) {
			String ownerName = "Balance lock is owned by thread " +
					(balanceLockOwner != null ? balanceLockOwner.getName() : "none");
			throw new IllegalThreadStateException(ownerName);
		}
	}
}
