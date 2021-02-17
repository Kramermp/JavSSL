package javssl.sclient;

import javssl.util.ArrayHelper;
import javssl.util.BadArgumentsException;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class S_Client {
	String[] args = null;
	String target = "";
	URL targetURL = null;
	HttpsURLConnection con = null;

	Certificate[] certs = null;

	boolean showCertsFlag = false;
	boolean saveCertFlag = false;
	private boolean fileFlag = false;
	private String fileLocation = "";


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
			certs = con.getServerCertificates();

			if(showCertsFlag) {
				printCerts();
			}

			if(saveCertFlag) {
				saveCert();
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

		if (ArrayHelper.indexOfIgnoreCase("-showcerts", args) != -1) {
			showCertsFlag = true;
		}

		if (ArrayHelper.indexOfIgnoreCase("-saveCert", args) != -1) {
			saveCertFlag = true;
			fileLocation = targetURL.getHost() + ".crt";
			int fileIndex = ArrayHelper.indexOfIgnoreCase("-file", args);
			if (fileIndex != -1 && fileIndex != args.length) {
				fileFlag = true;
				fileLocation = args[fileIndex + 1];
			}

		}
	}

	private void printCerts() {
		for(int i =0; i < certs.length; i++) {
			System.out.println(certs[i]);
		}
	}

	private void saveCert() {
		X509Certificate xCert = (X509Certificate) certs[0];
		try {
			BASE64Encoder encoder = new BASE64Encoder();
			PrintWriter out = new PrintWriter(new File(fileLocation));
			out.println(X509Factory.BEGIN_CERT);
			String encodeResults = encoder.encode(xCert.getEncoded());
			out.println(encodeResults);
			out.println(X509Factory.END_CERT);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
	}

}
