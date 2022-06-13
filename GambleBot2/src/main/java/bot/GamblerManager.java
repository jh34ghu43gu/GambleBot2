package bot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;
import model.Crate;

/**
 * Object to hold various objects (ie list of crates) instead of loading separately in various commands.
 *
 */
public class GamblerManager {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(GamblerManager.class);
	private ArrayList<Crate> crates;
	private HashMap<Integer, Double> prices;
	private HashMap<Integer, String> currencies;
	
	public GamblerManager() {
		crates = Crate.getCrates();
		crates.addAll(Crate.getCases());
		prices = new HashMap<Integer, Double>();
		currencies = new HashMap<Integer, String>();
		
		//Get price map
		JsonObject priceList = Utils.getLocalPriceList().get("items").getAsJsonObject();
		for(Crate crate : crates) {
			double value = 0.0;
			String currency = "";
			if(priceList.has(crate.getName())) {
				try {
					String craft = "Craftable";
					if(crate.getNumber() == 105) {
						craft = "Non-Craftable";
					}
					JsonObject crateObj = priceList.get(crate.getName()).getAsJsonObject()
							.get("prices").getAsJsonObject()
							.get("6").getAsJsonObject() //Unique quality = 6
							.get("Tradable").getAsJsonObject()
							.get(craft).getAsJsonObject()
							.get(Integer.toString(crate.getNumber())).getAsJsonObject();
					if(crateObj.has("value_high")) {
						value = crateObj.get("value").getAsDouble() + crateObj.get("value_high").getAsDouble();
						value = Math.floor((value/2)*100.0)/100.0;
					} else {
						value = crateObj.get("value").getAsDouble();
					}
					
					currency = crateObj.get("currency").getAsString();
					if(currency.equalsIgnoreCase("metal")) { 
						currency = "ref"; 
					}
				} catch(NullPointerException e) {
					log.error("NullPointer for crate/case: " + crate.getName() + " " + crate.getNumber());
					e.printStackTrace();
					continue;
				}
			} else {
				log.warn("NO PRICE FOUND FOR: " + crate.getName() + " " + crate.getNumber() + ", SKIPPING.");
				continue;
			}
			prices.put(crate.getNumber(), value);
			currencies.put(crate.getNumber(), currency);
		}
		
	}

	/**
	 * @return list of all crate objects
	 */
	public ArrayList<Crate> getAllCrates() {
		return crates;
	}
	
	/**
	 * @param cases True for only cases, false for only crates
	 * @return list of Crate objects either crates or cases only
	 */
	public ArrayList<Crate> getCratesOrCases(boolean cases) {
		ArrayList<Crate> c = new ArrayList<Crate>();
		for(Crate crate : crates) {
			if(crate.isCase() == cases) {
				c.add(crate);
			}
		}
		return c;
	}
	
	/**
	 * @param cases True for only cases, false for only crates
	 * @return list of crate/case names as strings
	 */
	public ArrayList<String> getNames(boolean cases) {
		ArrayList<String> c = new ArrayList<String>();
		for(Crate crate : crates) {
			if(crate.isCase() == cases) {
				c.add(crate.getNames());
			}
		}
		return c;
	}
	
	/**
	 * Similar to getNames but appends a price from the pricelist
	 * @param cases True for only cases, false for only crates
	 * @return list of crate/case names as strings
	 */
	public ArrayList<String> getNamesAndPrices(boolean cases) {
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		ArrayList<String> c = new ArrayList<String>();
		for(Crate crate : crates) {
			if(crate.isCase() == cases) {
				c.add(crate.getNames() + " (" + twoDec.format(prices.get(crate.getNumber())) + " " + currencies.get(crate.getNumber()) + ")");
			}
		}
		
		return c;
	}
}
