package rellotge;

import java.text.ParseException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.BufferManager;
import common.Tracer;
import common.Watchdog;

public class ThreadConsumer extends TimerTask{
	
	
	private static final Logger LOGGER = Tracer.getLogger(ThreadConsumer.class);

	@Override
	public void run() {
		Watchdog.imAlive("consumer");		
		
				
		String[] res = null;	
		
		DBRellotge mm = Rellotge.getMM();
		res = (String[])BufferManager.movementsFunction(BufferManager.RETRIEVEALL, null);
		
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
					BufferManager.movementsFunction(BufferManager.PUT, res[i]);
				}
			}
			
		}		
	}	

}
