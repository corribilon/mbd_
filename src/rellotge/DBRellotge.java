package rellotge;

import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import common.BufferManager;

import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class DBRellotge extends MysqlManager {

	private static final Logger LOGGER = Tracer.getLogger(DBRellotge.class);

	public DBRellotge(Connection connect) {
		super(connect);
	}

	public void updateStatus(String[] res) throws ParseException {
		for (int i = 0; i < res.length; i++) {
			if (res[i].contains("opt:")) {
				insertAbsentisme(res, i);
			} else {
				insertRellotge(res, i);
			}
		}
	}

	private void insertAbsentisme(String res[], int i) {

		// Get the data

		
		String[] data = res[i].split(";");
		String concept = data[0].replace("opt:", "");
		String labelId = data[1].replace("iduser:", "").split("/")[0];
		String date = data[1].replace("iduser:", "").split("/")[1];
		HashMap<String, ArrayList<String>> mapa = SearchSensor.getMapa();
		if (mapa != null) {			
			try {
				String idUser = mapa.get(labelId).get(0);
				// Construct the sql
				String sql = "INSERT INTO absentismo (`Matrícula`,`Data del concepte`, `Concepte`)VALUES(" + idUser + ",'"
						+ date + "','" + concept + "')";
				LOGGER.log(Level.INFO, "UPD: "+sql);
				if (TestConnection.isUp()) {
					Statement statement = connect.createStatement();
					int resI = statement.executeUpdate(sql);
					LOGGER.log(Level.INFO, "UPDATED ELEMENT:\t\t " + res[i] + "\t\t" + resI);
					if (resI == 0) {
						BufferManager.movementsFunction(BufferManager.PUT, res[i]);
					}
				} else {
					LOGGER.log(Level.INFO,"No Connection to DDBB. Sending result to the buffer.");
					BufferManager.movementsFunction(BufferManager.PUT, res[i]);
				}
			} catch (CommunicationsException e) {
				LOGGER.log(Level.INFO, "Communications Exception. Sending result to the buffer: " + e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			} catch (SQLException e) {
				LOGGER.log(Level.INFO, "Exception on the Database Side. Sending result back to the buffer: " + e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			} catch (SocketException e) {
				LOGGER.log(Level.WARNING, "Error with the socket. Sending result back to the buffer: " + e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			}
		} else {
			LOGGER.log(Level.WARNING,
					"Map is null son cant send the data [" + res[i] + "] to the server... Taking back to the buffer");
			BufferManager.movementsFunction(BufferManager.PUT, res[i]);
		}

		
	}

	private void insertRellotge(String[] res, int i) {
		int resI = -1;
		String time = null;
		String labelId = null;
		String[] arr = null;
		arr = getArray(res, i);
		if (arr != null) {
			labelId = arr[0];
			time = arr[1];
			String sql = "INSERT INTO rellotge (userid,id,time,entradasortida)" + "VALUES (-1," + labelId + ", '" + time
					+ "', " + Rellotge.prop.getProperty("entrada") + ")";
			try {
				if (TestConnection.isUp()) {
					Statement statement = connect.createStatement();
					resI = statement.executeUpdate(sql);
					System.out.println("UPDATED ELEMENT:\t\t " + res[i] + "\t\t" + resI);
					if (resI == 0) {
						BufferManager.movementsFunction(BufferManager.PUT, res[i]);
					}
				} else {
					System.out.println("No Connection to DDBB. Sending result to the buffer.");
					BufferManager.movementsFunction(BufferManager.PUT, res[i]);
				}
			} catch (CommunicationsException e) {
				LOGGER.log(Level.INFO, "Communications Exception. Sending result to the buffer: " + e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			} catch (SQLException e) {
				LOGGER.log(Level.INFO, "Exception on the Database Side. Sending back to the buffer the result: " + e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			} catch (SocketException e) {
				LOGGER.log(Level.WARNING, "Error with the socket. " + e.toString(), e);
				BufferManager.movementsFunction(BufferManager.PUT, res[i]);
			}
		} else {
			System.out.println("IGNORING ELEMENT: \t\t " + res[i]);
		}
	}

	private String[] getArray(String[] res, int i) {
		String[] lineRead = res[i].split("/");
		long h = -1;
		try {
			h = Long.parseLong(lineRead[0].trim());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Read " + lineRead + " is not a number! Ignoring this element!");
			return null;
		}
		String[] toRet = new String[2];
		toRet[0] = h + ""; // labelId
		toRet[1] = lineRead[1];
		return toRet;
	}

	public void sendHeartBeat() {
		String idheartbeat = getIdHeartbeat();
		String numLiniaStr = String.format("%02d", Rellotge.getNumLinia());
		String sql = "INSERT INTO 02_tbl_heartbeat (`idheartbeat`, `idlinia`) VALUES(" + idheartbeat + ", "
				+ numLiniaStr + ") ON DUPLICATE KEY UPDATE idlinia=" + numLiniaStr + ", `time`=CURRENT_TIMESTAMP;";
		try {
			if (TestConnection.isUp()) {
				Statement statement = connect.createStatement();
				statement.executeUpdate(sql);
			}
		} catch (CommunicationsException e) {
			LOGGER.log(Level.INFO, "Communications Exception. " + e.toString(), e);
		} catch (SQLException e) {
			LOGGER.log(Level.INFO, e.toString(), e);
		} catch (SocketException e) {
			LOGGER.log(Level.WARNING, "Error with the socket. " + e.toString(), e);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error sending heartbeat " + e.toString(), e);
		}
	}

	public static String getIdHeartbeat() {
		Calendar c = Calendar.getInstance();
		int day_of_week = c.get(Calendar.DAY_OF_WEEK);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int current_minutes = ((hour * 60) + minute);
		String dayOfWeekStr = String.format("%02d", day_of_week);
		String currentMinutesStr = String.format("%04d", current_minutes);
		String numLinia = String.format("%02d", Rellotge.getNumLinia());
		String idheartbeat = currentMinutesStr + dayOfWeekStr + numLinia;
		return idheartbeat;
	}

}
