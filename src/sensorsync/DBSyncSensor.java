package sensorsync;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class DBSyncSensor extends MysqlManager {
	

	private static final Logger LOGGER = Tracer.getLogger(DBSyncSensor.class);

	




	public HashMap<String,ArrayList<Object>> getDatabaseRes(){
		String sql = "SELECT r.*,d.`Nombre`, d.`Apellidos`, d.`foto`  FROM `01_tbl_rellotgenew` as r, `datos` as d WHERE d.`Id Matrícula`=r.iduser";
		HashMap<String,ArrayList<Object>> toRet = new HashMap<String,ArrayList<Object>>();
		try {
			if (TestConnection.isUp()) {
				statement = connect.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				rs.first();
				ArrayList<Object> row;
				String hash = "";

				
				do{
					row = new ArrayList<Object>();
					hash = rs.getString("hash");
					
					row.add(rs.getInt("iduser"));
					row.add(rs.getInt("labelid"));
					for (int i = 0; i < 6; i++) {
						row.add(rs.getString(i+2));
					}	
					toRet.put(hash, row);
				}while(rs.next());
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error querying the database "+e.toString(), e);			
		}
		return toRet;
	}
	
	
}
