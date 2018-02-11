package com.db.awmd.challenge.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountTest {

	@Test
	public void testIncrease() {
		Account account = new Account("1", 100.2);
		account.lockBalance();
		account.increaseBalance(100.3);
		account.unlockBalance();

		double balance = account.readBalance();
		assertEquals(balance, 200.5, 0.0);
	}

	@Test(expected = IllegalThreadStateException.class)
	public void testIncreaseUnlocked() {
		Account account = new Account("1", 100.2);
		account.increaseBalance(100);
	}

	@Test(expected = IllegalThreadStateException.class)
	public void testParallelIncrease() throws InterruptedException {
		Account account = new Account("1");
		Thread t1 = new Thread(() -> {
			account.lockBalance();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			} finally {
				account.unlockBalance();
			}
		}, "Lock-Owner");


		t1.start();
		Thread.sleep(200);
		try {
			account.increaseBalance(100.2);
		} catch (Exception e) {
			throw e;
		} finally {
			t1.join();
		}
	}

	@Test
	public void testDecrease() {
		Account account = new Account("1", 100.2);
		account.lockBalance();
		account.decreaseBalance(50.2);
		account.unlockBalance();

		double balance = account.readBalance();
		assertEquals(balance, 50.0, 0.0);
	}

	@Test(expected = IllegalThreadStateException.class)
	public void testDecreaseUnlocked() {
		Account account = new Account("1", 100.2);
		account.increaseBalance(100);
	}

	@Test(expected = IllegalThreadStateException.class)
	public void testParallelDecrease() throws InterruptedException {
		Account account = new Account("1");
		Thread t1 = new Thread(() -> {
			account.lockBalance();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			} finally {
				account.unlockBalance();
			}
		}, "Lock-Owner");


		t1.start();
		Thread.sleep(200);
		try {
			account.decreaseBalance(100.2);
		} catch (Exception e) {
			throw e;
		} finally {
			t1.join();
		}
	}

	@Test
	public void testRead() {
		Account account = new Account("1", 100);
		assertEquals(100.0, account.readBalance(), 0.0);
	}

	@Test
	public void testReadBalanceLocked() throws InterruptedException {
		Account account = new Account("1", 100);

		Thread t1 = new Thread(() -> {
			account.lockBalance();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			} finally {
				account.increaseBalance(100).unlockBalance();
			}
		});

		t1.start();
		Thread.sleep(100);

		double balance = account.readBalance();
		t1.join();

		assertEquals(100.0, balance, 0.0);
	}

	@Test
	public void testReadSynchronized() throws InterruptedException {
		Account account = new Account("1", 100);

		Thread t1 = new Thread(() -> {
			account.lockBalance();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			} finally {
				account.increaseBalance(100).unlockBalance();
			}
		});

		t1.start();
		Thread.sleep(100);

		double balance = account.readBalanceSynchronized();
		assertEquals(200.0, balance, 0.0);
	}
}