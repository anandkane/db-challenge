package com.db.awmd.challenge.exception;

import com.db.awmd.challenge.domain.Account;

public class InsufficientFundsException extends RuntimeException {
	private final Account source;

	public InsufficientFundsException(Account source) {
		this.source = source;
	}

	@Override
	public String getMessage() {
		return "Available balance in account with id " + source.getAccountId() + " is " + source.getBalance();
	}
}
