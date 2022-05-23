package bot;

import java.util.ArrayList;

import model.Crate;

/**
 * Object to hold various objects (ie list of crates) instead of loading separately in various commands.
 *
 */
public class GamblerManager {
	
	private ArrayList<Crate> crates;
	
	public GamblerManager() {
		crates = Crate.getCrates();
		crates.addAll(Crate.getCases());
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
}
