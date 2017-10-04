package barcodescanner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import common.EncDec;
import common.KeyLog;
import common.TestConnection;
import common.Tracer;
import common.Watchdog;

public class BarcodeScanner {
	
	private static final Logger LOGGER = Tracer.getLogger(BarcodeScanner.class);
	static Properties prop;
	
	static String mysqluser = "";	
	static String mysqlpassword = "";
	static String mysqldbname = "";
	static String mysqlip=""; 
	
	private static int num_linia;
	
	static DBBarcodescanner mm = null;
	
	
	private static ArrayList<String> movements;
	
	public final static int PUT = 1;
	public final static int RETRIEVEALL = 2;
	
	public final static int NUM_LINIA = 1;

	public static void main(String[] args) throws Exception {
		
		ThreadConsumer.loadBuffer();
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
        
        GlobalScreen.addNativeKeyListener(new KeyLog());
		
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
		if(mm==null){
			mm = new DBBarcodescanner();
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


	public static synchronized Object movementsFunction(int order, Object params){		
		if(order == PUT){
			getMov().add((String)params);
			return null;
		}
		if(order == RETRIEVEALL){
			int s = getMov().size();
			if(s<=0)return null;
			String[] toRet = new String[s];
			for (int i = 0; i < s; i++)	toRet[i] = getMov().get(i);
			getMov().clear();
			return toRet;			
		}		
		return null;		
	}
	
	
	private static ArrayList<String> getMov(){
		if(movements==null){
			movements = new ArrayList<String>();
		}
		return movements;
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

}
