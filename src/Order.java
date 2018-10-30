
// simple wrapper calls for complex orders
public class Order {
	
	public Integer buyOrSell;	
	public Integer amount;
	public Long price;
	
	public Order(Integer _buyOrSell, Integer _amount, Long _price) {
		buyOrSell = _buyOrSell;
		amount = _amount;
		price = _price;
	}
	
}
