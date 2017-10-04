package rellotge;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
import common.TestConnection;
import common.Tracer;
import common.Validator;
import common.Watchdog;

public class Rellotge implements Validator{
	
	private static final Logger LOGGER = Tracer.getLogger(Rellotge.class);
	static Properties prop;
	
	static String mysqluser = "";	
	static String mysqlpassword = "";
	static String mysqldbname = "";
	static String mysqlip=""; 
	
	private static int num_linia;
	
	static DBRellotge mm = null;
	
	public final static int NUM_LINIA = 1;
	
	private HashMap<String, Long> last5mins;

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
        
        Rellotge r = new Rellotge();
		GlobalScreen.addNativeKeyListener(new KeyLog(r));		
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
	
	
	public static DBRellotge getMM(){
		if(mm==null){
			mm = new DBRellotge();
			try {
				if(TestConnection.isUp()){
					if(mm.readDataBase(mysqluser, mysqlpassword, mysqldbname, mysqlip) == false){
						mm = null;
					}
				}else{
					mm = null;
				}
			} catch (Exception e) {
	        	LOGGER.log(Level.WARNING, "There was a problem connecting to database. "+e.toString(), e);
				mm = null;
			}
		}
		return mm;
	}
	
	public static void resetConnection() {
		getMM().close();
		mm = null;
		getMM();
	}
	
	
	public static int getNumLinia(){return num_linia;}
	
	private static void listen() {
		
		Watchdog.imAlive("main");
		Watchdog.imAlive("consumer");
		Watchdog.imAlive("heartbeat");
		
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
			input = new FileInputStream("config.rellotge.properties");
			prop.load(input);
		} catch (IOException io) {
			LOGGER.log(Level.WARNING, io.toString(), io);
		} 
		return prop;
	  }

	
	private boolean isEntryOk(String labelId, long time) throws ParseException {

		if (last5mins == null) {
			last5mins = new HashMap<String, Long>();
		}

		// We update the status of the map removing all elements that are older
		// than 5 minutes
		ArrayList<String> keys = new ArrayList<String>(last5mins.keySet());
		long timeMillis = time;
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


	public boolean isValid(Object o) {
		//Must be an String
		if(!(o instanceof String)) {
			return false;
		}
		//Must be parseable into a long variable
		try {
			Long.parseLong((String)o);
		}catch (NumberFormatException e) {
			//Is not a number;
			return false;
		}
		//It must be not entered in the last 5 minutes		
		try {
			return isEntryOk((String)o, System.currentTimeMillis());
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, "Error trying to check when was the last time the "+(String)o +" entry.", e);
			return false;
		}
	}
	
}
