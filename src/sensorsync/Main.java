package sensorsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.EncDec;
import common.TestConnection;
import common.Tracer;





public class Main {

	private static final Logger LOGGER = Tracer.getLogger(Main.class);

	static Properties prop;

	static String mysqluser = "";
	static String mysqlpassword = "";
	static String mysqldbname = "";
	static String mysqlip = "";
	static String pathToCommand = "";

	static DBSyncSensor mm = null;
	
	public void run() {
		DBSyncSensor mm = getMM();
		HashMap<String,ArrayList<Object>> res = mm.getDatabaseRes();
		try {
			bulkResIntoFile(res);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		try {
			updateSensorFlash(res);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"Error updating flash memory: "+e.toString(),e);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE,"Error updating flash memory: "+e.toString(),e);
		}
	}

	private void updateSensorFlash(HashMap<String, ArrayList<Object>> res) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(pathToCommand+"/deleteAll.py");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		p.waitFor();
		
		p = Runtime.getRuntime().exec(pathToCommand+"/upload_characterics.py");
		stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		p.waitFor();
	}

	private void bulkResIntoFile(HashMap<String, ArrayList<Object>> res) throws IOException {
		File f = new File("localDB.ldb");
		if(!f.exists()){
			f.createNewFile();
		}
		PrintWriter pw = new PrintWriter(f);
		ArrayList<String> it = new ArrayList<String>(res.keySet());
		String line = "";
		for (String key : it) {
			line = key+";";
			line = line + it.toString();
			pw.println(line);
		}
		pw.close();
	}
	
	
	public static void main(String[] argsv) throws IOException{
		
		Tracer.setup();
		
		prop = new Properties();
		prop.load(new FileInputStream(new File("config.sensorsync.properties")));
		setUpProperties();
	}

	
	public static DBSyncSensor getMM() {
		if (mm == null) {
			mm = new DBSyncSensor();
			try {
				if (TestConnection.isUp()) {
					if (mm.readDataBase(mysqluser, mysqlpassword, mysqldbname,
							mysqlip) == false) {
						mm = null;
					}
				} else {
					mm = null;
				}
			} catch (Exception e) {
				LOGGER.log(
						Level.WARNING,
						"There was a problem connecting to database. "
								+ e.toString(), e);
				mm = null;
			}
		}
		return mm;
	}

	private static void setUpProperties() {
		mysqluser = EncDec.dec(prop.getProperty("mysqluser"));
		mysqlpassword = EncDec.dec(prop.getProperty("mysqlpassword"));
		mysqldbname = EncDec.dec(prop.getProperty("mysqldbname"));
		mysqlip = EncDec.dec(prop.getProperty("mysqlip"));
		pathToCommand = prop.getProperty("pathToCommand");		
	}
}
