import java.io.*;
import java.security.Key;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import net.sourceforge.jpcap.capture.PacketCapture;

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
	
	public static String decrypt(byte[] bytes) {
		try {
	    	String source = Utility.getString(bytes);
	    	// Get our secret key
	    	Key k = Utility.getKey(key);
	    	// Create the cipher
	    	Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    	// Encrypt the cleartext
	    	byte[] ciphertext = getBytes( source );
	    	// Initialize the same cipher for decryption
	    	desCipher.init(Cipher.DECRYPT_MODE, k);
	    	// Decrypt the ciphertext
	    	byte[] cleartext = desCipher.doFinal(ciphertext);
	    	// Return the clear text
	    	return new String( cleartext );
	    } catch( Exception e ) {
	    	//e.printStackTrace();
	    }
	    return null;
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
	
	static byte[] getBytes( String str ) {
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
	
	public static String getFinalString(String payload) {
		String result = "";
		if(payload.length() % 3 != 0) {
			return "";
		}
		if(payload.length() != 0) {
			result += payload.charAt(0);
		}
		for(int i=1; i<payload.length(); i++) {
			if(i % 3 == 0) {
				result += "-";
			}
			result += payload.charAt(i);		
		}
		return result;
	}
	
	public static int getCaptorIndex(String device) {
		try {
			String[] devs = PacketCapture.lookupDevices();
			for(int i=0; i<devs.length; i++) {
				if(devs[i].contains(device)) {
					return i;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static BitSet toBitSet(String number) {
		BitSet result = new BitSet(32);
		for(int i=0; i<32; i++) {
			result.set(i, false);
			result.set(i, false);
			if(number.charAt(i) == '1') {
				result.set(i);
			}
			if(number.charAt(i) == '1') {
				result.set(i);
			}
		}
		return result;
	}
	
	public static String logicalAnd(String a, String b) {
		String result = "";
		for(int i=0; i<a.length(); i++) {
			if(a.charAt(i) == '1' && b.charAt(i) == '1') {
				result += "1";
			} else {
				result += "0";
			}
		}
		return result;
	}
}
