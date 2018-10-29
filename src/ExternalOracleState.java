
/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.FCDataInputStream;
import com.swirlds.platform.FCDataOutputStream;
import com.swirlds.platform.FastCopyable;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.swirlds.platform.Utilities;

/**
 * This holds the current state of the swirld. For this simple "hello swirld" code, each transaction is just
 * a string, and the state is just a list of the strings in all the transactions handled so far, in the
 * order that they were handled.
 */
public class ExternalOracleState implements SwirldState {

	// SHARED STATE
	//shared state is a map of identifier -> integers (random number) 
 	private Map<String, String> randoms = new HashMap<String, String>();
	
	/** names and addresses of all members */
	private AddressBook addressBook;

	/** @return all the state information received so far from the network */
	public synchronized Map<String, String> getState() {
		return randoms;
	}

	
	
	/** @return all the strings received so far from the network, concatenated into one */
	public synchronized String getReceived() {
		String result = "";
		for (Map.Entry<String, String> entry : randoms.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			result += key + " " + value.toString() + " ";
		}
		return result;
	}
	
	
	/** @return the same as getReceived, so it returns the entire shared state as a single string */
	public synchronized String toString() {
		String result = "";
		for (Map.Entry<String, String> entry : randoms.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			result += key + " " + value.toString() + " ";
		}
		return result;	
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public synchronized AddressBook getAddressBookCopy() {
		return addressBook.copy();
	}

	@Override
	public synchronized FastCopyable copy() {
		ExternalOracleState copy = new ExternalOracleState();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public synchronized void copyTo(FCDataOutputStream outStream) {
		try {
			List<String> stringArray1 = new ArrayList<String>();
			List<String> stringArray2 = new ArrayList<String>();
			
			for (Map.Entry<String, String> entry : randoms.entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				stringArray1.add(key);
				stringArray2.add(value);
			}

			Utilities.writeStringArray(outStream, 
					stringArray1.toArray(new String[0]));

			Utilities.writeStringArray(outStream, 
					stringArray2.toArray(new String[0]));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(FCDataInputStream inStream) {
		try {
			List<String> stringArray1 = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));

			List<String> stringArray2 = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));
			
			if (stringArray1.size() != stringArray2.size()) {
				throw new IOException("Size mismatch");	
			}
			
			for (int i = 0; i < stringArray1.size(); i ++) {
				String key = stringArray1.get(i);
				String value = stringArray2.get(i);
				randoms.put(key, value);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		randoms = new HashMap<String, String>(((ExternalOracleState)old).randoms);
		addressBook = ((ExternalOracleState) old).addressBook.copy();
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timestamp, byte[] transaction, Address address) {
		
		try {
			String transactionString = new String(transaction, StandardCharsets.UTF_8);
			String name = transactionString.substring(0, transactionString.indexOf("-")-1 );
			String value = transactionString.substring(transactionString.indexOf("-") + 2, transactionString.length());		
			randoms.put(name, value);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void noMoreTransactions() {
	}

	@Override
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.addressBook = addressBook;
	}
}