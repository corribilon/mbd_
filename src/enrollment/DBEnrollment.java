package enrollment;

import java.sql.ResultSet;

import java.util.logging.Level;
import java.util.logging.Logger;

import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class DBEnrollment extends MysqlManager{

	private static final Logger LOGGER = Tracer.getLogger(DBEnrollment.class);	

	public boolean updateDigitalCharacterics(String idMat, String characterics,
			String hash) {

		boolean toRet = true;

		String sql = "INSERT INTO 01_tbl_rellotgenew "
				+ "(iduser,labelid,characterics,hash)" + "VALUES" + "(" + idMat
				+ ",-50,'" + characterics + "','" + hash + "') "
				+ "ON DUPLICATE KEY UPDATE " + "characterics='" + characterics
				+ "'," + "hash='" + hash + "'";

		try {
			if (TestConnection.isUp()) {
				statement = connect.createStatement();
				statement.executeUpdate(sql);

			} else {
				System.out
						.println("No Connection to DDBB. Sending result to the buffer.");
				toRet = false;
			}
		} catch (Exception e) {
			LOGGER.log(Level.INFO,
					"Error while pushing data to DDBB: " + e.toString(), e);
			toRet = false;
		}

		return toRet;

	}

	public String getUserInfo(String idMat) {
		String toRet = null;

		String sql = "SELECT * FROM `datos` WHERE `Id Matrícula`=" + idMat;

		try {
			if (TestConnection.isUp()) {
				statement = connect.createStatement();
				ResultSet rs = statement.executeQuery(sql);

				if (rs.first()) {
					
					toRet = rs.getString("Nombre") + " "
							+ rs.getString("Apellidos");
				}
			} else {
				LOGGER.log(Level.INFO, "No hi ha connexió a internet.");
				toRet = null;
			}
		} catch (Exception e) {
			LOGGER.log(Level.INFO,
					"Error while pushing data to DDBB: " + e.toString(), e);
			toRet = null;
		}

		return toRet;
	}



}
