package com.datastax.sequence;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;
import com.datastax.sequence.dao.SequenceService;

public class Main {
	private static Logger logger = LoggerFactory.getLogger( Main.class );
	private static int NO_OF_SEQUENCES = 100000;
	private int noOfThreads;

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
		
		try {
			executor.shutdownNow();
			executor.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.end();
		logger.info(NO_OF_SEQUENCES + " sequences acquired in " + timer.getTimeTakenSeconds() + " secs.");
	}


	class Writer implements Runnable {

		private SequenceService service;
		
		public Writer(SequenceService service) {
			this.service = service;
		}

		@Override
		public void run() {
			for  (int i = 0; i < NO_OF_SEQUENCES/noOfThreads; i ++) {
					
				String issuer = "" + (new Double(Math.random() * 1000000).intValue());				
				service.getNextSequenceNo(issuer);
				//logger.info(service.getNextSequenceNo(issuer) + "");
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
