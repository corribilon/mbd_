package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * How to be used:
 * 
 * <pre>
 *  - On KeyLog press the enter
 *  	 - validate the entry
 *  	 - save on the buffer.
 *  	 - update the buffer file.
 *  - On consume the buffer
 *       - return all the elements from the buffer.
 *       - remove all elements from the file.
 *   - On load buffer file
 *       - for each entry of the file:
 *       	- validate
 *       	- put into the buffer
 *       	- do not remove any entry from the file
 * 
 * </pre>
 * 
 * @author amd
 *
 */
public class BufferManager {

	private static final Logger LOGGER = Tracer.getLogger(KeyLog.class);
	private static ArrayList<String> movements;

	public final static int PUT = 1;

	public final static int RETRIEVEALL = 2;

	/**
	 * Read the file and bulk the content into the movements array.
	 * 
	 * @throws FileNotFoundException
	 *             if the buffer.bcs file is not found on the same directory
	 */
	public static void loadBuffer() throws FileNotFoundException {		
		File f = new File("buffer.bcs");
		if (!f.exists()) {
			return;
		}

		Scanner s = new Scanner(f);

		ArrayList<String> lines = new ArrayList<String>();

		while (s.hasNextLine()) {
			lines.add(s.nextLine());
		}

		s.close();
		
		//Clear the buffer file
		updateBuffer();

		for (String line : lines) {
			movementsFunction(PUT, line);
		}

	}

	/**
	 * PUT and RETRIEVE elements from the movements array
	 * @param order BufferManager.PUT or BufferManager.RETRIEVEALL
	 * @param params in case of PUT function is the content to be put on the buffer
	 * @return null or an String[] as the retrieved data from the buffer.
	 */
	public static synchronized Object movementsFunction(int order, Object params) {
		Object toRet = null;
		if (order == PUT) {
			getMov().add((String) params);
			updateBuffer();
		}
		if (order == RETRIEVEALL) {
			int s = getMov().size();
			if (s <= 0) {
				updateBuffer();
			} else {
				String[] toRetArr = new String[s];
				for (int i = 0; i < s; i++)
					toRetArr[i] = getMov().get(i);
				getMov().clear();
				updateBuffer();
				toRet = toRetArr;
			}
		}

		return toRet;
	}

	/**
	 * Get the movements array
	 * @return the movements array
	 */
	private static ArrayList<String> getMov() {
		if (movements == null) {
			movements = new ArrayList<String>();
		}
		return movements;
	}

	/**
	 * Writes into the buffer file the content of movements array.
	 */
	public static void updateBuffer() {
		try {
			saveBuffer((String[]) getMov().toArray());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not save into the buffer", e);
		}
	}

	/**
	 * Save into the buffer the content given as a param
	 * @param res the content to be written
	 * @throws IOException if the buffer couldn't be written
	 */
	private static void saveBuffer(String[] res) throws IOException {
		File f = new File("buffer.bcs");
		if (!f.exists()) {
			f.createNewFile();
		}
		PrintWriter p = new PrintWriter(f);

		p.print("");
		if (res != null) {
			for (int i = 0; i < res.length; i++) {
				p.println(res[i]);
			}
		}
		p.close();
	}

}
