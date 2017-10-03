package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MysqlManager {

	private static final Logger LOGGER = Tracer.getLogger(MysqlManager.class);

	protected Connection connect = null;
	protected Statement statement = null;

	private ResultSet resultSet = null;

	public boolean readDataBase(String username, String password,
			String dbname, String ip) {

		boolean toRet = true;
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://" + ip + "/"
					+ dbname + "?" + "user=" + username + "&password="
					+ password);
		} catch (Exception e) {
			toRet = false;
			LOGGER.log(Level.SEVERE, "Error trying to to read the database.", e);

		}
		return toRet;
	}

	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.toString(), e);
		}
	}


}
