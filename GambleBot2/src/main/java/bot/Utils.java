package bot;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Leaderboard;
import beans.Player;
import beans.Server;
import ch.qos.logback.classic.Logger;
import files.ConfigHelper;
import model.Skin;
import model.Tour;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import repository.LeaderboardRepository;
import repository.PlayerRepository;
import repository.ServerRepository;

/**
 * Utility class for static methods used in multiple other classes
 * @author jh34ghu43gu
 */
public class Utils {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Utils.class);
	
	private static final String TICKET_URL = "https://wiki.teamfortress.com/w/images/9/9b/Backpack_Tour_of_Duty_Ticket.png";
	private static final String KEY_URL = "https://wiki.teamfortress.com/w/images/8/83/Backpack_Mann_Co._Supply_Crate_Key.png";
	private static final int MAX_BOARDS = 100;
	private static final String PRICE_URL = "https://backpack.tf/api/IGetPrices/v4?key=";
	private static final String LOCAL_PRICELIST_FILE = "prices.json";
	private static final HashMap<String, Integer> QUALITY_MAP = new HashMap<String, Integer>() {{
		this.put("Unique", 6);
		this.put("Strange", 11);
		this.put("Unusual", 5);
		this.put("Haunted", 13);
		this.put("Vintage", 3);
		this.put("Genuine", 1);
		this.put("Collectors", 14);
	}};
	private static final HashMap<String, Integer> UNUSUAL_EFFECT_MAP = new HashMap<String, Integer>() {{
		this.put("Green Confetti", 6);
		this.put("Purple Confetti", 7);
		this.put("Haunted Ghosts", 8);
		this.put("Green Energy", 9);
		this.put("Purple Energy", 10);
		this.put("Circling TF Logo", 11);
		this.put("Massed Flies", 12);
		this.put("Burning Flames", 13);
		this.put("Scorching Flames", 14);
		this.put("Searing Plasma", 15);
		this.put("Vivid Plasma", 16);
		this.put("Sunbeams", 17);
		this.put("Circling Peace Sign", 18);
		this.put("Circling Heart", 19);
		this.put("Stormy Storm", 29);
		this.put("Blizzardy Storm", 30);
		this.put("Nuts n' Bolts", 31);
		this.put("Orbiting Planets", 32);
		this.put("Orbiting Fire", 33);
		this.put("Bubbling", 34);
		this.put("Smoking", 35);
		this.put("Steaming", 36);
		this.put("Flaming Fantern", 37);
		this.put("Cloudy Moon", 38);
		this.put("Cauldron Bubbles", 39);
		this.put("Eerie Orbiting Fire", 40);
		this.put("Knifestorm", 43);
		this.put("Misty Skull", 44);
		this.put("Harvest Moon", 45);
		this.put("It's A Secret To Everybody", 46);
		this.put("Stormy 13th Hour", 47);
		this.put("Kill-a-Watt", 56);
		this.put("Terror-Watt", 57);
		this.put("Cloud 9", 58);
		this.put("Aces High", 59);
		this.put("Dead Presidents", 60);
		this.put("Miami Nights", 61);
		this.put("Disco Beat Down", 62);
		this.put("Phosphorous", 63);
		this.put("Sulphurous", 64);
		this.put("Memory Leak", 65);
		this.put("Overclocked", 66);
		this.put("Electrostatic", 67);
		this.put("Power Surge", 68);
		this.put("Anti-Freeze", 69);
		this.put("Time Warp", 70);
		this.put("Green Black Hole", 71);
		this.put("Roboactive", 72);
		this.put("Arcana", 73);
		this.put("Spellbound", 74);
		this.put("Chiroptera Venenata", 75);
		this.put("Poisoned Shadows", 76);
		this.put("Something Burning This Way Comes", 77);
		this.put("Hellfire", 78);
		this.put("Darkblaze", 79);
		this.put("Demonflame", 80);
		this.put("Bozo The All-Gnawing", 81);
		this.put("Amaranthine", 82);
		this.put("Stare From Beyond", 83);
		this.put("The Ooze", 84);
		this.put("Ghastly Ghosts Jr", 85);
		this.put("Haunted Phantasm Jr", 86);
		this.put("Frostbite", 87);
		this.put("Molten Mallard", 88);
		this.put("Morning Glory", 89);
		this.put("Death at Dusk", 90);
		this.put("Abduction", 91);
		this.put("Atomic", 92);
		this.put("Subatomic", 93);
		this.put("Electric Hat Protector", 94);
		this.put("Magnetic Hat Protector", 95);
		this.put("Voltaic Hat Protector", 96);
		this.put("Galactic Codex", 97);
		this.put("Ancient Codex", 98);
		this.put("Nebula", 99);
		this.put("Death by Disco", 100);
		this.put("It's a mystery to everyone", 101);
		this.put("It's a puzzle to me", 102);
		this.put("Ether Trail", 103);
		this.put("Nether Trail", 104);
		this.put("Ancient Eldritch", 105);
		this.put("Eldritch Flame", 106);
	}};
	
	/**
	 * @param event
	 * @param channel Channel restriction to check for; either 'mvm' 'misc' or 'crate'
	 * @return If the command can be run in the given channel.
	 */
	public static boolean commandCheck(CommandEvent event, String channel) {
		Server server = getServer(event.getGuild().getId());
		String cID = event.getChannel().getId();
		if(channel.equalsIgnoreCase("mvm")) {
			if(server.getMvmChannel().isEmpty() || server.getMvmChannel().equals(cID)) {
				return true;
			}
		} else if(channel.equalsIgnoreCase("crate")) {
			if(server.getCrateChannel().isEmpty() || server.getCrateChannel().equals(cID)) {
				return true;
			}
		} else if(channel.equalsIgnoreCase("misc")) {
			if(server.getMiscChannel().isEmpty() || server.getMiscChannel().equals(cID)) {
				return true;
			}
		}
		return false; //Catch is always false
	}
	
	/**
	 * @param serverID
	 * @return Server object from database if it exists or a new object that will need to be saved later.
	 */
	public static Server getServer(String serverID) {
		ServerRepository SR = GambleBot.getContext().getBean(ServerRepository.class);
		Server server;
		if(SR.existsById(serverID)) {
			server = SR.getById(serverID);
		} else {
			server = new Server(serverID);
		}
		return server;
	}
	
	/**
	 * @param playerID
	 * @return Player object from database if it exists or a new object with a 0.0 balance that will need to be saved later.
	 */
	public static Player getPlayer(String playerID) {
		PlayerRepository PR = GambleBot.getContext().getBean(PlayerRepository.class);
		Player player;
		if(PR.existsById(playerID)) {
			player = PR.getById(playerID);
		} else {
			player = new Player(playerID, 0.0);
		}
		return player;
	}
	
	/**
	 * Shorter method to get command prefix, 
	 * also removes extra magic variables; only this and GambleBot's string needs changes
	 * @return
	 */
	public static String getPrefix() {
		return ConfigHelper.getOptionFromFile("DISCORD_PREFIX");
	}
	
	/**
	 * Convert an arraylist of items into an embed.
	 * @param items
	 * @param title 
	 * @param thumbnail mvm or crate
	 * @return
	 */
	public static MessageEmbed itemsToEmbed(Player player, ArrayList<Item> items, String title, String thumbnail) {
		EmbedBuilder EB = new EmbedBuilder();
		User owner = GambleBot.getJDA().getUserById(player.getId());
		EB.setColor(new Color(255,215,0));
		if(owner == null) {
			EB.setAuthor("");
		} else {
			EB.setAuthor(owner.getName());
		}
		//TODO maybe figure out how to add avatar pfp
		if(thumbnail.equalsIgnoreCase("mvm")) {
			EB.setThumbnail(TICKET_URL);
		} else if(thumbnail.equalsIgnoreCase("crate")) {
			EB.setThumbnail(KEY_URL);
		}
		//Off chance user didn't get enough stuff
		if(items.isEmpty()) {
			if(thumbnail.equalsIgnoreCase("mvm")) {
				EB.addField("Unlucky! Did not recieve any stranges or pro ks fabs.", "", false);
			} else if(thumbnail.equalsIgnoreCase("crate")) {
				EB.addField("Unlucky! Did not recieve enough significant items to display.", "", false);
			}
			return EB.build();
		}
		
		//Try to consolidate skins
		ArrayList<Item> noSkinItems = new ArrayList<Item>();
		ArrayList<Skin> skinItems = new ArrayList<Skin>();
		for(Item item : items) {
			if(item.getWear() == null) {
				noSkinItems.add(item);
			} else {
				Skin tempSkin = new Skin();
				tempSkin.setName(item.getName());
				tempSkin.setTier(item.getTier());
				tempSkin.setStrange(item.getQuality().equals("Strange")
						|| (item.getSecondaryQuality() != null && item.getSecondaryQuality().equals("Strange")));
				tempSkin.setUnusual(item.getQuality().equals("Unusual"));
				if(tempSkin.isUnusual()) {
					tempSkin.setEffect(item.getEffect());
				}
				tempSkin.addWear(item.getWear(), item.getQuantity());
				for(Skin skin : skinItems) {
					if(skin.canCombine(tempSkin)) {
						tempSkin = skin.combine(tempSkin);
					}
				}
				if(tempSkin.totalWears() > 0) {
					skinItems.add(tempSkin);
				}
			}
		}
		if(!skinItems.isEmpty()) {
			skinItems.sort(new Skin());
		}
		
		ArrayList<String> itemText = new ArrayList<String>();
		int itemTextIndex = 0;
		String emote = "";
		for(Skin skin : skinItems) {
			if(!skin.getEffect().isEmpty()) {
				emote += ConfigHelper.getOptionFromFile("UNUSUAL_EMOTE");
			}
			String itemStr = "� " + emote + " " + skin.toDiscordString();
			if(itemText.isEmpty()) {
				itemText.add(itemStr);
			} else if(itemText.get(itemTextIndex).length()+ itemStr.length() >= 1024) {
				itemTextIndex++;
				itemText.add(itemStr);
			} else {
				itemText.set(itemTextIndex, itemText.get(itemTextIndex) + "\n" + itemStr);
			}
			emote = "";
		}
		
		for(Item item : noSkinItems) {
			if(Tour.australiumEmotes.containsKey(item.getName())) {
				emote = Tour.australiumEmotes.get(item.getName());
			}
			if(item.getKillstreakTier() == 3) {
				emote += ConfigHelper.getOptionFromFile("PRO_KS_EMOTE");
			}
			if(item.getEffect() != null) {
				emote += ConfigHelper.getOptionFromFile("UNUSUAL_EMOTE");
			} else if(item.getQuality().equals("Unusual") && item.getName().contains("Unusualifier")) {
				emote += ConfigHelper.getOptionFromFile("UNUSUALIFIER_EMOTE");
			}
			String itemStr = "� " + emote + " " + item.toDiscordString();
			if(itemText.isEmpty()) {
				itemText.add(itemStr);
			} else if(itemText.get(itemTextIndex).length()+ itemStr.length() >= 1024) {
				itemTextIndex++;
				itemText.add(itemStr);
			} else {
				itemText.set(itemTextIndex, itemText.get(itemTextIndex) + "\n" + itemStr);
			}
			emote = "";
		}
		//Make sure message is valid size.
		int i = 0;
		boolean modified = false;
		String modifiedTitle = "Could not display " + (items.size() - i) + " items.\n" + title;
		for(String s : itemText) {
			if(EB.length() + s.length() + modifiedTitle.length() + 4 < 6000) { //+4 for increased digits itemText.size might be
				EB.addField("", s, false);
				i += s.split("\n").length;
			} else {
				modified = true;
				break;
			}
		}
		if(modified) {
			EB.setTitle("Could not display " + (items.size() - i) + " items.\n" + title);
		} else {
			EB.setTitle(title);
		}
		return EB.build();
	}
	
	
	/**
	 * Return the arraylist of all leaderboard entries for the given event. List is sorted by value
	 * @param event "{tour OR overall} drystreak", "unusual drought"
	 * @return
	 */
	public static ArrayList<Leaderboard> getLeaderboard(String event) {
		ArrayList<Leaderboard> boards = new ArrayList<Leaderboard>();
		LeaderboardRepository LR = GambleBot.getContext().getBean(LeaderboardRepository.class);
		Optional<List<Leaderboard>> result = LR.findByEventOrderByValueAsc(event);
		if(result.isPresent()) {
			for(Leaderboard board : result.get()) {
				boards.add(board);
			}
		}
		return boards;
	}
	
	/**
	 * Attempt to add a board to database if it is a new top #MAX_BOARDS, delete bottom board if MAX_BOARDS has been reached
	 * @param board
	 * @return
	 */
	public static boolean updateLeaderboard(Leaderboard board) {
		String event = board.getEvent();
		ArrayList<Leaderboard> boards = getLeaderboard(event);
		
		//Empty board just add the event
		if(boards.isEmpty()) {
			board.save();
			return true;
		}
		
		if(boards.size() <= MAX_BOARDS) { //Can add board regardless
			board.save();
			return true;
		} else if(board.getValue() > boards.get(boards.size()-1).getValue()) {
			//Board value is higher than the lowest value and we need to remove the lowest one
			board.save();
			Leaderboard lowest = boards.get(0);
			//Select lowest one
			for(Leaderboard l : boards) {
				if(l.getValue() < lowest.getValue()) {
					lowest = l;
				} else if(l.getValue() == lowest.getValue()) { //Tie, set newest one
					if(l.getDate().compareTo(lowest.getDate()) > 0) {
						lowest = l;
					}
				}
			}
			lowest.delete();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the rank of the board relative to the event list
	 * @param board
	 * @return
	 */
	public int getLeaderboardRanking(Leaderboard board) {
		String event = board.getEvent();
		ArrayList<Leaderboard> boards = getLeaderboard(event);
		
		if(boards.isEmpty()) { //Shouldn't happen
			return -1;
		}
		
		int rank = 1;
		for(Leaderboard LB : boards) {
			if(LB.getValue() < board.getValue()) {
				rank++;
			} else if(LB.getValue() == board.getValue()) { //Tie, oldest one has rank priority
				if(LB.getDate().compareTo(board.getDate()) < 0) {
					rank++;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return rank;
	}
	
	/**
	 * Get the pricelist from backpack.tf and create the local file.
	 * @return false on error
	 */
	public static boolean updatePricelist() {
		try {
			URL url = new URL(PRICE_URL + ConfigHelper.getOptionFromFile("BACKPACK_API_KEY"));
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
		    httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
			Scanner sc = new Scanner(httpcon.getInputStream());
			StringBuffer sb = new StringBuffer();
			while(sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			
			String result = sb.toString();
			BufferedWriter writer = new BufferedWriter(new FileWriter(LOCAL_PRICELIST_FILE));
			writer.write(result);
			writer.close();
			
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @return JsonObject of the local pricelist
	 */
	public static JsonObject getLocalPriceList() {
		Gson gson = new Gson();
		JsonObject pricelist = new JsonObject();
		
		try {
			FileInputStream input = new FileInputStream(new File(LOCAL_PRICELIST_FILE));
			CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	        decoder.onMalformedInput(CodingErrorAction.IGNORE);
	        InputStreamReader reader = new InputStreamReader(input, decoder);
	        BufferedReader bufferedReader = new BufferedReader(reader);
			pricelist = gson.fromJson(bufferedReader, JsonObject.class).getAsJsonObject("response");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pricelist;
	}
	
	/**
	 * Take an arraylist of items and get the total value in keys. Returned as a json object with "value" and "items".
	 * @param items
	 * @return JsonObject with fields "value" and "items". 
	 * Note that items refers to how many Item objects had prices, not the quantity field of said objects.
	 */
	public static JsonObject getTotalItemsValue(ArrayList<Item> items) {
		double keys = 0.0;
		double metal = 0.0;
		int itemsPriced = 0;
		JsonObject prices = Utils.getLocalPriceList().get("items").getAsJsonObject();;
		
		double keyPrice = prices.get("Mann Co. Supply Crate Key").getAsJsonObject()
				.get("prices").getAsJsonObject()
				.get("6").getAsJsonObject()
				.get("Tradable").getAsJsonObject()
				.get("Craftable").getAsJsonArray()
				.get(0).getAsJsonObject()
				.get("value").getAsDouble();
		double hatPrice = prices.get("Random Craft Hat").getAsJsonObject()
				.get("prices").getAsJsonObject()
				.get("6").getAsJsonObject()
				.get("Tradable").getAsJsonObject()
				.get("Craftable").getAsJsonArray()
				.get(0).getAsJsonObject()
				.get("value").getAsDouble();
		
		for(Item item : items) {
			if(QUALITY_MAP.containsKey(item.getQuality())) {
				try {
					JsonObject itemObj = null;
					if(item.getQuality().equals("Unusual")) {
						if(UNUSUAL_EFFECT_MAP.containsKey(item.getEffect())) {
							itemObj = prices.get(item.getName()).getAsJsonObject()
									.get("prices").getAsJsonObject()
									.get(QUALITY_MAP.get(item.getQuality()).toString()).getAsJsonObject()
									.get("Tradable").getAsJsonObject()
									.get(item.isCraftable() ? "Craftable" : "Non-Craftable").getAsJsonObject()
									.get(UNUSUAL_EFFECT_MAP.get(item.getEffect()).toString()).getAsJsonObject();
							
						} else {
							//log.warn("Effect \"" + item.getEffect() +"\" is not mapped!");
							continue;
						}
					} else {
						itemObj = prices.get(item.getName()).getAsJsonObject()
								.get("prices").getAsJsonObject()
								.get(QUALITY_MAP.get(item.getQuality()).toString()).getAsJsonObject()
								.get("Tradable").getAsJsonObject()
								.get(item.isCraftable() ? "Craftable" : "Non-Craftable").getAsJsonArray()
								.get(0).getAsJsonObject();
					}
					if(itemObj.get("currency").getAsString().equals("metal")) {
						metal += itemObj.get("value").getAsDouble() * (double) item.getQuantity();
						itemsPriced++;
					} else if(itemObj.get("currency").getAsString().equals("keys")) {
						keys += itemObj.get("value").getAsDouble() * (double) item.getQuantity();
						itemsPriced++;
					} else if(itemObj.get("currency").getAsString().equals("hat")) {
						metal += itemObj.get("value").getAsDouble() * hatPrice * (double) item.getQuantity();
						itemsPriced++;
					} else {
						log.warn("Could not find price for: " + item.toString());
					}
				} catch(NullPointerException e) {
					if(item.getLevel() != 99) { //Ignore paints/skins
						//log.warn("(NULL) Could not find price for: " + item.toString());
					}
				} catch(Exception e) {
					log.error("Unexpected error while fetching prices.");
					e.printStackTrace();
				}
			} else {
				log.warn("Missing quality in map for item: " + item.toString());
			}
		}
		/*
		log.warn("Metal: " + metal);
		log.warn("Keys: " + keys);
		log.warn("Metal to keys: " + (metal/keyPrice)); */
		
		keys += metal/keyPrice;
		JsonObject out = new JsonObject();
		out.addProperty("value", keys);
		out.addProperty("items", itemsPriced);
		
		return out;
	}

}
