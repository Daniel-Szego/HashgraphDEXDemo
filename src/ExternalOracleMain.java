
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.swirlds.platform.Browser;
import com.swirlds.platform.Console;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldMain;
import com.swirlds.platform.SwirldState;
import java.math.BigInteger;
import java.net.*;
import java.io.*;


/**
 * This HelloSwirld creates a single transaction, consisting of the string "Hello Swirld", and then goes
 * into a busy loop (checking once a second) to see when the state gets the transaction. When it does, it
 * prints it, too.
 */
public class ExternalOracleMain implements SwirldMain {
	/** the platform running this app */
	public Platform platform;
	/** ID number for this member */
	public long selfId;
	/** a console window for text output */
	public Console console;
	/** sleep this many milliseconds after each sync */
	public final int sleepPeriod = 100;
	/** number of rounds after the calculation starts */
	/** it has to be replaced by the real consensus event */
	public final int calculatingStarts = 50;
	
		
	/**
	 * This is just for debugging: it allows the app to run in Eclipse. If the config.txt exists and lists a
	 * particular SwirldMain class as the one to run, then it can run in Eclipse (with the green triangle
	 * icon).
	 * 
	 * @param args
	 *            these are not used
	 */
	public static void main(String[] args) {
		Browser.main(args);
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public void preEvent() {
	}

	@Override
	public void init(Platform platform, long id) {
		this.platform = platform;
		this.selfId = id;
		//this.console = platform.createConsole(true); // create the window, make it visible
		this.console = platform.createConsole(true); // create the window, make it visible

		String[] pars = platform.getParameters();
		
		platform.setAbout("Decentralized External Oracle v. 0.1\n"); // set the browser's "about" box
		platform.setSleepAfterSync(sleepPeriod);

	}	

	protected void LogException(Exception e) {
		console.out.println(e.toString() + " " +  e.getMessage() + " ");
	}
	
	protected void LogMessage(String message) {
		console.out.println(message);
	}
	
	@Override
	public void run() {
		try {
			String myName = platform.getState().getAddressBookCopy()
					.getAddress(selfId).getSelfName();
	
			
			console.out.println("Decentralized External Oracle v.0.1");	
			console.out.println("My name is " + myName);
			
			// calling external data
			String externalData = ReadExternalData();
			
			console.out.println("External data is " + externalData);
			String transactionString = myName + " - " + externalData;	
			byte[] transaction = transactionString.getBytes(StandardCharsets.UTF_8);
			
			platform.createTransaction(transaction);
			String lastReceived = "";
			
			int rounds = 0;
			String stateString = "";
			while (true) {
				ExternalOracleState state = (ExternalOracleState) platform
						.getState();
				String received = state.getReceived();				
				rounds++;					
				
				// THIS MUST BE THE REACHING CONSENSUS EVENT
				// REPLACE IN REAL IMPLEMENTATIONS
				if (rounds >= calculatingStarts - 1) {
					stateString = received;
					break;
				}	
				
				if (!lastReceived.equals(received)) {
					lastReceived = received;
					console.out.println("Received: " + received); // print all received transactions
				}
				try {
					Thread.sleep(sleepPeriod);
				} catch (Exception e) {
					LogException(e);
				}
			}
			
			console.out.println("Calculating oracle for the external data"); 
			
			// DATA CALCULATION FOR EXTERNAL DATA
			// SIMPLE ALGORITHM -> GETTING MOST VOTED
			 Map<String, Integer> voted = new HashMap<String, Integer>();
			 String[] stringArray = stateString.split(" ");
			 for (int i = 0; i < stringArray.length; i++) {
				 String name = "";
				 String value = "";
				 if (i % 2 == 0) {
					 name = stringArray[i];
				 }
				 else {
					 value = stringArray[i];
					 
					 if (voted.containsKey(value)){
						int vote =  voted.get(value);
						voted.put(value, vote + 1);
					 }
					 else {
						voted.put(value, 1);					 
					 }
				 }
			 }
			 
			String maxkey = "";
			int maxVote = 0;
			for (Map.Entry<String, Integer> entry : voted.entrySet())
			{
				String key = entry.getKey();
				int vote = entry.getValue();
				
				if (maxVote < vote) {
					maxVote = vote;
					maxkey = key;
				}
			}
			
			console.out.println("Max voted result : " + maxkey); 
 
			
		} catch(Exception e){
			LogException(e);
		}
	}

	@Override
	public SwirldState newState() {
		return new ExternalOracleState();
	}
	
	public String ReadExternalData() {
		String result = "";
		
        // Make a URL to the web page
        URL url;
		try {
			url = new URL("http://stackoverflow.com/questions/6159118/using-java-to-pull-data-from-a-webpage");

	        // Get the input stream through URL Connection
	        URLConnection con = url.openConnection();
	        InputStream is =con.getInputStream();
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	
	        String line = null;
	
	        // simple test result
	        result = br.readLine();
	        if (result.length() > 5)
	        result = result.substring(0, 4);
	        // read each line and write to System.out
	        //while ((line = br.readLine()) != null) {
	        //    System.out.println(line);
	        //}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return result;
	}

	
}