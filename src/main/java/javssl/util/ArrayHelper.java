package javssl.util;

public class ArrayHelper {

	public static int indexOfIgnoreCase(String tarString, String[] searchArray) {

		for(int i = 0; i < searchArray.length; i++){
			if(searchArray[i].equalsIgnoreCase(tarString)){
				return i;
			}
		}
		return -1;
	}
}
