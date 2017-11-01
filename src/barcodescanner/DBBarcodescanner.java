package barcodescanner;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.BufferManager;
import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class DBBarcodescanner extends MysqlManager{
	
	public DBBarcodescanner(Connection mm) {
		super(mm);
	}

	private static final Logger LOGGER = Tracer.getLogger(DBBarcodescanner.class);

	public void updateStatus(String[] res) {
		int resI = 0;
		String idArticulo = "";
		String numunidades = "";
		String idpedido = "";
		String[] arr = null;
		String date = "";
		String barcode = "";
		for (int i = 0; i < res.length; i++) {
			arr = getArray(res, i);			
			if(arr!=null){
				barcode = res[i].split("/")[0];
				idArticulo = arr[0];
				numunidades = arr[4];
				idpedido = arr[3];
				date = arr[5];
				String sql = 	"INSERT INTO `02_tbl_salidas` (`idbarcode`, `idarticulo`, `numunidades`, `idpedido`, `fechayhora`) "+
								"SELECT * FROM (SELECT '"+barcode+"', "+idArticulo+", "+numunidades+", "+idpedido+", '"+date+"') AS tmp "+
									"WHERE NOT EXISTS ( "+
										"SELECT `idbarcode` FROM `02_tbl_salidas` WHERE `idbarcode` = '"+barcode+"'"+
									") LIMIT 1;";

				LOGGER.log(Level.INFO,"UPD: "+sql);
				try {
					if(TestConnection.isUp()){
						Statement statement = connect.createStatement();
						resI = statement.executeUpdate(sql);
						LOGGER.log(Level.INFO,"UPDATED ELEMENT:\t\t "+res[i] + "\t\t" +resI);
					}else{
						LOGGER.log(Level.INFO,"No Connection to DDBB. Sending result to the buffer.");
						BufferManager.movementsFunction(BarcodeScanner.PUT, res[i]);
					}
				} catch (Exception e) {
					LOGGER.log(Level.INFO, "Error while pushing barcode to DDBB. Sending result to the buffer: "+e.toString(), e);
					BufferManager.movementsFunction(BarcodeScanner.PUT, res[i]);
					
				}				
			}else{
				LOGGER.log(Level.WARNING,"IGNORING ELEMENT: \t\t "+res[i]);
			}		    
		}
	}

	

	private String[] getArray(String[] res, int i) {
		
		String[] splStr = res[i].split("/");
		
		String date = "";
		if(splStr.length<=1){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = sdf.format(new Date());
		}else{
			date = splStr[1];
		}
		
		String lineRead = splStr[0];
		
		if(lineRead.length()!=23){
			return null;
		}
		
		String[] toRet = new String[6];
		toRet[0] = lineRead.substring(0,5); //idArticulo
		toRet[1] = lineRead.substring(5,9); //numero de caja
		toRet[2] = lineRead.substring(9,14); //orden de trabajo
		toRet[3] = lineRead.substring(14,19); //id del pedido
		toRet[4] = lineRead.substring(19,23); // num elementos por caja
		toRet[5] = date; // fecha de la lectura
		
		return toRet;
	}

	public void sendHeartBeat() {
		
		
		String idheartbeat = getIdHeartbeat();
		
		String numLiniaStr = String.format("%02d", BarcodeScanner.getNumLinia());
		
		String sql = "INSERT INTO 02_tbl_heartbeat (`idheartbeat`, `idlinia`) VALUES("+idheartbeat+", "+numLiniaStr+") ON DUPLICATE KEY UPDATE idlinia="+numLiniaStr+", `time`=CURRENT_TIMESTAMP;";
		
		try{
			if(TestConnection.isUp()){
				Statement statement = connect.createStatement();
				statement.executeUpdate(sql);
			}
		}catch(Exception e){
			LOGGER.log(Level.WARNING, "Error sending heartbeat "+e.toString(), e);
		}						
	}

	public static String getIdHeartbeat() {
		Calendar c = Calendar.getInstance();
		

		int day_of_week = c.get(Calendar.DAY_OF_WEEK);
		int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int current_minutes = ((hour * 60) + minute);
        
        String dayOfWeekStr = String.format("%02d", day_of_week);
        String currentMinutesStr = String.format("%04d", current_minutes);
		String numLinia = String.format("%02d", BarcodeScanner.getNumLinia());
		
		String idheartbeat = currentMinutesStr+dayOfWeekStr+numLinia;
		return idheartbeat;
	}
	  
	  
	
	
}
