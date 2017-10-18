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
		HashMap<String,ArrayList<String>> toRet = null;
		try {
			if (TestConnection.isUp()) {
				Statement statement = connect.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				boolean isValidRow = rs.first();
				ArrayList<String> row;
				if(isValidRow) {
					toRet = new HashMap<String,ArrayList<String>>();
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
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);		
			toRet = null;
		}
		return toRet;
	}
	
	
	
	public ArrayList<String> getAbsentisme(){
		String sql = "Select concepto FROM `01_tbl_absentismo_concepte_new` WHERE 1";
		ArrayList<String> toRet = null;
		try {
			if (TestConnection.isUp()) {
				Statement statement = connect.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				boolean isValidRow = rs.first();
				if(isValidRow) {
					toRet = new ArrayList<String>();
					do{						
						String concepto = rs.getString("concepto")+"";
						toRet.add(concepto);						
					}while(rs.next());
					
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);		
			toRet = null;
		}
		return toRet;
	}
	
	
}
