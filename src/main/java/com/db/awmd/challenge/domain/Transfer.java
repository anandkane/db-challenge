package com.db.awmd.challenge.domain;

public class Transfer {
	private String fromAccountId;
	private String toAccountId;
	private double amount;

	public Transfer() {

	}

	public Transfer(String fromAccountId, String toAccountId, double amount) {
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("{").append("fromAccountId='").append(fromAccountId)
				.append('\'').append(", toAccountId='").append(toAccountId).append('\'').append(", amount=")
				.append(amount).append('}').toString();
	}

	public String getFromAccountId() {
		return fromAccountId;
	}

	public void setFromAccountId(String fromAccountId) {
		this.fromAccountId = fromAccountId;
	}

	public String getToAccountId() {
		return toAccountId;
	}

	public void setToAccountId(String toAccountId) {
		this.toAccountId = toAccountId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
}
