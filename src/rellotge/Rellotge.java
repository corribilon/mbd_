package rellotge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
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
import org.json.simple.JSONObject;

import common.BufferManager;
import common.EncDec;
import common.KeyLog;
import common.MysqlManager;
import common.TestConnection;
import common.Tracer;
import common.Validator;
import common.Watchdog;

public class Rellotge implements Validator{
	
	private static final int NUM_MINUTES_NEXT_READ = 10;
	private static final Logger LOGGER = Tracer.getLogger(Rellotge.class);
	static Properties prop;
	
	static String mysqluser = "";	
	static String mysqlpassword = "";
	static String mysqldbname = "";
	static String mysqlip=""; 
	
	private static int num_linia;	
	
	public final static int NUM_LINIA = 1;
	
	private HashMap<String, Long> last5mins;
	
	public static Rellotge instance;
	
	public static int idOperationCounter = 0;

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
        instance = r;
        r.sendToPort("blank");
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
		Connection mm = null;
		try {
			if (TestConnection.isUp()) {
				mm=MysqlManager.readDataBase(mysqluser, mysqlpassword, mysqldbname, mysqlip);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "There was a problem connecting to database. " + e.toString(), e);	
		}
		DBRellotge dbRellotge = null;	
		if(mm!=null) {
			dbRellotge = new DBRellotge(mm);	
		}

		return dbRellotge;
	}
	

	
	
	public static int getNumLinia(){return num_linia;}
	
	private static void listen() {
		
		Watchdog.imAlive("main");
		Watchdog.imAlive("consumer");
		Watchdog.imAlive("heartbeat");
		Watchdog.imAlive("fingerprint");
		
		TimerTask task = new TimerHeartbeat();
    	Timer timer = new Timer();
    	timer.schedule(task, 1000,60000);
		
    	TimerTask consumerTask = new ThreadConsumer();
    	Timer timerCons = new Timer();
    	timerCons.schedule(consumerTask, 1000,10000);
    	
    	final Thread fingerprint = new Thread(new SearchSensor());
    	fingerprint.start();
    	
    	TimerTask thFg = new TimerTask() {			
			@Override
			public void run() {
				Watchdog.imAlive("fingerprint");
				boolean isAlive = fingerprint.isAlive();
				if(!isAlive) {
					LOGGER.log(Level.WARNING, "The SensorSynch Thread has ended... Restarting");
					fingerprint.start();
				}				
			}
		};
		Timer tFg = new Timer();
		tFg.schedule(thFg, 1000, 5000);
		
		
		
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
		long minutes5ago = timeMillis - (NUM_MINUTES_NEXT_READ * 60 * 1000);
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
			LOGGER.log(Level.INFO, "The input "+o.toString() +" is not an string!");
			return false;
		}
		//Must be parseable into a long variable
		try {
			Long.parseLong((String)o);
		} catch (NumberFormatException e) {
			//Is not a number;
			LOGGER.log(Level.INFO, "The input "+o.toString() +" is not a representable number!");
			return false;
		}
		
		//If its a String and its parseable to a long then it can be send to the port to 
		//be shown on the presentation layer
		sendToPort((String)o);
		
		//It must be not entered in the last 5 minutes		
		try {
			boolean entryOk = isEntryOk((String)o, System.currentTimeMillis());
			if(!entryOk) {
				LOGGER.log(Level.INFO, "The input "+o.toString() +" was entered less than "+NUM_MINUTES_NEXT_READ+" minutes ago!");
			}
			return entryOk;
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, "Error trying to check when was the last time the "+(String)o +" entry.", e);
			return false;
		}
	}


	@SuppressWarnings("unchecked")
	private synchronized void sendToPort(String o) {
		JSONObject obj = new JSONObject();
		if(o.equals("blank")) {
			idOperationCounter = (idOperationCounter+1)%1986;
			String idOp = idOperationCounter+"";
			String op = "blank";			
			obj.put("idOperation",idOp);
			obj.put("operation",op);			
		}else {
			ArrayList<String> l = SearchSensor.getData(o);
			String labelId = o;
			String nom = l.get(4);
			String cognoms = l.get(5);
			String foto = l.get(6);
			String op ="";
			if(Integer.parseInt(Rellotge.prop.getProperty("entrada"))==0) {
				op = "entry";
			}else {
				op = "out";
			}
			idOperationCounter = (idOperationCounter+1)%1986;
			String idOp = idOperationCounter+"";			
			
			obj.put("idOperation",idOp);
			obj.put("operation",op);
			obj.put("nom",nom);
			obj.put("cognoms",cognoms);
			obj.put("foto", "photos/www/bdncapac/upload/"+foto);
			obj.put("idUser", labelId);	        
	        
		}
		File f = new File("portRellotge.json");
        PrintWriter p=null;
		try {
			p = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not write on the port file: "+e.toString(), e);

		}
        p.write(obj.toJSONString());
        p.close();
	}
	
}
