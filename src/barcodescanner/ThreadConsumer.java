package barcodescanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
		DBBarcodescanner mm;		
		String[] res = null;		
		mm = BarcodeScanner.getMM();
		res = (String[])BarcodeScanner.movementsFunction(BarcodeScanner.RETRIEVEALL, null);
		try {
			saveBuffer(res);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.toString(), e);	
		}
		if(res!=null){
			if(mm!=null){
				mm.updateStatus(res);
			}else{
				for (int i = 0; i < res.length; i++) {
					BarcodeScanner.movementsFunction(BarcodeScanner.PUT, res[i]);
				}
			}			
		}		
	}
	
	
	public void saveBuffer(String[] res) throws IOException{
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
			BarcodeScanner.movementsFunction(BarcodeScanner.PUT, s.nextLine());
		}
		
		s.close();
	}
	

}
