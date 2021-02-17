package javssl;

import javssl.sclient.S_Client;

/**
 * EntryPoint
 */
public class JavSSL {

	public static void main(String[] args) throws Exception {
		JavSSLVerb action = JavSSLVerb.getEnumOf(args[0]);

		switch(action) {
			case S_CLIENT:
					new S_Client(removeVerb(args)).run();
				break;
			case UNKNOWN:
				//Do  Help
				break;
		}
	}

	public static String[] removeVerb(String[] args) {
		String[] newArray = new String[args.length - 1];
		for(int i = 1; i < args.length; i++) {
			newArray[i - 1 ] = args[i];
		}
		return newArray;
	}
}
