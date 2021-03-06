package barcodescanner;

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
		DBBarcodescanner mm = BarcodeScanner.getMM();
		String[] res = (String[])BufferManager.movementsFunction(BarcodeScanner.RETRIEVEALL, null);
		if(res!=null){
			if(mm!=null){
				try {
					mm.updateStatus(res);
				}catch (Exception e) {
					//Should never fall in this exception
		        	LOGGER.log(Level.WARNING, "Error parsing the date time. "+e.toString(), e);
				}
				
			}else{
				for (int i = 0; i < res.length; i++) {
					BufferManager.movementsFunction(BarcodeScanner.PUT, res[i]);
				}
			}			
		}		
	}
	
	

	

}
