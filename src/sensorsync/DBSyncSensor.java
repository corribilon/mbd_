package sensorsync;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class DBSyncSensor extends MysqlManager {

	public DBSyncSensor(Connection mm) {
		super(mm);
	}

	private static final Logger LOGGER = Tracer.getLogger(DBSyncSensor.class);

	public HashMap<String,ArrayList<String>> getDatabaseRes(){
		String sql = "SELECT r.*,d.`Nombre`, d.`Apellidos`, d.`foto`  FROM `01_tbl_rellotgenew` as r, `datos` as d WHERE d.`Id Matrícula`=r.iduser";
		HashMap<String,ArrayList<String>> toRet = new HashMap<String,ArrayList<String>>();
		try {
			if (TestConnection.isUp()) {
				Statement statement = connect.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				rs.first();
				ArrayList<String> row;
				
				do{
					row = new ArrayList<String>();
					
					String iduser = rs.getInt("iduser")+"";
					row.add(iduser);
					String labelid = rs.getInt("labelid")+"";
					row.add(labelid);					

					String characterics = rs.getString("characterics");
					row.add(characterics);
					String hashStr = rs.getString("hash");
					row.add(hashStr);
					String nombre = rs.getString("Nombre");
					row.add(nombre);
					String apellidos = rs.getString("Apellidos");
					row.add(apellidos);
					String foto = rs.getString("foto");
					row.add(foto);	
						
					toRet.put(iduser, row);
				}while(rs.next());
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error querying the database "+e.toString(), e);			
		}
		return toRet;
	}
	
	
}
