package serverWITHclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class test {
	public static void main(String[] args) throws UnknownHostException {
		
		InetAddress localhost = InetAddress.getLocalHost(); 
		System.out.println ("localhost: "+localhost.getHostAddress()); 
		System.out.println ("localhost: "+localhost.getHostName());

	}
}
