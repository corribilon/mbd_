package sensorsync;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;



import common.EncDec;
import common.MysqlManager;
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
		HashMap<String,ArrayList<String>> res = mm.getDatabaseRes();
		try {
			bulkResIntoFile(res);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		
	}



	private void bulkResIntoFile(HashMap<String, ArrayList<String>> res) throws IOException {
		File f = new File("localDB.ldb");
		if(!f.exists()){
			f.createNewFile();
		}
		PrintWriter pw = new PrintWriter(f);
		ArrayList<String> it = new ArrayList<String>(res.keySet());
		String line = "";
		for (String key : it) {
			line = res.get(key).get(2);
			line = line + ";" + key + ";";
			line = line + res.get(key).toString();			
			pw.println(line);
		}
		pw.close();
	}
	
	
	public static void main(String[] argsv) throws IOException{
		
		Tracer.setup();
		
		prop = new Properties();
		prop.load(new FileInputStream(new File("config.sensorsync.properties")));
		setUpProperties();
		new Main().run();
	}

	
	public static DBSyncSensor getMM(){
		Connection mm = null;
		try {
			if (TestConnection.isUp()) {
				mm=MysqlManager.readDataBase(mysqluser, mysqlpassword, mysqldbname, mysqlip);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "There was a problem connecting to database. " + e.toString(), e);	
		}
		DBSyncSensor dbRellotge = null;	
		if(mm!=null) {
			dbRellotge = new DBSyncSensor(mm);	
		}

		return dbRellotge;
	}

	private static void setUpProperties() {
		mysqluser = EncDec.dec(prop.getProperty("mysqluser"));
		mysqlpassword = EncDec.dec(prop.getProperty("mysqlpassword"));
		mysqldbname = EncDec.dec(prop.getProperty("mysqldbname"));
		mysqlip = EncDec.dec(prop.getProperty("mysqlip"));
		pathToCommand = prop.getProperty("pathToCommand");		
	}
}
