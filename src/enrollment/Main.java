package enrollment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


import common.EncDec;
import common.MysqlManager;
import common.TestConnection;
import common.Tracer;

public class Main {

	private static final Logger LOGGER = Tracer.getLogger(Main.class);
	static Properties prop;

	static String mysqluser = "";
	static String mysqlpassword = "";
	static String mysqldbname = "";
	static String mysqlip = "";

	static DBEnrollment mm = null;

	private static String characterics;
	private static String hash;
	private static boolean end;
	private static String idMat;
	private static boolean error;
	private static String command;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException,
			InterruptedException {
		

		Tracer.setup();

		System.out.println("*******************************************");
		System.out.println("*                                         *");
		System.out.println("*  Enregistrament de l'empremta dactilar  *");
		System.out.println("*                                         *");
		System.out.println("*******************************************");
		System.out.println("");
		

		prop = new Properties();
		prop.load(new FileInputStream(new File("config.enrollment.properties")));
		setUpProperties();
		Scanner sc = null;
		sc = new Scanner(System.in);

		while (true) {
			System.out.println("Instruccions d'ús:");
			System.out.println("------------------");
			System.out
					.println(""
							+ "+------------------------------------------------------------+\n"
							+ "| Un cop el lector d'empremtes dongui llum verda             |\n"
							+ "| posar el dit index en la superficie del lector.            |\n"
							+ "|                                                            |\n"
							+ "| Quan la llum verda s'apagui, treure el dit i tornar-lo     |\n"
							+ "| a posar quan el lector dongui llum verda un altre cop.     |\n"
							+ "+------------------------------------------------------------+\n");

			end = false;
			error = false;

			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			// read the output from the command
			String s = null;
			while (!end) {
				while ((s = stdInput.readLine()) != null) {
					processInput(s);
				}
				while ((s = stdError.readLine()) != null) {
					processError(s);
				}
			}
			p.waitFor();
			if (!error) {
				idMat = "";
				boolean validated = false;

				while (!validated) {
					System.out
							.println("A continuació teclegi el Id. Matrícula de l'empremta dactilar:");
					idMat = sc.nextLine();
					try {
						Integer.parseInt(idMat);
						validated = true;
					} catch (Exception e) {
						System.out
								.println("El Id. Matrícula ha de ser un número enter.");
					}
					if (validated) {
						String res = getUserInfo();
						if (res == null) {
							System.out
									.println("El usuari no existeix a la BBDD.");
							validated = false;
						} else {
							System.out.println("");
							System.out
									.println("La identitat de l'empremta dactilar: ");
							System.out.println("Id Matrícula\t\t-" + idMat);
							System.out.println("Nom i cognoms\t\t-" + res);
							System.out.println("Es correcte? (s/n):");
							validated = sc.nextLine().equalsIgnoreCase("S");
						}
						System.out.println("");
						if (!validated)
							System.out.println("-- Identitat incorrecte.");
					}
				}
				System.out.println("-- Identitat confirmada.");
				insertInDB();

			}
			Thread.sleep(3000);
			System.out.println("");
			System.out.println("*******************************************");
			System.out.println("");
		}
	}

	private static String getUserInfo() {
		System.out.println("-- Obtenint info del usuari");
		DBEnrollment mm = getMM();
		String res = mm.getUserInfo(idMat);
		return res;
	}

	private static void insertInDB() {
		System.out.println("-- Passant l'empremta a la Base de Dades...");
		DBEnrollment mm = getMM();

		mm.updateDigitalCharacterics(idMat, characterics, hash);

		System.out
				.println("S'ha enregistrat correctament l'empremta dactilar.");

	}

	private static void processError(String s) {
		System.out.println("Error en el proces:");
		System.out.println(s);
		error = true;
		end = true;
	}

	private static void processInput(String s) {
		if (s.equalsIgnoreCase("Waiting Finger")) {
			// System.out
			// .println("Posi el dit índex en el lector d'empremtes dactilars");
		} else if (s.equalsIgnoreCase("Put Finger again")) {
			// System.out.println("Posi el dit índex un altre vegada");
		} else if (s.contains("Characterics")) {
			System.out.println("L'empremta s'ha enregistrat correctament.");
			characterics = s.replace("Characterics:", "").trim();
		} else if (s.contains("Hash")) {
			hash = s.replace("Hash:", "").trim();
		} else if (s.equalsIgnoreCase("End Enrollment")) {
			end = true;
		} else if (s.equalsIgnoreCase("Remove Finger")) {
			// System.out.println("Tregui el dit.");
		} else if (s.equalsIgnoreCase("Template deleted")) {
			// System.out.println("Tregui el dit.");
		} else {
			end = true;
			error = true;
			System.out.println("Error: " + s);
		}		
	}

	public static DBEnrollment getMM(){
		Connection mm = null;
		try {
			if (TestConnection.isUp()) {
				mm=MysqlManager.readDataBase(mysqluser, mysqlpassword, mysqldbname, mysqlip);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "There was a problem connecting to database. " + e.toString(), e);	
		}
		DBEnrollment dbRellotge = null;	
		if(mm!=null) {
			dbRellotge = new DBEnrollment(mm);	
		}
		return dbRellotge;
	}

	private static void setUpProperties() {
		mysqluser = EncDec.dec(prop.getProperty("mysqluser"));
		mysqlpassword = EncDec.dec(prop.getProperty("mysqlpassword"));
		mysqldbname = EncDec.dec(prop.getProperty("mysqldbname"));
		mysqlip = EncDec.dec(prop.getProperty("mysqlip"));
		command = prop.getProperty("enrollmentCommand");
	}

}
