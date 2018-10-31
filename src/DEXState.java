
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
public class DEXState implements SwirldState {

	// SHARED STATE
	//shared state is the order book
 	private Map<String, Order> orderBook = new HashMap<String, Order>();
	
	/** names and addresses of all members */
	private AddressBook addressBook;

	/** @return all the state information received so far from the network */
	public synchronized Map<String, Order> getState() {
		return orderBook;
	}

	
	
	/** @return all the strings received so far from the network, concatenated into one */
	public synchronized String getReceived() {
		String result = "";
		for (Map.Entry<String, Order> entry : orderBook.entrySet())
		{
			String name = entry.getKey();
			Order order = entry.getValue();
			Integer buyOrSell = order.buyOrSell;
			Integer amount = order.amount;
			Long price = order.price;
			result += name + " " + buyOrSell.toString() + 
					" " + amount.toString() + " " + price.toString() + " ";
		}
		return result;
	}
	
	
	/** @return the same as getReceived, so it returns the entire shared state as a single string */
	public synchronized String toString() {
		String result = "";
		for (Map.Entry<String, Order> entry : orderBook.entrySet())
		{
			String name = entry.getKey();
			Order order = entry.getValue();
			Integer buyOrSell = order.buyOrSell;
			Integer amount = order.amount;
			Long price = order.price;
			result += name + " " + buyOrSell.toString() + 
					" " + amount.toString() + " " + price.toString() + " ";
		}
		return result + " ";
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public synchronized AddressBook getAddressBookCopy() {
		return addressBook.copy();
	}

	@Override
	public synchronized FastCopyable copy() {
		DEXState copy = new DEXState();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public synchronized void copyTo(FCDataOutputStream outStream) {
		try {
			List<String> nameArray = new ArrayList<String>();		
			List<Integer> buyOrSellArray = new ArrayList<Integer>();
			List<Integer> amountArray = new ArrayList<Integer>();
			List<Long> priceArray = new ArrayList<Long>();
			
			for (Map.Entry<String, Order> entry : orderBook.entrySet())
			{
				String name = entry.getKey();
				Order order = entry.getValue();
				Integer buyOrSell = order.buyOrSell;
				Integer amount = order.amount;
				Long price = order.price;
				nameArray.add(name);
				buyOrSellArray.add(buyOrSell);
				amountArray.add(amount);
				priceArray.add(price);
			}

			Utilities.writeStringArray(outStream, 
					nameArray.toArray(new String[0]));
			
			int[] buyOrSellA = buyOrSellArray.stream().mapToInt(i -> i).toArray();
			
			Utilities.writeIntArray(outStream,
					buyOrSellA);

			int[] amountA = amountArray.stream().mapToInt(i -> i).toArray();
			
			Utilities.writeIntArray(outStream,
					amountA);

			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(FCDataInputStream inStream) {
		try {
			List<String> nameArray = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));
			
			int[] buyOrSellArray = Utilities.readIntArray(inStream);
			
			int[] amountArray = Utilities.readIntArray(inStream);

			long[] priceArray = Utilities.readLongArray(inStream);
						
			if ((nameArray.size() != buyOrSellArray.length) ||
					(buyOrSellArray.length != amountArray.length) ||
					(amountArray.length != priceArray.length)) {
				throw new IOException("Size mismatch");	
			}
			
			for (int i = 0; i < nameArray.size(); i ++) {
				String name = nameArray.get(i);
				int buyOrSell = buyOrSellArray[i];
				int amount = amountArray[i];
				long price = priceArray[i];
				Order addedOrder = new Order(
						new Integer(buyOrSell),
						new Integer(amount),
						new Long(price));
				
				orderBook.put(name, addedOrder);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		orderBook = new HashMap<String, Order>(((DEXState)old).orderBook);
		addressBook = ((DEXState) old).addressBook.copy();
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timestamp, byte[] transaction, Address address) {
		
		try {
			String transactionString = new String(transaction, StandardCharsets.UTF_8);
			String[] transactionArray = transactionString.split(" ");
			String name = transactionArray[0];
			String buyOrSellString = transactionArray[1];
			String amountString = transactionArray[2];
			String priceString = transactionArray[3];
			
			Integer buyOrSell;
			if (buyOrSellString.equals("yes")) {
				buyOrSell = 1;
			}
			else {
				buyOrSell = 0;				
			}
			
			Integer amount = new Integer(amountString);
			Long price = new Long(priceString);
			Order newOrder = new Order(buyOrSell, amount, price);	
			orderBook.put(name, newOrder);	
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