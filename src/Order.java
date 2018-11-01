
// simple wrapper calls for complex orders
public class Order {
	
	// name of the node creating the order
	public String name;
	// buy or sell order
	public Integer buyOrSell;
	// amount to change
	public Integer amount;
	// price 
	public Long price;
	// administrasion variable if the order is matched
	public boolean matched;
	
	public Order(String _name, Integer _buyOrSell, Integer _amount, Long _price) {
		name = _name;
		buyOrSell = _buyOrSell;
		amount = _amount;
		price = _price;
		matched = false;
	}
	
}
