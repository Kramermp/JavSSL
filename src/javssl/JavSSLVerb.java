package javssl;

public enum JavSSLVerb {
	S_CLIENT,
	UNKNOWN;

	public static JavSSLVerb  getEnumOf(String arg) {

		switch(arg.toUpperCase()) {
			case "S_CLIENT":
				return S_CLIENT;
			default:
				return UNKNOWN;
		}
	}
}
