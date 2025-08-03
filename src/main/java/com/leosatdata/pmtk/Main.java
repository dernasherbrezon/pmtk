package com.leosatdata.pmtk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.fazecast.jSerialComm.SerialPort;

public class Main {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("missing arguments. format: pmtk <serial interface> <baud rate> <pmtk command>");
			System.exit(1);
			return;
		}
		final SerialPort port = SerialPort.getCommPort(args[0]);
		port.setBaudRate(Integer.valueOf(args[1]));
		port.setParity(SerialPort.NO_PARITY);
		port.setNumStopBits(SerialPort.ONE_STOP_BIT);
		port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10000, 10000);
		if (!port.openPort()) {
			System.err.println("cannot open port");
			System.exit(1);
			return;
		}
		CountDownLatch latch = new CountDownLatch(1);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
				String curLine = null;
				try {
					while (!Thread.currentThread().isInterrupted() && (curLine = reader.readLine()) != null) {
						if (curLine.startsWith("$G")) {
							continue;
						}
						System.out.println(curLine.trim());
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			}
		}, "reader");
		t.setDaemon(true);
		t.start();
		String command = args[2];
		if (!command.startsWith("$")) {
			command = "$" + command;
		}
		String fullMessage;
		int star = command.indexOf('*');
		if (star != -1) {
			fullMessage = command.substring(1, star);
		} else {
			fullMessage = command.substring(1);
		}
		String toSend = "$" + fullMessage + "*" + calculateChecksum(fullMessage) + "\r\n";
		System.err.println("Sending: " + toSend.trim());
		try {
			port.getOutputStream().write(toSend.getBytes(StandardCharsets.US_ASCII));
			port.getOutputStream().flush();
		} catch (IOException e) {
			System.err.println("unable to send command");
			e.printStackTrace();
			port.closePort();
			System.exit(1);
			return;
		}

		try {
			latch.await(10000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.err.println("cannot wait any longer");
			e.printStackTrace();
			port.closePort();
			System.exit(1);
			return;
		}
		port.closePort();
	}

	private static String calculateChecksum(String message) {
		int result = 0;
		for (int i = 0; i < message.length(); i++) {
			result = result ^ message.charAt(i);
		}
		return Integer.toHexString(result).toUpperCase();
	}

}
