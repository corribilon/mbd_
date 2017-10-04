package barcodescanner;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Tracer;
import common.Watchdog;

public class TimerHeartbeat extends TimerTask{
	
	private static final Logger LOGGER = Tracer.getLogger(TimerHeartbeat.class);
	
	@Override
	public void run() {
		Watchdog.imAlive("heartbeat");
		DBBarcodescanner mm = BarcodeScanner.getMM();
		if(mm!=null){
			try{
				mm.sendHeartBeat();
			}catch(Exception e){
				LOGGER.log(Level.SEVERE, "Error while trying to send the heartbeat! "+e.toString(), e);
			}
		}
	}

}
