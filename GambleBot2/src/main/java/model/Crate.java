package model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import beans.Item;
import beans.Player;
import ch.qos.logback.classic.Logger;

public class Crate {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Crate.class);
	public static final String HAT_FILE = "tf2hats.json";
	public static final String CRATE_FILE = "tf2crates.json";
	public static final String CASE_FILE = "tf2cases.json";
	public static double unboxPrice = 2.49;
	
	//Chances are 0 - 1
	private static double unusualChance = 0.0069;
	//Killstreak kit odds
	private static double specializedChance = 0.15; //TODO get non-guessed values
	private static double professionalChance = 0.05;
	//Cases
	private static double strangeChance = 0.1;
	private static double tierRollingChance = 0.2; //20% chance to roll the higher tier in cases
	//Extras
	private static double smallBonus = 0.15;
	private static double mediumBonus = 0.03; //Guess for non-unusual bonus items
	private static double largeBonus = unusualChance; //Guess for unusualifier
	//Warpaints/Skins
	private static double battleWorn = 0.1;
	private static double wellWorn = 0.3;
	private static double fieldTested = 0.7;
	private static double minimalWear = 0.9;
	//FactoryNew will be > minimalWear
	
	//Rarities
	private static String[] rarities = {"Civilian", "Freelance", "Mercenary", "Commando", "Assassin", "Elite"};
	public static HashMap<String, String> rarityEmotes = new HashMap<String, String>() {{
		put("Civilian", "<:Civilian:908844101036814357>");
		put("Freelance", "<:Freelance:908844101527564331>");
		put("Mercenary", "<:Mercenary:908844102454497290>");
		put("Commando", "<:Commando:908844101514985522>");
		put("Assassin", "<:Assassin:908844101015859310>");
		put("Elite", "<:Elite:908844101653393419>");
	}};
	private static String[] wears = {"Battle Scarred", "Well Worn", "Field-Tested", "Minimal Wear", "Factory New"};
	private static String[] paintEffects = {"Hot", "Cool", "Isotope" }; //I don't expect more of these so thus hard code here
	
	//Specific crate properties
	private String name;
	private int number;
	private boolean unusuals; //For free case things
	private boolean strangeUnusual;
	private boolean isCase;
	private boolean isSkin;
	private boolean isKillstreakKits;
	private ArrayList<String> effectList;
	private ArrayList<CrateItem> itemList;
	private JsonElement bonusList; //Has a list of arrays, up to 1 item from each array can be won
	private boolean crateBonus;
	private String crateHats = "hats";
	
	public Crate(String name, int number, boolean unusual, boolean strangeUnusual, boolean isCase, boolean isSkin, boolean isKillstreakKits) {
		this.name = name;
		this.number = number;
		this.unusuals = unusual;
		this.strangeUnusual = strangeUnusual;
		this.isCase = isCase;
		this.isSkin = isSkin;
		this.isKillstreakKits = isKillstreakKits;
		itemList = new ArrayList<CrateItem>();
		effectList = new ArrayList<String>();
		
	}
	
	/**
	 * @return Generic odds
	 */
	public static String getAllOdds() {
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		DecimalFormat threeDec = new DecimalFormat("###,###.###");
		DecimalFormat noDec = new DecimalFormat("###,###");
		double merc = 1.0-tierRollingChance;
		double com = merc * tierRollingChance;
		double ass = com * tierRollingChance;
		double elite = 1.0 - merc - com - ass;
		String out = "Overall crate/case odds:\n"
				+ "\tBoth Crates and Cases:\n"
				+ "\t\tUnusual chance: " + twoDec.format(unusualChance*100.0) + "%\n"
				+ "\t\tStrange chance (applicable crates and all cases): " + noDec.format(strangeChance*100.0) + "%\n"
				+ "\n\tCase related:\n"
				+ "\t\tRolling tier upgrade chance: " +  twoDec.format(tierRollingChance*100.0) + "%\n"
				+ "\t\t\t" + rarityEmotes.get("Mercenary") + "Mercenary effective: " +  twoDec.format(merc*100.0) + "%\n"
				+ "\t\t\t" + rarityEmotes.get("Commando") + "Commando effective: " +  twoDec.format(com*100.0) + "%\n"
				+ "\t\t\t" + rarityEmotes.get("Assassin") + "Assassin effective: " +  threeDec.format(ass*100.0) + "%\n"
				+ "\t\t\t" + rarityEmotes.get("Elite") + "Elite effective: " +  threeDec.format(elite*100.0) + "%\n"
				+ "\t\tSkin wear:\n"
				+ "\t\t\tBattle Scarred: " + noDec.format(battleWorn*100.0) + "%\n"
				+ "\t\t\tWell Worn: " + noDec.format((wellWorn - battleWorn)*100.0) + "%\n"
				+ "\t\t\tField-Tested: " + noDec.format((fieldTested - wellWorn)*100.0) + "%\n"
				+ "\t\t\tMinimal Wear: " + noDec.format((minimalWear - fieldTested)*100.0) + "%\n"
				+ "\t\t\tFactory New: " + noDec.format((1.0 - minimalWear)*100.0) + "%\n"
				+ "\t\tRolling Bonus Item chance: " + twoDec.format(mediumBonus) + "%\n"
				+ "\t\t\tSingle Bonus Item from normal cases: " + twoDec.format(mediumBonus*5.0) + "% (5 types of bonus items)\n" //TODO magic numbers maybe
				+ "\t\t\tTwo Bonus Items from normal cases: " + twoDec.format(mediumBonus*5.0 * (mediumBonus * 4.0)) + "% (5 types of bonus items)\n"
				+ "\t\t\tSingle Bonus Item from winter cosmetic or some halloween cases: " + twoDec.format(mediumBonus*6.0) + "% (6 types of bonus items)\n"
				+ "\t\t\tTwo Bonus Items from winter cosmetic or some halloween cases: " + twoDec.format(mediumBonus*6.0 * (mediumBonus * 5.0)) + "% (6 types of bonus items)\n"
				+ "\t\tUnusualifier Bonus Item chance: " + twoDec.format(largeBonus*100.0) + "%\n";
		return out;
	}
	
	public ArrayList<Item> open(Player player) {
		if(isCase) {
			return openCase(player);
		} else {
			return openCrate(player);
		}
	}
	
	public ArrayList<Item> openCrate(Player player) {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(this.drawMainItem(player));
		if(crateBonus) {
			JsonObject bonusLists = bonusList.getAsJsonObject();
			Random rand = new Random();
			for(Entry<String, JsonElement> listObj : bonusLists.entrySet()) {
				double weight = listObj.getValue().getAsJsonObject().get("weight").getAsDouble();
				if(rand.nextDouble() <= weight) {
					JsonArray bonusItems = listObj.getValue().getAsJsonObject().get("items").getAsJsonArray();
					String itemName = bonusItems.get(rand.nextInt(bonusItems.size())).getAsString();
					items.add(new Item(itemName, listObj.getValue().getAsJsonObject().get("quality").getAsString(), 1, 1, player, true));
				}
			}
		}
		return items;
	}
	
	/**
	 * Open a case, drawns both bonus and main items.
	 * @param player
	 * @return ArrayList of the items
	 */
	public ArrayList<Item> openCase(Player player) {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(this.drawMainItem(player));
		if(unusuals) {
			for(Item i : this.drawBonusItems(player)) {
				items.add(i);
			}
		}
		return items;
	}
	
	/**
	 * @param player
	 * @return The main item from the case (unusual or normal)
	 */
	public Item drawMainItem(Player player) {
		Random rand = new Random();
		boolean strange = (rand.nextDouble() <= strangeChance);
		if(rand.nextDouble() <= unusualChance && unusuals) {
			if(isCase) { //Unusual case draw
				int tierNum = 0;
				String tier = "";
				if(isSkin) {
					while(rand.nextDouble() < tierRollingChance && tierNum+1 < this.getTiers(false).size()) {
						tierNum++;
					}
					tier = this.getTiers(false).get(tierNum);
				} else {
					int i = 2;
					tier = rarities[i];
					if(rand.nextDouble() < tierRollingChance) { //Merc -> Commando
						if(this.getTiers(true).contains(rarities[i+1])) {
							i++;
							tier = rarities[i];
						} else {
							i++;
						}
						if(rand.nextDouble() < tierRollingChance) { //Commando -> Assassin
							if(this.getTiers(true).contains(rarities[i+1])) {
								i++;
								tier = rarities[i];
							} else {
								i++;
							}
							if(rand.nextDouble() < tierRollingChance) { //Assassin -> Elite
								if(this.getTiers(true).contains(rarities[i+1])) {
									i++;
									tier = rarities[i];
								}
							}
						}
					}
					
				}
				
				CrateItem cItem = null;
				ArrayList<CrateItem> tierItemList = this.getTieredItems(tier);
				do {
					cItem = tierItemList.get(rand.nextInt(tierItemList.size()));
				} while(!cItem.isHat() && !isSkin);
				String effect = effectList.get(rand.nextInt(effectList.size()));
				Item item = new Item(cItem.getName(), "Unusual", rand.nextInt(100)+1, 1, player, true);
				item.setEffect(effect);
				item.setOrigin(this.name);
				item.setTier(cItem.getQuality());
				if(strange) {
					item.setSecondaryQuality("Strange");
				}
				if(isSkin) {
					item.setLevel(99);
					double wear = rand.nextDouble();
					if(wear <= battleWorn) {
						item.setWear(wears[0]);
					} else if(wear <= wellWorn) {
						item.setWear(wears[1]);
					} else if(wear <= fieldTested) {
						item.setWear(wears[2]);
					} else if(wear <= minimalWear) {
						item.setWear(wears[3]);
					} else {
						item.setWear(wears[4]);
					}
				}
				return item;
			} else { //Unusual crate draw
				String effect = effectList.get(rand.nextInt(effectList.size()));
				Item item = new Item(getCrateUnusualHat(crateHats), "Unusual", rand.nextInt(100)+1, 1, player, true);
				item.setEffect(effect);
				item.setOrigin(this.name);
				if(strangeUnusual && strange) {
					item.setSecondaryQuality("Strange");
				}
				return item;
			}
		} else if(isCase) { //Normal case draw
			int tierNum = 0;
			String tier = "";
			while(rand.nextDouble() < tierRollingChance && tierNum+1 < this.getTiers(false).size()) {
				tierNum++;
			}
			tier = this.getTiers(false).get(tierNum);
			CrateItem cItem = null;
			ArrayList<CrateItem> tierItemList = this.getTieredItems(tier);
			cItem = tierItemList.get(rand.nextInt(tierItemList.size()));
			Item item = new Item(cItem.getName(), "Unique", 1, 1, player, true);
			item.setOrigin(this.name);
			item.setTier(cItem.getQuality());
			if(strange && unusuals) { //Cases w/o unusuals do not have stranges 
				item.setQuality("Strange");
			}
			if(isSkin) {
				item.setLevel(99);
				double wear = rand.nextDouble();
				if(wear <= battleWorn) {
					item.setWear(wears[0]);
				} else if(wear <= wellWorn) {
					item.setWear(wears[1]);
				} else if(wear <= fieldTested) {
					item.setWear(wears[2]);
				} else if(wear <= minimalWear) {
					item.setWear(wears[3]);
				} else {
					item.setWear(wears[4]);
				}
			}
			return item;
		} else { //Normal crate draw
			int totalWeight = 0;
			for(CrateItem ci : itemList) {
				totalWeight += ci.getWeight() * 10000;
			}
			int choice = rand.nextInt(totalWeight);
			CrateItem cItem = null;
			for(CrateItem ci : itemList) {
				cItem = ci;
				if(choice - (ci.getWeight()*10000) <= 0) {
					break;
				} else {
					choice = (int) (choice - (ci.getWeight()*10000));
				}
			}
			Item item = null;
			if(isKillstreakKits()) {
				double tier = rand.nextDouble();
				item = new Item(cItem.getName() + " Kit", cItem.getQuality(), 5, 1, player, false);

				if(tier <= professionalChance) {
					item.setKillstreakTier(3);
					item.setKillstreakSheen(Tour.randomSheen());
					item.setKillstreaker(Tour.randomKillstreaker());
				} else if(tier <= (professionalChance + specializedChance)) {
					item.setKillstreakTier(2);
					item.setKillstreakSheen(Tour.randomSheen());
				} else {
					item.setKillstreakTier(1);
				}
				
			} else {
				item = new Item(cItem.getName(), cItem.getQuality(), 1, 1, player, true);
			}
			item.setOrigin(this.name);
			if(strangeUnusual && strange) {
				if(item.getQuality().equals("Unique")) {
					item.setQuality("Strange");
				} else {
					item.setSecondaryQuality("Strange"); //Haunted items
				}
			}
			return item;
		}
	}
	
	/**
	 * @param player
	 * @return List of bonus items if any
	 */
	public ArrayList<Item> drawBonusItems(Player player) {
		ArrayList<Item> bonusItems = new ArrayList<Item>();
		Random rand = new Random();
		for(Entry<String, JsonElement> bonusType : bonusList.getAsJsonObject().entrySet()) {
			//Unusualifier has lower chance, everything else has an independent chance of happening that is probably the same chance.
			//TODO update odds if I get bonus item data
			if(bonusType.getKey().equals("Unusualifier") && rand.nextDouble() < largeBonus) {
				String uName = bonusType.getValue().getAsJsonArray().get(rand.nextInt(bonusType.getValue().getAsJsonArray().size())).getAsString();
				uName = "Unusual Taunt: " + uName + " Unusualifier";
				Item uItem = new Item(uName, "Unusual", 5, 1, player, true);
				uItem.setOrigin(this.name);
				bonusItems.add(uItem);
			} else if(rand.nextDouble() < mediumBonus && !bonusType.getKey().equals("Unusualifier")) {
				String iName = bonusType.getValue().getAsJsonArray().get(rand.nextInt(bonusType.getValue().getAsJsonArray().size())).getAsString();
				Item bItem = new Item(iName, "Unique", 1, 1, player, true); //TODO stat clocks have different levels than 1 but CBA rn
				bItem.setOrigin(this.name);
				bonusItems.add(bItem);
			}
		}
		return bonusItems;
	}
	
	/**
	 * @param tier
	 * @return Returns ArrayList of all items in the specified tier
	 */
	public ArrayList<CrateItem> getTieredItems(String tier) {
		ArrayList<CrateItem> tierItemList = new ArrayList<CrateItem>();
		for(CrateItem i : itemList) {
			if(i.getQuality().equals(tier)) {
				tierItemList.add(i);
			}
		}
		return tierItemList;
	}
	
	/**
	 * @param hatsOnly
	 * @return ArrayList of tiers. If hatsOnly then only includes tiers where there is a valid hat.
	 */
	public ArrayList<String> getTiers(boolean hatsOnly) {
		if(!isCase) {
			return null;
		}
		ArrayList<String> grades = new ArrayList<String>();
		for(CrateItem i : itemList) {
			if(!grades.contains(i.getQuality())) {
				if(hatsOnly && !i.isHat()) {
					continue;
				}
				grades.add(i.getQuality());
			}
		}
		return grades;
	}
	
	public static String getCrateUnusualHat(String type) {
		String hat = "null";
		Random rand = new Random();
		
		InputStream is = Tour.class.getClassLoader().getResourceAsStream(HAT_FILE);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			JsonObject obj = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			JsonArray hats = obj.get(type).getAsJsonArray();
			hat = hats.get(rand.nextInt(hats.size())).getAsString();
		} catch(Exception e) {
			log.error("Something went wrong reading tf2hats.json");
			e.printStackTrace();
		}
		
		return hat;
	}
	
	public static ArrayList<Crate> getCrates() {
		ArrayList<Crate> crates = new ArrayList<Crate>();
		InputStream is = Tour.class.getClassLoader().getResourceAsStream(CRATE_FILE);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			JsonObject obj = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			JsonObject effects = obj.getAsJsonObject("effects");
			JsonObject cratesObj = obj.getAsJsonObject("crates");
			
			for(Entry<String, JsonElement> crateObj : cratesObj.entrySet()) {
				String cName = crateObj.getValue().getAsJsonObject().get("name").getAsString();
				Boolean strangeUnu = crateObj.getValue().getAsJsonObject().get("strangeUnusual").getAsBoolean();
				Crate crate = new Crate(cName, Integer.parseInt(crateObj.getKey()), true, strangeUnu, false, false, false);
				for(Entry<String, JsonElement> lootListObj : crateObj.getValue().getAsJsonObject().get("lootlist").getAsJsonObject().entrySet()) {
					String listQuality = lootListObj.getValue().getAsJsonObject().get("quality").getAsString();
					double listWeight = lootListObj.getValue().getAsJsonObject().get("weight").getAsDouble();
					JsonArray listItems = lootListObj.getValue().getAsJsonObject().get("items").getAsJsonArray();
					listItems.forEach(itemObj -> {
						//Marking all items as not hat even though some of them might be since crates draw from an overall pool
						double weight = Math.floor((listWeight/(double)listItems.size())*10000.0)/(double)10000.0;
						CrateItem item = new CrateItem(itemObj.getAsString(), listQuality, weight, false);
						crate.addItem(item);
					});
				}
				JsonArray crateEffects = effects.get(crateObj.getValue().getAsJsonObject().get("effects").getAsString()).getAsJsonArray();
				crateEffects.forEach(effect -> {
					crate.addEffect(effect.getAsString());
				});
				
				if(crateObj.getValue().getAsJsonObject().has("bonuslist")) {
					crate.bonusList = crateObj.getValue().getAsJsonObject().get("bonuslist");
					crate.setCrateBonus(true);
				} else {
					crate.setCrateBonus(false);
				}
				if(crateObj.getValue().getAsJsonObject().has("hatspool")) {
					crate.setCrateHats(crateObj.getValue().getAsJsonObject().get("hatspool").getAsString());
				}
				if(crateObj.getValue().getAsJsonObject().has("killstreak")) {
					crate.setKillstreakKits(true);
				}
				crates.add(crate);
			}
			
		} catch(Exception e) {
			log.error("Something went wrong getting crates.");
			e.printStackTrace();
		}
		return crates;
	}
	
	public static ArrayList<Crate> getCases() {
		ArrayList<Crate> cases = new ArrayList<Crate>();
		InputStream is = Tour.class.getClassLoader().getResourceAsStream(CASE_FILE);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			JsonObject obj = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			//Effects
			ArrayList<String> globalEffects = new ArrayList<String>();
			ArrayList<String> paintEffects = new ArrayList<String>();
			for(String s : Crate.paintEffects) {
				paintEffects.add(s);
			}
			JsonArray effects = obj.get("GlobalEffects").getAsJsonArray();
			for(JsonElement s : effects) {
				globalEffects.add(s.getAsString());
			}
			//Global bonus items
			JsonElement globalBonusItems = obj.get("GlobalBonus");
			
			
			for(Entry<String, JsonElement> caseElement : obj.entrySet()) {
				if(caseElement.getKey().equals("GlobalEffects") || caseElement.getKey().equals("GlobalBonus")) { continue; } //Not a case
				String caseName = caseElement.getKey();
				int caseNum = caseElement.getValue().getAsJsonObject().get("number").getAsInt();
				boolean skins = caseName.contains("Weapons Case") || caseName.contains("War Paint Case");
				boolean caseUnusuals = true;
				if(caseName.contains("Grade Keyless Case")) {
					skins = true;
					caseUnusuals = false;
				}
				
				//Get items
				ArrayList<CrateItem> crateItems = new ArrayList<CrateItem>();
					//Get hats if we can
				ArrayList<String> hatList = new ArrayList<String>();
				if(caseElement.getValue().getAsJsonObject().has("hats")) {
					JsonArray hatArr = caseElement.getValue().getAsJsonObject().get("hats").getAsJsonArray();
					for(JsonElement s : hatArr) {
						hatList.add(s.getAsString());
					}
				}
					//Actually get items now
				for(String rarityString : rarities) {
					JsonArray rarityArr = caseElement.getValue().getAsJsonObject().get(rarityString.toLowerCase()).getAsJsonArray();
					if(!rarityArr.isEmpty()) {
						for(JsonElement s : rarityArr) {
							crateItems.add(new CrateItem(s.getAsString(), rarityString, 1, hatList.contains(s.getAsString())));
						}
					}
				}
				//We have the items, check if there are extra effects
				ArrayList<String> caseEffects = new ArrayList<String>();
				if(!skins) {
					for(String s : globalEffects) {
						caseEffects.add(s);
					}
					if(caseElement.getValue().getAsJsonObject().has("effects")) {
						JsonArray effectArr = caseElement.getValue().getAsJsonObject().get("effects").getAsJsonArray();
						for(JsonElement s : effectArr) {
							caseEffects.add(s.getAsString());
						}
					}
				} else {
					caseEffects = paintEffects;
				}
				//Get additional bonus items if exist
				JsonElement caseBonus = globalBonusItems.deepCopy();
				if(caseElement.getValue().getAsJsonObject().has("bonus")) {
					caseBonus.getAsJsonObject().add("Specific", caseElement.getValue().getAsJsonObject().get("bonus"));
				}
				
				Crate caseItem = new Crate(caseName, caseNum, caseUnusuals, true, true, skins, false);
				caseItem.setEffectList(caseEffects);
				caseItem.setItemList(crateItems);
				caseItem.setBonusList(caseBonus);
				cases.add(caseItem);
			}
		} catch(Exception e) {
			log.error("Something went wrong getting cases.");
			e.printStackTrace();
		}
		return cases;
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
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * @return the unusuals
	 */
	public boolean isUnusuals() {
		return unusuals;
	}

	/**
	 * @param unusuals the unusuals to set
	 */
	public void setUnusuals(boolean unusuals) {
		this.unusuals = unusuals;
	}

	/**
	 * @return the strangeUnusual
	 */
	public boolean isStrangeUnusual() {
		return strangeUnusual;
	}

	/**
	 * @param strangeUnusual the strangeUnusual to set
	 */
	public void setStrangeUnusual(boolean strangeUnusual) {
		this.strangeUnusual = strangeUnusual;
	}

	/**
	 * @return the isCase
	 */
	public boolean isCase() {
		return isCase;
	}

	/**
	 * @param isCase the isCase to set
	 */
	public void setCase(boolean isCase) {
		this.isCase = isCase;
	}

	/**
	 * @return the isSkin
	 */
	public boolean isSkin() {
		return isSkin;
	}

	/**
	 * @param isSkin the isSkin to set
	 */
	public void setSkin(boolean isSkin) {
		this.isSkin = isSkin;
	}

	/**
	 * @return the effectList
	 */
	public ArrayList<String> getEffectList() {
		return effectList;
	}

	/**
	 * @param effectList the effectList to set
	 */
	public void setEffectList(ArrayList<String> effectList) {
		this.effectList = effectList;
	}
	
	/**
	 * @param effect to add to effectList
	 */
	public void addEffect(String effect) {
		this.effectList.add(effect);
	}

	/**
	 * @return the itemList
	 */
	public ArrayList<CrateItem> getItemList() {
		return itemList;
	}

	/**
	 * @param itemList the itemList to set
	 */
	public void setItemList(ArrayList<CrateItem> itemList) {
		this.itemList = itemList;
	}
	
	/**
	 * @param item to add to itemList
	 */
	public void addItem(CrateItem item) {
		this.itemList.add(item);
	}
	
	/**
	 * @return the bonusList
	 */
	public JsonElement getBonusList() {
		return bonusList;
	}

	/**
	 * @param bonusList the bonusList to set
	 */
	public void setBonusList(JsonElement bonusList) {
		this.bonusList = bonusList;
	}
	
	public boolean hasCrateBonus() {
		if(isCase) { return false; }
		return crateBonus;
	}
	
	public void setCrateBonus(boolean bonus) {
		this.crateBonus = bonus;
	}
	
	public String getCrateHats() {
		return crateHats;
	}
	
	public void setCrateHats(String hats) {
		this.crateHats = hats;
	}
	
	public String getNames() {
		return name + " #" + number;
	}
	
	public void setKillstreakKits(Boolean ks) {
		isKillstreakKits = ks;
	}
	
	public boolean isKillstreakKits() {
		return isKillstreakKits;
	}

	@Override
	public String toString() {
		String out = "Crate Name: " + name + " Number: " + number
				+ "\n  Unusuals: " + unusuals + " Strange Unusuals: " + strangeUnusual
				+ "\n  Case: " + isCase + " Skins/Paints: " + isSkin;
		out += "\n  Effects: ";
		for(String effect : effectList) {
			out += "\n\t" + effect;
		}
		out += "\n  Items: ";
		for(CrateItem item : itemList) {
			out += "\n\t" + item.toString();
		}
		
		return out;
	}
}
