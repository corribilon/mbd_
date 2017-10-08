package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MysqlManager {

	private static final Logger LOGGER = Tracer.getLogger(MysqlManager.class);
	
	protected Connection connect;
	
	public MysqlManager(Connection mm) {
		connect = mm;
	}
	

	public static synchronized Connection readDataBase(String username, String password,
			String dbname, String ip) {
		Connection connect = null;		
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://" + ip + "/"
					+ dbname + "?" + "user=" + username + "&password="
					+ password+"&connectTimeout=5000&socketTimeout=30000&autoReconnect=true");
		} catch (Exception e) {			
			LOGGER.log(Level.SEVERE, "Error trying to to read the database.", e);
		}
		return connect;
	}

	


}
