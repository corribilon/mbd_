package common;


import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;




public class TestConnection {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Tracer.getLogger(TestConnection.class);

	public static boolean isUp() throws SocketException {
		boolean isUp = false;
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
		  NetworkInterface interf = interfaces.nextElement();
		  if (interf.isUp() && !interf.isLoopback())
			  isUp = true;
		}
		return isUp;
	}
	

}
