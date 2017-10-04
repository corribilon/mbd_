package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;




public class Watchdog {
	
	private static final Logger LOGGER = Tracer.getLogger(Watchdog.class);
	
	public static void imAlive(String id){
		File f = new File("timers/timer_"+id);
		if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
				
			}
		}
		PrintWriter pw =null;
		try {
			pw = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		pw.print("0");
		pw.close();		
	}
	
	


}
