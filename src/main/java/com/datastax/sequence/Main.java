package com.datastax.sequence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;
import com.datastax.sequence.dao.SequenceService;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	private static int NO_OF_SEQUENCES = 100000;
	private int noOfThreads;
	private AtomicLong counter = new AtomicLong(0);

	public Main() {

		String noOfThreadsStr = PropertyHelper.getProperty("noOfThreads", "5");
		noOfThreads = Integer.parseInt(noOfThreadsStr);

		SequenceService service = new SequenceService();
		ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
		Timer timer = new Timer();
		timer.start();

		for (int i = 0; i < noOfThreads; i++) {
			executor.execute(new Writer(service));
		}

		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class Writer implements Runnable {

		private SequenceService service;

		public Writer(SequenceService service) {
			this.service = service;
		}

		@Override
		public void run() {
			while(true){

				String issuer = "" + (new Double(Math.random() * 1000000).intValue());
				service.getNextSequenceNo(issuer);
				counter.incrementAndGet();
				
				if (counter.get()%10000==0){
					logger.info("Total : " + counter.get());
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();

		System.exit(0);
	}
}
