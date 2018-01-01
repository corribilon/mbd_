package rellotge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import common.BufferManager;
import common.KeyLog;
import common.Tracer;

public class SearchSensor implements Runnable {

	private static final String LOCAL_DB_LDB = "../SensorSync/localDB.ldb";
	// private static final String LOCAL_DB_LDB = "localDB.ldb";
	private static final Logger LOGGER = Tracer.getLogger(SearchSensor.class);
	private static HashMap<String, ArrayList<String>> mapa = null;
	private static long lastUpd = -1;

	public void run() {

		while (true) {

			try {
				if (getHour() != 00) {
					String hash = null;
					try {
						hash = readFinger();
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.toString(), e);
					} catch (InterruptedException e) {
						LOGGER.log(Level.SEVERE, e.toString(), e);
					}

					if (hash != null) {
						ArrayList<String> content = null;
						try {
							content = readMapaAndFilterByHash(hash);
							System.out.println("CONTENT: " + content);
						} catch (FileNotFoundException e) {
							LOGGER.log(Level.SEVERE, e.toString(), e);
						}
						if (content != null) {
							String buffer2 = content.get(1);
							boolean isValid = Rellotge.instance.isValid(buffer2);
							if (isValid) {
								String d = KeyLog.sdf.format(new Date());
								buffer2 = (buffer2 + "/" + d);
								BufferManager.movementsFunction(BufferManager.PUT, buffer2);
								LOGGER.log(Level.INFO, "READ FROM FINGERPRINT: " + buffer2);
							} else {
								LOGGER.log(Level.SEVERE,
										"NOT VALID READ FROM FINGERPRINT: " + buffer2 + " - Ignored...");
							}
						} else {
							LOGGER.log(Level.WARNING, "CONTENT = NULL");
						}
					} else {
						LOGGER.log(Level.WARNING, "NOT HASH FOUND ON LOCAL DB!");
						Rellotge.instance.sendToPort("failed");
					}
				} else {
					try {
						Thread.sleep(10000);
					} catch (Exception e) {
						LOGGER.log(Level.WARNING, "Could not wait on the sync time. " + e.toString(), e);
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "An unhandled exception occured on the Sensor Read thread. " + e.toString(),
						e);
			}
		}

	}

	public static ArrayList<String> getData(String labelId) {
		return getMapa().get(labelId);
	}

	public static synchronized HashMap<String, ArrayList<String>> getMapa() {
		if (lastUpd == -1 || lastUpd < System.currentTimeMillis() - (20 * 60 * 1000)) {
			try {
				readMapaAndFilterByHash("-1");
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
			}
		}
		return mapa;
	}

	private static synchronized ArrayList<String> readMapaAndFilterByHash(String hash) throws FileNotFoundException {
		HashMap<String, ArrayList<String>> copy = null;
		if (mapa != null) {
			copy = new HashMap<String, ArrayList<String>>(mapa);
		}
		ArrayList<String> toRet = null;
		try {
			mapa = new HashMap<String, ArrayList<String>>();
			File f = new File(LOCAL_DB_LDB);
			Scanner s = new Scanner(f);
			String file = "";

			while (s.hasNextLine()) {
				file = file + s.nextLine();
			}

			JSONArray a = new JSONArray(file);

			for (Object object : a) {
				JSONObject o = (JSONObject) object;
				String labelid = o.getString("labelid");
				ArrayList<String> strArr = new ArrayList<String>();
				strArr.add(o.getString("iduser"));
				strArr.add(o.getString("labelid"));
				strArr.add(o.getString("characterics"));
				strArr.add(o.getString("hash"));
				strArr.add(o.getString("nom"));
				strArr.add(o.getString("cognoms"));
				strArr.add(o.getString("foto"));
				mapa.put(labelid, strArr);

				if (hash.trim().equals(o.getString("hash").trim())) {
					toRet = strArr;
				}

			}

			s.close();
			lastUpd = System.currentTimeMillis();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Rollback to the state before. Error wile loading the local DB." + e.toString(),
					e);
			// Restore the state before (Tha last that was working)
			mapa = copy;
			if (mapa != null) {
				ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(mapa.values());
				// Iterate over all the values to find the hash
				for (ArrayList<String> arrayList : list) {
					if (arrayList.get(3).equals(hash)) {
						toRet = arrayList;
					}
				}
			}
		}
		return toRet;
	}

	private String readFinger() throws IOException, InterruptedException {
		String hash = null;

		Process p = Runtime.getRuntime().exec("python2 ./search.py");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		p.waitFor(); // Blocking!

		String s = "";

		while ((s = stdInput.readLine()) != null) {
			if (s.contains("Hash:")) {
				hash = s.replace("Hash: ", "");
				hash = hash.trim();
				System.out.println("Found with hash: " + hash);
			}
			if (s.contains("No match found!")) {
				System.out.println("Didn't find the template!");
			}
		}
		String err = "";
		boolean hasErr = false;
		while ((s = stdError.readLine()) != null) {
			hasErr = true;
			err = err + s + "\n";
		}
		if (hasErr) {
			LOGGER.log(Level.SEVERE, "Error executing the python SEARCH script:\n" + err);
		}
		return hash;
	}

	public static void main(String[] argsv) {

	}

	public static int getHour() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		String s = sdf.format(new Date());
		return Integer.parseInt(s);
	}

}
