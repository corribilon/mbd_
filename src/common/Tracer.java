package common;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Tracer{

	private static final Logger LOGGER = getLogger(Tracer.class);
	private static FileHandler handler;

	public static Logger getLogger(@SuppressWarnings("rawtypes") Class c) {
		Logger l = Logger.getLogger(c.getName());
		
		File f = new File("logs");
		if(!f.exists()) {
			f.mkdir();
		}else if(!f.isDirectory()) {
			f.mkdir();
		}
			
		
		if (handler == null) {
			try {
				handler = new FileHandler("logs/log.log", 4 * 1024 * 1024, 10, true);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			handler.setFormatter(new SimpleFormatter());
		}
		l.addHandler(handler);
		return l;
	}


	public static void setup() throws IOException {
		//This registers the exception 
        //handler for every thread
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        
	}
	

	
	
	//simple UncaughtExceptionHandler which logs
	static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
		// Get a logger. You can use LogConfigurator/Manager
		// This logger will conatin the stacktrace of all
		// the uncaught exceptions in the world.
		Logger log = LOGGER;
		// Implement your own way of logging here
		public void uncaughtException(final Thread t, final Throwable e) {
			log.log(Level.SEVERE, e.toString(), e);
		}
	}
	



}
