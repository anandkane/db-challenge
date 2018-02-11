package com.db.awmd.challenge.exception;

import java.util.ArrayList;
import java.util.List;

public class AccountNotFoundException extends RuntimeException {
	private List<String> accountIds = new ArrayList<>();

	public AccountNotFoundException(List<String> accountIds) {
		this.accountIds = accountIds;
	}

	public AccountNotFoundException() {
	}

	public void addAccountId(java.util.function.Supplier<String> supplier) {
		if (supplier.get() != null) {
			accountIds.add(supplier.get());
		}
	}

	@Override
	public String getMessage() {
		return "Account(s) with id(s) " + accountIds + " could not be found";
	}

	public void throwMe() {
		if (!accountIds.isEmpty()) {
			throw this;
		}
	}
}
