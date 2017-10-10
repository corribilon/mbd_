package rellotge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.BufferManager;
import common.KeyLog;
import common.Tracer;

public class SearchSensor implements Runnable {

	private static final Logger LOGGER = Tracer.getLogger(SearchSensor.class);
	private static HashMap<String, ArrayList<String>> mapa = null;
	private static long lastUpd = -1;

	public void run() {

		while (true) {

			String hash = null;
			try {
				hash = readFinger();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
			}

			if (hash != null) {
				ArrayList<String> content = null;
				try {
					content = readMapaAndFilterByHash(hash);
				} catch (FileNotFoundException e) {
					LOGGER.log(Level.SEVERE, e.toString(), e);
				}
				if (content != null) {
					String buffer2 = content.get(1);
					boolean isValid = Rellotge.instance.isValid(buffer2);
					if (isValid) {
						String d = KeyLog.sdf.format(new Date());
						buffer2 = (buffer2 + "/" + d);
						BufferManager.movementsFunction(BufferManager.PUT, buffer2);
						LOGGER.log(Level.INFO, "READ FROM FINGERPRINT: " + buffer2);
					} else {
						LOGGER.log(Level.SEVERE, "NOT VALID READ FROM FINGERPRINT: " + buffer2 + " - Ignored...");
					}
				}
			}
		}
	}

	
	public static ArrayList<String> getData(String labelId){
		return getMapa().get(labelId);
	}
	
	public static synchronized HashMap<String, ArrayList<String>> getMapa(){
		if(lastUpd==-1 || lastUpd<System.currentTimeMillis()-(20*60*1000)) {
			try {
				readMapaAndFilterByHash("-1");
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
			}
		}
		return mapa;
	}
	
	private static synchronized ArrayList<String> readMapaAndFilterByHash(String hash) throws FileNotFoundException {
		HashMap<String, ArrayList<String>> copy = new HashMap<String, ArrayList<String>>(mapa);
		ArrayList<String> toRet = null;
		try {
			mapa = new HashMap<String, ArrayList<String>>();
			File f = new File("localDB.ldb");
			Scanner s = new Scanner(f);
			String line = "";
			
			while (s.hasNextLine()) {
				line = s.nextLine();
				String[] arr = line.split(";");
				String characterics = arr[0];
				line.replace(characterics, "$$$token_characterics$$$");
				arr = arr[2].split(",");
				ArrayList<String> content = new ArrayList<String>();
				for (int i = 0; i < arr.length; i++) {
					if (arr[i].equals("$$$token_characterics$$$")) {
						arr[i] = characterics;
					}
					content.add(arr[i]);
				}
				String labelid = arr[1];
				String hashA = arr[3];
				if (hash.equals(hashA)) {
					toRet = content;
				}
				mapa.put(labelid, content);
			}
			s.close();
			lastUpd = System.currentTimeMillis();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Rollback to the state before. Error wile loading the local DB."+e.toString(), e);
			//Restore the state before (Tha last that was working)
			mapa = copy;
			if(mapa!=null) {
				ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(mapa.values());
				//Iterate over all the values to find the hash
				for (ArrayList<String> arrayList : list) {
					if(arrayList.get(3).equals(hash)) {
						toRet = arrayList;
					}
				}
			}
		}
		return toRet;
	}

	private String readFinger() throws IOException, InterruptedException {
		String hash = null;

		Process p = Runtime.getRuntime().exec("python2 ./search.py");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		p.waitFor(); //Blocking!

		String s = "";

		while ((s = stdInput.readLine()) != null) {
			if (s.contains("Hash:")) {
				hash = s.replace("Hash: ", "");
				System.out.println("Found with hash: " + hash);
			}
			if (s.contains("No match found!")) {
				System.out.println("Didn't find the template!");
			}
		}
		String err = "";
		boolean hasErr = false;
		while ((s = stdError.readLine()) != null) {
			hasErr = true;
			err = err + s + "\n";
		}
		if (hasErr) {
			LOGGER.log(Level.SEVERE, "Error executing the python SEARCH script:\n" + err);
		}
		return hash;
	}

}
