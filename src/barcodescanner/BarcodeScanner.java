package barcodescanner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import java.util.Properties;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import common.BufferManager;
import common.EncDec;
import common.KeyLog;
import common.MysqlManager;
import common.TestConnection;
import common.Tracer;
import common.Validator;
import common.Watchdog;


public class BarcodeScanner implements Validator{
	
	private static final Logger LOGGER = Tracer.getLogger(BarcodeScanner.class);
	static Properties prop;
	
	static String mysqluser = "";	
	static String mysqlpassword = "";
	static String mysqldbname = "";
	static String mysqlip=""; 
	
	private static int num_linia;
	
	static DBBarcodescanner mm = null;
	
	public final static int PUT = 1;
	public final static int RETRIEVEALL = 2;
	
	public final static int NUM_LINIA = 1;

	public static void main(String[] args) throws Exception {
		
		Tracer.setup();
		
		BufferManager.loadBuffer();
		// Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
            
        }
        catch (NativeHookException ex) {        	
        	LOGGER.log(Level.SEVERE, "There was a problem registering the native hook. "+ex.toString(), ex);
            System.exit(1);
        }
        
        GlobalScreen.addNativeKeyListener(new KeyLog(new BarcodeScanner()));
		
		prop = getProperties();
		
		setUpProperties();
		listen();
		
	}


	static void setUpProperties() {
		mysqluser =  EncDec.dec(prop.getProperty("mysqluser"));
		mysqlpassword =  EncDec.dec(prop.getProperty("mysqlpassword"));
		mysqldbname = EncDec.dec(prop.getProperty("mysqldbname"));
		mysqlip = EncDec.dec(prop.getProperty("mysqlip"));
		
		num_linia = Integer.parseInt(prop.getProperty("num_linia"));
	}
	
	
	public static DBBarcodescanner getMM(){
		Connection mm = null;
		try {
			if (TestConnection.isUp()) {
				mm=MysqlManager.readDataBase(mysqluser, mysqlpassword, mysqldbname, mysqlip);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "There was a problem connecting to database. " + e.toString(), e);	
		}
		DBBarcodescanner dbRellotge = null;	
		if(mm!=null) {
			dbRellotge = new DBBarcodescanner(mm);	
		}

		return dbRellotge;
	}
	

	
	public static int getNumLinia(){return num_linia;}
	
	private static void listen() {
		
		TimerTask task = new TimerHeartbeat();
    	Timer timer = new Timer();
    	timer.schedule(task, 1000,60000);
		
    	TimerTask consumerTask = new ThreadConsumer();
    	Timer timerCons = new Timer();
    	timerCons.schedule(consumerTask, 1000,10000);
		
		System.out.println("LISTENING...");
		while(true){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e.toString(), e);
			}
			Watchdog.imAlive("main");
		}
		
	}
	
	
	public static Properties getProperties() {
		
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.barcodescanner.properties");

			prop.load(input);

		} catch (IOException io) {
			LOGGER.log(Level.WARNING, io.toString(), io);
		} 
		
		return prop;
	  }


	public boolean isValid(Object o) {
		// Must be an String
		if (!(o instanceof String)) {
			return false;
		}
		// Must be parseable into a long variable
		String num = (String)o;
		int l = num.length();
		boolean isValid = l==23;
		for (int i = 0; i < l; i++) {
			try {
				Integer.parseInt(num.charAt(i) + "");
			} catch (NumberFormatException e) {
				isValid = false;
			}
		}
		return isValid;
	}

}
