package com.datastax.sequence.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.PropertyHelper;

public class SequenceService {
	
	private SequenceDao dao = new SequenceDao(PropertyHelper.getProperty("contactPoints", "localhost").split(","));
	private static Logger logger = LoggerFactory.getLogger( SequenceService.class );
	
	public int getNextSequenceNo(String id){
		//Read the sequence
		int seq = dao.read(id);
		
		//If we can't update, then someone else has used it 
		while (!dao.update(id, seq + 1, seq)){
			
			logger.info("Failed updating sequence for " + (seq + 1));
			
			//so read again and try to update
			seq = dao.read(id);
		}
		
		//When this thread has updated the sequence we know that it can use the 
		return seq;
	}
}
