package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import beans.Item;
import beans.Player;

/**
 * A class to contain tour related static methods and some String[]. 
 * Most methods require a Player owner parameter which is the player object of the person receiving the item.
 * <p>
 * Valid tours are TwoCities, MechaEngine, GearGrinder, OilSpill, SteelTrap
 * @author jh34ghu43gu
 */
public class Tour {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Tour.class);
	private static Random rand = new Random();
	//Note these match the order of the item input binary
	public static String[] battleWorns = {"Battle-Worn Robot KB-808", "Battle-Worn Robot Taunt Processor", "Battle-Worn Robot Money Furnace" };
	public static String[] reinforceds = {"Reinforced Robot Emotion Detector", "Reinforced Robot Humor Suppression Pump", "Reinforced Robot Bomb Stabilizer" };
	public static String[] pristines = {"Pristine Robot Currency Digester", "Pristine Robot Brainstorm Bulb" };
	
	public static String[] botkillers = {"Flame Thrower", "Knife", "Medi Gun", "Minigun", "Rocket Launcher", "Scattergun", "Sniper Rifle", "Stickybomb Launcher", "Wrench"};
	public static String[] roboHats = {"Robot Running Man", "Tin Pot", "Pyrobotics Pack", "Battery Bandolier",
			"U-clank-a", "Tin-1000", "Medic Mech-Bag", "Bolted Bushman", "Stealth Steeler" };
	
	public static HashMap<String, String> australiumEmotes = new HashMap<String, String>() {{
		put("Australium Ambassador", "<:Backpack_Australium_Ambassador:835951712472531044>");
		put("Australium Axtinguisher", "<:Backpack_Australium_Axtinguisher:835951710442618960>");
		put("Australium Black Box", "<:Backpack_Australium_Black_Box:835951730352586773>");
		put("Australium Blutsauger", "<:Backpack_Australium_Blutsauger:835951748018339881>");
		put("Australium Eyelander", "<:Backpack_Australium_Eyelander:835951715777642516>");
		put("Australium Flame Thrower", "<:Backpack_Australium_Flame_Throwe:835951748891803658>");
		put("Australium Force-A-Nature", "<:Backpack_Australium_ForceANature:835951743779078146>");
		put("Australium Frontier Justice", "<:Backpack_Australium_Frontier_Jus:835951742763401276>");
		put("Australium Grenade Launcher", "<:Backpack_Australium_Grenade_Laun:835951750326124564>");
		put("Australium Knife", "<:Backpack_Australium_Knife:835951720051376139>");
		put("Australium Medi Gun", "<:Backpack_Australium_Medi_Gun:835951750023348224>");
		put("Australium Minigun", "<:Backpack_Australium_Minigun:835951752661565510>");
		put("Australium Rocket Launcher", "<:Backpack_Australium_Rocket_Launc:835951747600089139>");
		put("Australium Scattergun", "<:Backpack_Australium_Scattergun:835951741891510313>");
		put("Australium Sniper Rifle", "<:Backpack_Australium_Sniper_Rifle:835951743505268736>");
		put("Australium Stickybomb Launcher", "<:Backpack_Australium_Stickybomb_L:835951751672496149>");
		put("Australium Submachine Gun", "<:Backpack_Australium_SMG:835951740716711936>");
		put("Australium Tomislav", "<:Backpack_Australium_Tomislav:835951748702797824>");
		put("Australium Wrench", "<:Backpack_Australium_Wrench:835951728343121972>");
		put("Golden Frying Pan", "<:Backpack_Golden_Frying_Pan:835951747255107654>");
	}};
	
	private static final String WEAPON_FILE = "tf2weapons.json";
	
	/**
	 * Creates a full tour's worth of loot (missions + tour finish).
	 * @param tourName
	 * @param tourNumber
	 * @param owner
	 * @return
	 */
	public static ArrayList<Item> doTour(String tourName, int tourNumber, Player owner) {
		ArrayList<Item> loot = new ArrayList<Item>();
		int missions = 0;
		if(tourName.equalsIgnoreCase("OilSpill")) {
			missions = 6;
		} else if(tourName.equalsIgnoreCase("SteelTrap")) {
			missions = 6;
		} else if(tourName.equalsIgnoreCase("MechaEngine")) {
			missions = 3;
		} else if(tourName.equalsIgnoreCase("TwoCities")) {
			missions = 4;
		} else if(tourName.equalsIgnoreCase("GearGrinder")) {
			missions = 3;
		} else { //Empty if invalid tour
			return loot;
		}
		
		for(int i = 0; i < missions; i++) { //Do X missions
			for(Item item : Tour.doMission(tourName, owner)) {
				//Combine
				for(Item lootItem : loot) {
					if(lootItem.canCombine(item)) {
						lootItem.setQuantity(lootItem.getQuantity() + item.getQuantity());
						item.setQuantity(0);
						break;
					}
				}
				//If item has a quantity over 0 then it wasn't added to an item that already existed
				if(item.getQuantity() > 0) {
					loot.add(item);
				}
			}
		}
		for(Item item : Tour.doTourLoot(tourName, tourNumber, owner)) { //Actual tour loot
			loot.add(item);
		}
		
		return loot;
	}
	
	/**
	 * Get individual mission loot.
	 * @param tourName
	 * @param owner
	 * @return
	 */
	public static ArrayList<Item> doMission(String tourName, Player owner) {
		ArrayList<Item> loot = new ArrayList<Item>();
		loot.add(Tour.randomMissionItem(owner, tourName));
		if(tourName.equalsIgnoreCase("TwoCities")) {
			for(Item i : Tour.randomMissionParts(owner)) {
				loot.add(i);
			}
			//11% chance for spec fab
			if(rand.nextInt(100) < 11) {
				loot.add(Tour.randomKillstreakFab(2, owner));
			}
		}
		
		return loot;
	}
	
	/**
	 * Get the end tour loot (botkillers, aussies, ks kits)
	 * @param tourName
	 * @param tour	The tour number
	 * @param owner
	 * @return
	 */
	public static ArrayList<Item> doTourLoot(String tourName, int tour, Player owner) {
		ArrayList<Item> loot = new ArrayList<Item>();
		int level = (int) (tour % 256); //Simulate tour number roll over
		boolean rareBotkiller = rand.nextInt(10) == 1; //10% chance for the rare botkiller
		Item botkiller = Tour.randomWeapon(3, owner);
		botkiller.setOrigin(tourName);
		botkiller.setLevel(level);
		if(tourName.equalsIgnoreCase("OilSpill")) {
			if(!rareBotkiller) {
				botkiller.setName("Rust Botkiller " + botkiller.getName() + " Mk.I");
			} else {
				botkiller.setName("Blood Botkiller " + botkiller.getName() + " Mk.I");
			}
			loot.add(botkiller);
		} else if(tourName.equalsIgnoreCase("SteelTrap")) {
			if(!rareBotkiller) {
				botkiller.setName("Silver Botkiller " + botkiller.getName() + " Mk.I");
			} else {
				botkiller.setName("Gold Botkiller " + botkiller.getName() + " Mk.I");
			}
			loot.add(botkiller);
		} else if(tourName.equalsIgnoreCase("MechaEngine")) {
			if(!rareBotkiller) {
				botkiller.setName("Silver Botkiller " + botkiller.getName() + " Mk.II");
			} else {
				botkiller.setName("Gold Botkiller " + botkiller.getName() + " Mk.II");
			}
			loot.add(botkiller);
		} else if(tourName.equalsIgnoreCase("TwoCities")) {
			Item ks = Tour.randomWeapon(1, owner);
			ks.setKillstreakTier(1);
			ks.setName(ks.getName() + " Kit");
			ks.setLevel(level);
			ks.setOrigin(tourName);
			loot.add(ks);
			
			Item spec = Tour.randomKillstreakFab(2, owner);
			spec.setLevel(level);
			loot.add(spec);
			
			//20% chance prof fab
			if(rand.nextInt(100) < 20) {
				Item pro = Tour.randomKillstreakFab(3, owner);
				pro.setLevel(level);
				loot.add(pro);
			}
		} else if(tourName.equalsIgnoreCase("GearGrinder")) {
			if(!rareBotkiller) {
				botkiller.setName("Carbonado Botkiller " + botkiller.getName() + " Mk.I");
			} else {
				botkiller.setName("Diamond Botkiller " + botkiller.getName() + " Mk.I");
			}
			loot.add(botkiller);
		}
		
		//Aussie for not oil spill tours 2.11%
		if(!tourName.equalsIgnoreCase("OilSpill")) {
			int ausChance = rand.nextInt(100000);
			if(ausChance < 2110) {
				Item aus = null;
				if(ausChance < 5) { //Pan chance 1 in 19000 ~ 5/100000
					aus = new Item("Golden Frying Pan", "Strange", 1, 1, owner, false);
					aus.setKillstreakTier(3);
					aus.setKillstreakSheen(Tour.randomSheen());
					aus.setKillstreaker(Tour.randomKillstreaker());
				} else {
					aus = Tour.randomWeapon(2, owner);
				}
				aus.setLevel(level);
				aus.setOrigin(tourName);
				loot.add(aus);
			}
		}
		
		return loot;
	}
	
	/**
	 * Creates a random item for generic mission loot
	 * Weapon - 93%
	 * Robot Hat - 5.95%
	 * Normal hat/paint - 2%
	 * Robro 3000 - 0.05%
	 * @param owner
	 * @param tourName
	 * @return
	 */
	public static Item randomMissionItem(Player owner, String tourName) {
		int type = rand.nextInt(10000)+1;
		String itemName = "";
		if(type <= 9300) { //Random weapon (93%)
			return Tour.randomWeapon(0, owner);
		} else if(type <= 9500) { //Normal hat or paint (2%)
			//TODO tool/hat randomizer
			itemName = "Random Hat/Paint";
		} else if(type <= 9995) { //Robo hat (5.95%)
			int roboHat = rand.nextInt(roboHats.length);
			itemName = roboHats[roboHat];
		} else { //Robro (0.05%)
			itemName = "Robro 3000";
		}
		Item item = new Item(itemName, "Unique", 1, 1, owner, true);
		item.setOrigin(tourName);
		return item;
	}
	
	/**
	 * Creates an array list of robot parts from a 2c mission
	 * 4 Battle-Worn and 1 Reinforced every time
	 * Recurring 50% chance for extra Battle-Worn
	 * Recurring 25% chance for extra Reinforced
	 * 1 Pristine part - 13%
	 * 1 Specialized kit - 11% - not in this method
	 * @param owner
	 * @return
	 */
	public static ArrayList<Item> randomMissionParts(Player owner) {
		ArrayList<Item> parts = new ArrayList<Item>();
		
		HashMap<String, Integer> partsMap = new HashMap<String, Integer>();
		//Guaranteed 4 BW parts
		for(int i = 0; i < 4; i++) {
			String bw = battleWorns[rand.nextInt(battleWorns.length)];
			if(partsMap.containsKey(bw)) {
				partsMap.put(bw, partsMap.get(bw)+1);
			} else {
				partsMap.put(bw, 1);
			}
		}
		//Roll for extra BW parts 50% chance until failure
		while(rand.nextInt(100) < 50) {
			String bw = battleWorns[rand.nextInt(battleWorns.length)];
			if(partsMap.containsKey(bw)) {
				partsMap.put(bw, partsMap.get(bw)+1);
			} else {
				partsMap.put(bw, 1);
			}
		}
		//Guaranteed reinforced part
		String r = reinforceds[rand.nextInt(reinforceds.length)];
		partsMap.put(r, 1);
		//Roll for extra reinforced parts 25% chance until failure
		while(rand.nextInt(100) < 25) {
			r = reinforceds[rand.nextInt(reinforceds.length)];
			if(partsMap.containsKey(r)) {
				partsMap.put(r, partsMap.get(r)+1);
			} else {
				partsMap.put(r, 1);
			}
		}
		//13% chance for 1 pristine
		if(rand.nextInt(100) < 13) {
			String p = pristines[rand.nextInt(pristines.length)];
			partsMap.put(p, 1);
		}
		//Create items and put in the output array
		for(Map.Entry<String, Integer> entry : partsMap.entrySet()) {
			Item part = new Item(entry.getKey(), "Unique", 1, entry.getValue(), owner, true);
			part.setOrigin("TwoCities");
			parts.add(part);
		}
		return parts;
	}
	
	/**
	 * Get a random weapon from tf2weapons.json depending on the given type wanted.
	 * @param type	0 for drops, 1 for ks, 2 for aussie, 3 for botkillers
	 * @param owner	Player object that owns the item.
	 * @return	Item or null if error, note all items returned are level 1
	 */
	public static Item randomWeapon(int type, Player owner) {
		if(type == 3) {
			String itemName = botkillers[rand.nextInt(botkillers.length)];
			Item drop = new Item(itemName, "Strange", 1, 1, owner, true);
			return drop;
		}
		InputStream is = Tour.class.getClassLoader().getResourceAsStream(WEAPON_FILE);
		Gson gson = new Gson();
		try {
			JsonObject weps = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			JsonArray weaponsArray = weps.get("weapons").getAsJsonArray();
			
			
			JsonObject weapon = null;
			ArrayList<JsonObject> weaponPool = new ArrayList<JsonObject>();
			for(int i = 0; i < weaponsArray.size(); i++) {
				JsonObject weaponObj = weaponsArray.get(i).getAsJsonObject();
				if(type == 0) { //Drop
					if(weaponObj.get("Drop").getAsBoolean()) {
						weaponPool.add(weaponObj);
					}
				} else if(type == 1) { //KS
					if(weaponObj.get("KS").getAsBoolean()) {
						weaponPool.add(weaponObj);
					}
				} else if(type == 2) { //Aussie
					if(weaponObj.get("Australium").getAsBoolean()) {
						weaponPool.add(weaponObj);
					}
				}
			}
			
			if(weaponPool.isEmpty()) {
				//Failsafe for bad type
				int i = rand.nextInt(weaponsArray.size());
				weapon = weaponsArray.get(i).getAsJsonObject();
			} else {
				if(type != 2) {
					int i = rand.nextInt(weaponPool.size());
					weapon = weaponPool.get(i);
				} else { //Aussies are weighted
					ArrayList<JsonObject> aussiePool = new ArrayList<JsonObject>();
					for(JsonObject o : weaponPool) {
						long weight = o.get("Weight").getAsLong();
						for(int i = 0; i < weight; i++) {
							JsonObject clone = o;
							aussiePool.add(clone);
						}
					}
					int i = rand.nextInt(aussiePool.size());
					weapon = aussiePool.get(i);
				}
				
			}
			String quality = "Unique";
			String wepName = weapon.get("Name").getAsString();
			boolean craft = true;
			if(type == 2) {
				wepName = "Australium " + wepName;
				quality = "Strange";
				craft = false;
			}
			
			Item drop = new Item(wepName, quality, 1, 1, owner, craft);
			return drop;
		} catch (IOException e) {
			log.error("Could not parse tf2weapons.json.");
			log.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param tier	2 for spec, 3 for pro
	 * @param owner
	 * @return A killstreak kit fabricator
	 */
	public static Item randomKillstreakFab(int tier, Player owner) {
		if(tier != 2 && tier != 3) {
			log.warn("Tried to create a ks fabricator for invalid tier: " + tier);
			return null;
		}
		Item item = Tour.randomWeapon(1, owner);
		item.setName(item.getName() + " Kit Fabricator");
		item.setKillstreakTier(tier);
		item.setKillstreakSheen(Tour.randomSheen());
		if(tier == 3) {
			item.setKillstreaker(Tour.randomKillstreaker());
		}
		
		HashMap<String, Integer> partsMap = new HashMap<String, Integer>();
		
		String inputString = "";
		int bw = 0;
		int r = 0;
		int p = 0;
		if(tier == 2) {
			inputString += "01"; //Spec takes 1 ks item
			bw = 24;
			r = 5;
			p = 0;
		} else {
			inputString += "10"; //Pro takes 2 spec ks item
			bw = 16;
			r = 6;
			p = 3;
		}
		
		for(int i = 0; i < bw; i++) {
			String bwPart = battleWorns[rand.nextInt(battleWorns.length)];
			if(partsMap.containsKey(bwPart)) {
				partsMap.put(bwPart, partsMap.get(bwPart)+1);
			} else {
				partsMap.put(bwPart, 1);
			}
		}
		for(int i = 0; i < r; i++) {
			String rPart = reinforceds[rand.nextInt(reinforceds.length)];
			if(partsMap.containsKey(rPart)) {
				partsMap.put(rPart, partsMap.get(rPart)+1);
			} else {
				partsMap.put(rPart, 1);
			}
		}
		for(int i = 0; i < p; i++) {
			String pPart = pristines[rand.nextInt(pristines.length)];
			if(partsMap.containsKey(pPart)) {
				partsMap.put(pPart, partsMap.get(pPart)+1);
			} else {
				partsMap.put(pPart, 1);
			}
		}
		
		String tempPartString = "";
		//BWs
		for(int i = 0; i < battleWorns.length; i++) {
			tempPartString = "";
			if(partsMap.containsKey(battleWorns[i])) {
				tempPartString = Integer.toBinaryString(partsMap.get(battleWorns[i]));
			}
			while(tempPartString.length() < 5) { //00000
				tempPartString = "0" + tempPartString;
			}
			inputString += tempPartString;
		}
		//Reinforceds
		for(int i = 0; i < reinforceds.length; i++) {
			tempPartString = "";
			if(partsMap.containsKey(reinforceds[i])) {
				tempPartString = Integer.toBinaryString(partsMap.get(reinforceds[i]));
			}
			while(tempPartString.length() < 3) { //000
				tempPartString = "0" + tempPartString;
			}
			inputString += tempPartString;
		}
		//Pristines
		for(int i = 0; i < pristines.length; i++) {
			tempPartString = "";
			if(partsMap.containsKey(pristines[i])) {
				tempPartString = Integer.toBinaryString(partsMap.get(pristines[i]));
			}
			while(tempPartString.length() < 2) { //00
				tempPartString = "0" + tempPartString;
			}
			inputString += tempPartString;
		}
		
		item.setInputs(Integer.parseInt(inputString, 2));
		item.setOrigin("TwoCities");
		return item;
	}
	
	/**
	 * Picks one of the 7 sheens randomly
	 * @return
	 */
	public static String randomSheen() {
		String[] sheens = {"Team Shine", "Mean Green", "Agonizing Emerald", 
				"Hot Rod", "Villainous Violet", "Deadly Daffodil", "Manndarin"};
		return sheens[rand.nextInt(7)];
	}
	
	/**
	 * Picks one of the 7 killstreakers randomly
	 * @return
	 */
	public static String randomKillstreaker() {
		String[] killstreaker = {"Fire Horns", "Tornado", "Singularity", 
				"Incinerator", "Flames", "Cerebral Discharge", "Hypno-Beam"};
		return killstreaker[rand.nextInt(7)];
	}
}
