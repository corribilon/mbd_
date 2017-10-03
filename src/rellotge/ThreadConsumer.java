package rellotge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Tracer;
import common.Watchdog;

public class ThreadConsumer extends TimerTask{
	
	
	private static final Logger LOGGER = Tracer.getLogger(ThreadConsumer.class);

	@Override
	public void run() {
		Watchdog.imAlive("consumer");		
		DBRellotge mm;		
		String[] res = null;	
		
		mm = Rellotge.getMM();
		res = (String[])Rellotge.movementsFunction(Rellotge.RETRIEVEALL, null);
		
		if(res!=null){
			if(mm!=null){
				try {
					mm.updateStatus(res);
				} catch (ParseException e) {
					//Should never fall in this exception
		        	LOGGER.log(Level.WARNING, "Error parsing the date time. "+e.toString(), e);
				}
			}else{
				for (int i = 0; i < res.length; i++) {
					Rellotge.movementsFunction(Rellotge.PUT, res[i]);
				}
			}			
		}		
	}
	
	
	public static void saveBuffer(String[] res) throws IOException{
		File f = new File("buffer.bcs");
		if(!f.exists()){
			f.createNewFile();
		}
		PrintWriter p = new PrintWriter(f);
		
		p.print("");
		if(res!=null){
			for (int i = 0; i < res.length; i++) {
				p.println(res[i]);
			}
		}
		p.close();
	}
	
	
	public static void loadBuffer() throws FileNotFoundException{
		File f = new File("buffer.bcs");
		if(!f.exists()){
			return;
		}
		Scanner s = new Scanner(f);
		
		while(s.hasNextLine()){
			Rellotge.movementsFunction(Rellotge.PUT, s.nextLine());
		}
		
		s.close();
	}
	

}
