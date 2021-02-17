package javssl.sclient;

import javssl.util.ArrayHelper;
import javssl.util.BadArgumentsException;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class S_Client {
	String[] args = null;
	String target = "";
	URL targetURL = null;
	HttpsURLConnection con = null;

	SSLSocket socket;
	X509Certificate[] chain;

	Certificate[] certs = null;

	boolean showCertsFlag = false;
	boolean saveCertFlag = false;
	private boolean fileFlag = false;
	private String fileLocation = "";
	private String trustStoreLocation = "";
	private char[] storePass = "changeit".toCharArray();


	public S_Client(String[] args) throws Exception {
		System.out.println("Building S_Client");
		this.args = args;
		processArgs();
	}

	public void run() {
		System.out.println("Running S_Client");
		System.out.println("Target: " + target);

		socketConnection();
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

		int trustStoreIndex = ArrayHelper.indexOfIgnoreCase("-truststore", args);
		if( trustStoreIndex!= -1 && trustStoreIndex != args.length) {
			trustStoreLocation = args[trustStoreIndex + 1];
		}

		int storePassIndex = ArrayHelper.indexOfIgnoreCase("-storePass", args);
		if( storePassIndex!= -1 && storePassIndex != args.length) {
			storePass = args[storePassIndex + 1].toCharArray();
		}
	}

	private void saveCert(X509Certificate xCert) {
		System.out.println("Saving cert to " + fileLocation);
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

	private void socketConnection() {
		SSLContext context = null;
		try {

			File file = new File(trustStoreLocation);
			if (file.isFile() == false) {
				if(trustStoreLocation!=null) {
					System.out.println("Failed to find trust store at location :" + trustStoreLocation);
				}
				file = new File("jssecacerts");
				if (file.isFile() == false) {
					char SEP = File.separatorChar;
					File dir = new File(System.getProperty("java.home") + SEP
							+ "lib" + SEP + "security");
					file = new File(dir, "jssecacerts");
					if (file.isFile() == false) {
						file = new File(dir, "cacerts");
					}
				}
			}

			System.out.println("Loading KeyStore " + file + "...");
			InputStream in = new FileInputStream(file);
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(in, storePass);
			in.close();

			context = SSLContext.getInstance("TLS");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
			SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
			context.init(null, new TrustManager[] {tm}, null);
			SSLSocketFactory factory = context.getSocketFactory();

			System.out.println("Opening connection to " + targetURL.getHost() + ":" + 443 + "...");
			socket = (SSLSocket)factory.createSocket(targetURL.getHost(), 443);
			socket.setSoTimeout(10000);
			try {
				System.out.println("Starting SSL handshake...");
				socket.startHandshake();
				socket.close();
				System.out.println();
				System.out.println("No errors, certificate is already trusted");
			} catch (SSLException e) {
				System.out.println("SSL Exception");
				e.printStackTrace(System.out);
				System.out.println("Exception During Handshake");
			}

			chain = tm.chain;
			if (chain == null) {
				System.out.println("Could not obtain server certificate chain");
				return;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println();
			System.out.println("Server sent " + chain.length + " certificate(s):");
			System.out.println();
			if(showCertsFlag) {
				MessageDigest sha1 = MessageDigest.getInstance("SHA1");
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				for (int i = 0; i < chain.length; i++) {
					X509Certificate cert = chain[i];
					System.out.println
							(" " + (i + 1) + " Subject " + cert.getSubjectDN());
					System.out.println("   Issuer  " + cert.getIssuerDN());
					sha1.update(cert.getEncoded());
					System.out.println("   sha1    " + toHexString(sha1.digest()));
					md5.update(cert.getEncoded());
					System.out.println("   md5     " + toHexString(md5.digest()));
					System.out.println();
				}
			}

			if(saveCertFlag) {
				saveCert(chain[0]);
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println("Socket Exception");
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception");
			e.printStackTrace();
		} catch (CertificateException e) {
			System.out.println("Cert Exception");
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}

	}

	private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

	private static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes) {
			b &= 0xff;
			sb.append(HEXDIGITS[b >> 4]);
			sb.append(HEXDIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}

	private static class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}
}

