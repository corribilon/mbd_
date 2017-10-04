package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import common.Tracer;

public class KeyLog implements NativeKeyListener {
	
	
	private static final Logger LOGGER = Tracer.getLogger(KeyLog.class );
	
	private static String buffer = new String();
	public static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
	
	public Validator validator;
	
	public KeyLog(Validator v) {
		validator = v;
	}
	
	
	
    public void nativeKeyPressed(NativeKeyEvent e) {
    	if(e.getKeyCode() == NativeKeyEvent.VC_ENTER || e.getKeyCode() == NativeKeyEvent.VC_KP_ENTER){
    		sendBuffer(buffer);
    		buffer = "";   
    	}
    }

    private void sendBuffer(String buffer2) {
		boolean isValid = validator.isValid(buffer2);
		
		if (isValid) {
			String d = sdf.format(new Date());
    		buffer2 = (buffer2+"/"+d);    		
			BufferManager.movementsFunction(BufferManager.PUT, buffer2);
			LOGGER.log(Level.INFO, "READ: " + buffer2);
		} else {
			LOGGER.log(Level.SEVERE, "NOT VALID READ: " + buffer2+" - Ignored...");
		}
	}

	public void nativeKeyReleased(NativeKeyEvent e) {
		//Do nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    	
    	buffer = buffer + e.getKeyChar();
    	
    	
    }

}