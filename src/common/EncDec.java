package common;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

public class EncDec {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Tracer.getLogger(EncDec.class);

	public static void main(String[] args) {
		
		@SuppressWarnings("resource")
		Scanner s = new Scanner(System.in);
		
		while(true) {
		
			
			
			System.out.println("+---------------------------+");
			System.out.println("| CODIFICADOR/DECODIFICADOR |");
			System.out.println("+---------------------------+");
			
			System.out.println("");
			
			System.out.println("1) Codificar");
			System.out.println("2) Decodificar");
			
			System.out.println("Escriba un opción (1 o 2):");
			
			int opt = Integer.parseInt(s.nextLine());
			
			boolean isOk = true;
			
			if(opt!=1 && opt!=2) {
				System.out.println("Opcion incorrecta. Saliendo del programa!");
				isOk = false;
			}
			
			if(isOk) {
			
				System.out.println("Escribe el texto:");
				
				String str = s.nextLine();			
				
				String res = "";
				
				if(opt==1) {
					res = enc(str);
					System.out.println(enc(str));
				}else {
					res = dec(str);
					System.out.println(dec(str));
				}
				
				File f = new File("out.txt");
				PrintWriter pw;
				try {
					pw = new PrintWriter(f);
					pw.println(res);
					pw.close();
					System.out.println("");
					System.out.println(" --> Resultado se ha guardado en el fichero: out.txt");
				} catch (FileNotFoundException e) {
					System.out.println("No se pudo generar el fichero out.txt. Revise los permisos de la carpeta "
							+ "que contiene este ejecutable.");
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					
	//				e.printStackTrace();
				}
			
			}
			
		}
	}


	public static void test(String stringToEnc) {
		String enc = enc(stringToEnc);
		String dec = dec(enc);
		
		System.out.println("ENC:"+enc+"\nDEC:"+dec);
	}
	
	
	public static String enc(String s){
		return new String(Base64.encodeBase64(Base64.encodeBase64(Base64.encodeBase64(s.getBytes()))));
	}
	
	
	public static String dec(String s){
		return new String(Base64.decodeBase64(Base64.decodeBase64(Base64.decodeBase64(s.getBytes()))));
	}

}
