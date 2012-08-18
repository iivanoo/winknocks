import javax.crypto.spec.*;

import java.security.*;

import javax.crypto.*;

import java.util.*;
import java.io.*;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class Utility {
	
	private static String key = "91-71-249-197-111-83-184-64";
	private static String generalKey = "91-71-249-197-111-83-184-64";

	/**
	 * @param file the string to write into the file
	 * @param fileName the location of the file
	 */
	public static void writeFile(String file, String fileName) {
		try {
			file = file.trim();
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			writer.println(file);
			writer.close();
		} catch (IOException e) {
			//e.printStackTrace();
			return;
		}
	}
	
	/**
	 * @param command
	 * @throws Exception
	 */
	public static String executeCommand(String[] command) throws Exception {
		String result = "";
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        return result;
	}
	
	public static String generatePayload(KnockSequence sequence, String urgentScript, int random) {
		SecureRandom r = new SecureRandom();
		String result = "<id>" + sequence.getId() + "-" + random + "</id>";
		result += "<time>" + System.currentTimeMillis() + r.nextInt(random) +  "</time>";
		result += "<command>" + urgentScript + "</command>";
		result += Utility.generateRandomString(sequence.getMaxFakePayload());
		return result;
	}
	
	public static String generateRandomString(int length) {
		Random random = new Random(System.currentTimeMillis());
		byte[] payloadBytes = new byte[random.nextInt(length)];
		random.nextBytes(payloadBytes);
		char[] c = new char[payloadBytes.length];
		for(int i=0; i<c.length; i++) {
			if(payloadBytes[i] < 0) {
				payloadBytes[i] = (byte) (payloadBytes[i] * (-1));
			}
			if(payloadBytes[i] < 33) {
				payloadBytes[i] = (byte) (payloadBytes[i] + 33);
			}
			c[i] = (char) payloadBytes[i];
		}
		return String.valueOf(c);
	}
	
	public static byte[] encrypt(String data) {
		try {
			Key k = Utility.getKey(key);
			// Create the cipher
			Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			// Initialize the cipher for encryption
			desCipher.init(Cipher.ENCRYPT_MODE, k);
			// Our cleartext as bytes
			byte[] cleartext = data.getBytes();
			// Encrypt the cleartext
			byte[] ciphertext = desCipher.doFinal(cleartext);
			// Return a String representation of the cipher text
			return ciphertext;
		} catch(Exception e) {
			//e.printStackTrace();
		}
	    return null;
		//return data.getBytes();
	}
	
	public static String generateKey(String s) {
		try {
			Key k = Utility.getKey(Utility.generalKey);
			// Create the cipher
			Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			// Initialize the cipher for encryption
			desCipher.init(Cipher.ENCRYPT_MODE, k);
			// Our cleartext as bytes
			byte[] cleartext = s.getBytes();
			// Encrypt the cleartext
			byte[] ciphertext = desCipher.doFinal(cleartext);
			// Return a String representation of the cipher text
			return Utility.getString(ciphertext);
		} catch(Exception e) {
			//e.printStackTrace();
		}
	    return null;
	}
	
	public static String getString( byte[] bytes ) {
	    StringBuffer sb = new StringBuffer();
	    for( int i=0; i<bytes.length; i++ ) {
	        byte b = bytes[ i ];
	        sb.append( ( int )( 0x00FF & b ) );
	        if( i+1 <bytes.length ) {
	        	sb.append( "-" );
	        }
	    }
	    return sb.toString();
	}
	
	private static byte[] getBytes( String str ) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StringTokenizer st = new StringTokenizer( str, "-", false );
		while(st.hasMoreTokens()) {
			int i = Integer.parseInt( st.nextToken() );
			bos.write( ( byte )i );
		}
		return bos.toByteArray();
	}
	
	private static Key getKey(String key) {
	    try {
	    	byte[] bytes = getBytes(key);
	    	DESKeySpec pass = new DESKeySpec(bytes);
	    	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
	    	SecretKey s = skf.generateSecret(pass);
	    	return s;
	    } catch( Exception e ) {
	        //e.printStackTrace();
	    }
	    return null;
	}

	/**
	 * @return Returns the key.
	 */
	public static String getKey() {
		return key;
	}

	/**
	 * @param key The key to set.
	 */
	public static void setKey(String key) {
		Utility.key = key;
	}
	
	public static String getFinalBytes(String payload) {
		String result = "";
		String[] separated = payload.split("-");
		for(int i=0; i<separated.length; i++) {
			String temp = separated[i];
			if(separated[i].length() == 1) {
				temp = "00" + separated[i];
			} else {
				if(separated[i].length() == 2) {
					temp = "0" + separated[i];
				}
			}
			result += temp;
		}
		return result;
	}
}
