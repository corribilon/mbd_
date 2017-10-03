package rellotge;

import java.net.SocketException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import common.KeyLog;
import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class DBRellotge extends MysqlManager{

	private static final Logger LOGGER = Tracer.getLogger(DBRellotge.class);

	private HashMap<String, Long> last5mins;
	
		

	
	public void updateStatus(String[] res) throws ParseException {
		int resI = 0;

		String time = "";
		String labelId = "";
		String[] arr = null;
		for (int i = 0; i < res.length; i++) {
			arr = getArray(res, i);
			if (arr != null) {
				labelId = arr[0];
				time = arr[1];
				boolean isOk = isEntryOk(labelId, time);

				if (isOk) {
					String sql = "INSERT INTO rellotge (userid,id,time,entradasortida)" + "VALUES (-1," + labelId
							+ ", '" + time + "', " + Rellotge.prop.getProperty("entrada") + ")";
					try {
						if (TestConnection.isUp()) {
							statement = connect.createStatement();
							resI = statement.executeUpdate(sql);
							System.out.println("UPDATED ELEMENT:\t\t " + res[i] + "\t\t" + resI);
							if(resI==0){
								Rellotge.movementsFunction(Rellotge.PUT, res[i]);
							}
						} else {
							System.out.println("No Connection to DDBB. Sending result to the buffer.");
							Rellotge.movementsFunction(Rellotge.PUT, res[i]);
						}
					} catch (CommunicationsException e) {
						LOGGER.log(Level.INFO, "Communications Exception. Sending result to the buffer: "+e.toString(), e);
						Rellotge.movementsFunction(Rellotge.PUT, res[i]);
						System.out.println("Resetting connection to DDBB.");
						Rellotge.resetConnection();
					} catch (SQLException e) {
						LOGGER.log(Level.INFO,"LabelId not registered. Ignoring read. "+e.toString(), e);						
					} catch (SocketException e) {
						LOGGER.log(Level.WARNING,"Error with the socket. "+ e.toString(), e);						
						Rellotge.movementsFunction(Rellotge.PUT, res[i]);
					}

				} else {
					System.out.println(
							"El identificador " + labelId + " ya ha sido leido durante los ultimos 5 minutos.");
				}

			} else {
				System.out.println("IGNORING ELEMENT: \t\t " + res[i]);
			}
		}
	}

	private boolean isEntryOk(String labelId, String time) throws ParseException {

		if (last5mins == null) {
			last5mins = new HashMap<String, Long>();
		}

		// We update the status of the map removing all elements that are older
		// than 5 minutes
		ArrayList<String> keys = new ArrayList<String>(last5mins.keySet());
		long timeMillis = KeyLog.sdf.parse(time).getTime();
		long minutes5ago = timeMillis - (10 * 60 * 1000);
		// System.out.println("NOW: "+timeMillis);
		// System.out.println("minutes5ago: "+minutes5ago);
		for (String key : keys) {
			long h = last5mins.get(key);
			// System.out.println("KEY: "+key + " TIME: "+h);
			if (h <= minutes5ago) {
				// System.out.println("ELEMENTO CADUCADO h="+h+"
				// minutes5ago="+minutes5ago+" ");
				// El elemento esta caducado.
				last5mins.remove(key);
			}
		}
		// The map is already updated!

		boolean toRet = !last5mins.containsKey(labelId);

		last5mins.put(labelId, timeMillis);

		// System.out.println("SE ACTUALIZA EL MAP CON LA NUEVA CLAVE");
		// System.out.println(last5mins.get(labelId));

		return toRet;
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
		//
		// if(numGlyph == 4){
		// toRet = res[i].split("-");
		// }else if(numSlash == 4){
		// toRet = res[i].split("/");
		// }
		//

		return toRet;
	}

	public void sendHeartBeat() {

		String idheartbeat = getIdHeartbeat();

		String numLiniaStr = String.format("%02d", Rellotge.getNumLinia());

		String sql = "INSERT INTO 02_tbl_heartbeat (`idheartbeat`, `idlinia`) VALUES(" + idheartbeat + ", "
				+ numLiniaStr + ") ON DUPLICATE KEY UPDATE idlinia=" + numLiniaStr + ", `time`=CURRENT_TIMESTAMP;";

		try {
			if (TestConnection.isUp()) {
				statement = connect.createStatement();
				statement.executeUpdate(sql);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error sending heartbeat "+e.toString(), e);			
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
