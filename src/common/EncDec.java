package common;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

public class EncDec {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Tracer.getLogger(EncDec.class);

	public static void main(String[] args) {
		test("Hello?¿?");		
	}


	private static void test(String stringToEnc) {
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
