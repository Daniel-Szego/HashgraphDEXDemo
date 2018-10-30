
// simple wrapper calls for complex orders
public class Order {
	
	public Boolean buyOrSell;	
	public Integer amount;
	public Double price;
	
	public Order(Boolean _buyOrSell, Integer _amount, Double _price) {
		buyOrSell = _buyOrSell;
		amount = _amount;
		price = _price;
	}
	
}
