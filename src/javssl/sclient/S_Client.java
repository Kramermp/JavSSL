package javssl.sclient;

import javssl.util.ArrayHelper;
import javssl.util.BadArgumentsException;
import sun.security.validator.Validator;
import sun.security.validator.ValidatorException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class S_Client {
	String[] args = null;
	String target = "https://self-signed.badssl.com/";
	URL targetURL = null;
	HttpsURLConnection con = null;
	boolean showCertsFlag = false;


	public S_Client(String[] args) throws Exception {
		System.out.println("Building S_Client");
		this.args = args;
		processArgs();
	}

	public void run() {
		System.out.println("Running S_Client");
		System.out.println("Target: " + target);

		try {
			con = (HttpsURLConnection) targetURL.openConnection();
			con.getInputStream();

			if(showCertsFlag) {
				printCerts();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Connected Successfully");
	}

	private void processArgs() throws BadArgumentsException, MalformedURLException {
		int connectIndex = ArrayHelper.indexOfIgnoreCase("-connect", args);

		//If not found or is last element
		if (connectIndex == -1 || connectIndex == args.length) {
			//TODO: Do something better
			throw new BadArgumentsException();
		}

		//Probably better logic to be had here
		target = args[1];
		targetURL = new URL(target);

		 if(ArrayHelper.indexOfIgnoreCase("-showcerts", args) != -1) {
			showCertsFlag = true;
		 }
	}

	private void printCerts() {
		Certificate[] certs = null;
		try {
			certs = con.getServerCertificates();
			for(int i =0; i < certs.length; i++) {
				System.out.println(certs.toString());
			}

		} catch (SSLPeerUnverifiedException e) {
			//Not Sure what Triggers this
			e.printStackTrace();
		}

	}

}
