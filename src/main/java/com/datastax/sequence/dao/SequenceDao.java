package com.datastax.sequence.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.WriteTimeoutException;

public class SequenceDao {

	private static Logger logger = LoggerFactory.getLogger(SequenceDao.class);
	private Session session;

	private static String keyspaceName = "datastax_unique_versions_demo";
	private static String seqtable = keyspaceName + ".sequence";

	private String UPDATE_SEQUENCE = "update " + seqtable + " set sequence = ? where id = ? if sequence = ?";
	private String READ_FROM_SEQUENCE = "select sequence from " + seqtable + " where id = ?";

	private PreparedStatement update;
	private PreparedStatement read;

	public SequenceDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();

		this.update = session.prepare(UPDATE_SEQUENCE);
		this.read = session.prepare(READ_FROM_SEQUENCE);
		this.read.setConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL);
	}

	public int read(String id) {
		ResultSetFuture resultSet = session.executeAsync(read.bind(id));
		Row row = resultSet.getUninterruptibly().one();
		if (row == null) {
			session.execute("insert into " + seqtable + " (id, sequence) values ('" + id + "', 0)");
			return 0;
		} else {
			return row.getInt("sequence");
		}
	}

	public boolean update(String id, int newSequence, int oldSequence) {
		
		try {
			ResultSetFuture resultSet = this.session.executeAsync(update.bind(newSequence, id, oldSequence));
			if (resultSet != null) {
				Row row = resultSet.getUninterruptibly().one();
				return row.getBool(0);
			}
		} catch (WriteTimeoutException e) {
			logger.warn(e.getMessage());
			return false;
		}
		
		
		return true;
	}
}
