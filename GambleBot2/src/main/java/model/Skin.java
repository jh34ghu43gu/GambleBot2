package model;

import java.util.Comparator;

import files.ConfigHelper;

public class Skin implements Comparator<Skin>{
	
	private String name;
	private String tier;
	private boolean strange;
	private boolean unusual;
	private String effect;
	private int[] wears;
	
	public Skin() {
		tier = "";
		strange = false;
		unusual = false;
		this.effect = "";
		this.wears = new int[] {0, 0, 0, 0, 0};
	}
	
	public Skin(String name, String tier, boolean strange, boolean unusual, String effect) {
		super();
		this.name = name;
		this.tier = tier;
		this.strange = strange;
		this.unusual = unusual;
		this.effect = effect;
		this.wears = new int[] {0, 0, 0, 0, 0};
	}



	public Skin combine(Skin skin) {
		for(int i = 0; i < wears.length; i++) {
			this.wears[i] += skin.getWears()[i];
		}
		skin.clearWears();
		return skin;
	}

	public boolean canCombine(Skin skin) {
		if(!this.name.equals(skin.getName())) {
			return false;
		}
		if(this.strange != skin.isStrange()) {
			return false;
		}
		if(this.unusual == skin.isUnusual()) {
			if(!this.effect.equals(skin.getEffect())) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	
	public String toDiscordString() {
		String out = "";
		if(strange) {
			out += " Strange"; 
		}
		if(!effect.isEmpty()) {
			out += " " + effect;
		}
		if(!out.isEmpty()) { //Maybe remove this for skins
			out += " Unique";
		}
		
		//Name
		out += " " + name;
		
		//Wear
		String wearStr = "("; //so we can trim before adding
		String[] wearStrArr = {"BS", "WW", "FT", "MW", "FN"};
		for(int i = 0; i < wears.length; i++) {
			if(wears[i] > 0) {
				wearStr += wears[i] + " " + wearStrArr[i] + ", ";
			}
		}
		wearStr = wearStr.substring(0, wearStr.length()-2) + ")";
		out += wearStr;
		
		//Tier orb
		if(Crate.rarityEmotes.containsKey(tier)) {
			out += " " + Crate.rarityEmotes.get(tier);
		}
		
		//Strange orb
		if(strange) {
			out += " " + ConfigHelper.getOptionFromFile("STRANGE_EMOTE");
		}
		return out;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	/**
	 * @return the tier
	 */
	public String getTier() {
		return tier;
	}

	/**
	 * @param tier the tier to set
	 */
	public void setTier(String tier) {
		this.tier = tier;
	}

	/**
	 * @return the strange
	 */
	public boolean isStrange() {
		return strange;
	}

	/**
	 * @param strange the strange to set
	 */
	public void setStrange(boolean strange) {
		this.strange = strange;
	}

	/**
	 * @return the unusual
	 */
	public boolean isUnusual() {
		return unusual;
	}

	/**
	 * @param unusual the unusual to set
	 */
	public void setUnusual(boolean unusual) {
		this.unusual = unusual;
	}

	/**
	 * @return the effect
	 */
	public String getEffect() {
		return effect;
	}

	/**
	 * @param effect the effect to set
	 */
	public void setEffect(String effect) {
		this.effect = effect;
	}

	/**
	 * @return the wears
	 */
	public int[] getWears() {
		return wears;
	}
	
	public void clearWears() {
		wears = new int[] {0, 0, 0, 0, 0};
	}
	
	public int totalWears() {
		int out = 0;
		for(int i : wears) {
			out += i;
		}
		return out;
	}

	public void addWear(String wear) {
		if (wear.equalsIgnoreCase("Battle Scarred")) {
			wears[0]++;
		} else if (wear.equalsIgnoreCase("Well Worn")) {
			wears[1]++;
		} else if (wear.equalsIgnoreCase("Field-Tested")) {
			wears[2]++;
		} else if (wear.equalsIgnoreCase("Minimal Wear")) {
			wears[3]++;
		} else if (wear.equalsIgnoreCase("Factory New")) {
			wears[4]++;
		}
	}
	
	public void addWear(String wear, int amt) {
		if (wear.equalsIgnoreCase("Battle Scarred")) {
			wears[0] += amt;
		} else if (wear.equalsIgnoreCase("Well Worn")) {
			wears[1] += amt;
		} else if (wear.equalsIgnoreCase("Field-Tested")) {
			wears[2] += amt;
		} else if (wear.equalsIgnoreCase("Minimal Wear")) {
			wears[3] += amt;
		} else if (wear.equalsIgnoreCase("Factory New")) {
			wears[4] += amt;
		}
	}

	@Override
	public int compare(Skin skin1, Skin skin2) {
		if(skin1.getTier().equals(skin2.getTier())) {
			if(skin1.isUnusual() == skin2.isUnusual()) {
				if(skin1.isStrange() == skin2.isStrange()) {
					return 0;
				} else {
					return skin1.isStrange() ? -1 : 1;
				}
			} else {
				return skin1.isUnusual() ? -1 : 1;
			}
		} else {
			String[] tiers = {"Civilian", "Freelance", "Mercenary", "Commando", "Assassin", "Elite"};
			int tier1 = -1;
			int tier2 = -1;
			for(int i = 0; i < tiers.length; i++) {
				if(skin1.getTier().equals(tiers[i])) {
					tier1 = i;
				}
				if(skin2.getTier().equals(tiers[i])) {
					tier2 = i;
				}
			}
			return tier1 > tier2 ? -1 : 1;
		}
	}
	
}
