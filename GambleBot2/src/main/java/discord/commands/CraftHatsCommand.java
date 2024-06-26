package discord.commands;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;
import ch.qos.logback.classic.Logger;
import model.Crate;
import model.Tour;

public class CraftHatsCommand extends Command {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CraftHatsCommand.class);
	private double craftCost = 3.0;
	private int maxCraft = 100000;
	private int maxAll = 1000;
	private HashMap<String, Double> hats;
	
	
	public CraftHatsCommand() {
		this.name = "crafthats";
		this.aliases = new String[] {"ch"};
		this.arguments = "<#||stats||all <#>>";
		this.help = "Craft <1-" + maxCraft + "> hats and see if you made profit."
				+ "\nOr craft stats for a list of the 50 most valuable hats."
				+ "\nOr craft all to attempt to craft every craftable hat. "
					+ "Says how many crafts it took and the starting amount of hats required. "
					+ "Optional aggregate attempts up to " + maxAll + " attempts.";
		this.cooldown = 5;
		this.hats = new HashMap<String, Double>();
		updateHats();
	}
	
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "misc")) {
			event.getMessage().delete().queue();
			return;
		}
		int amt = 1;
		boolean stats = false;
		boolean all = false;
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			for(String s : args) {
				log.debug("Arg: " + s);
			}
			if(args[0].equalsIgnoreCase("stats")) {
				stats = true;
			} else if(args[0].equalsIgnoreCase("all")) {
				all = true;
				if(args.length > 1) {
					try {
						amt = Integer.parseInt(args[1]);
						if(amt > maxAll) {
							amt = maxAll;
						}
						if(amt <= 0) {
							amt = 1;
						}
					} catch(Exception e) {
						//just do 1
					}
				}
			} else {
				try {
					amt = Integer.parseInt(args[0]);
					if(amt > maxCraft) {
						amt = maxCraft;
					}
					if(amt <= 0) {
						amt = 1;
					}
				} catch(Exception e) {
					//just do 1
				}
			}
		}
		
		if(stats) {
			LinkedHashMap<String, Double> sorted = (LinkedHashMap<String, Double>) sortByValue(hats);
			String out = "Top 50 valued hats:\n";
			int counter = 1;
			Object[] keys = sorted.keySet().toArray();
			for(int i = keys.length-1; i > keys.length-51; i--) {
				out += counter + ") " + keys[i] + " : " + sorted.get(keys[i]) + "ref\n";
				counter++;
			};
			out += "Total hats in craft pool: " + sorted.size();
			event.reply(out);
			return;
		}
		
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		if(all) {
			double totalCraftsDone = 0;
			double totalExcessHats = 0;
			double totalHatsRequired = 0;
			for(int i = 0; i < amt; i++) {
				int craftsDone = 0;
				int excessHats = 0;
				int hatsRequired = 0;
				ArrayList<String> hatsCrafted = new ArrayList<String>();
				
				Random rand = new Random();
				String[] set = hats.keySet().toArray(new String[0]);
				
				while(hatsCrafted.size() < hats.size()) {
					craftsDone++;
					if(excessHats >= 2) {
						excessHats -= 2;
					} else if(excessHats == 1) {
						hatsRequired++;
						excessHats--;
					} else {
						hatsRequired += 2;
					}
					
					String hat = set[rand.nextInt(hats.size())];
					if(hatsCrafted.contains(hat)) {
						excessHats++;
					} else {
						hatsCrafted.add(hat);
					}
				}
				if(amt == 1) {
					String out = "Crafted all " + hatsCrafted.size() + " hats with " + excessHats + " extra hats."
							+ "\nTook " + craftsDone + " crafts to do and required purchasing " + hatsRequired + " hats.";
					event.reply(out);
					return;
				} else {
					totalCraftsDone += craftsDone;
					totalExcessHats += excessHats;
					totalHatsRequired += hatsRequired;
				}
			}
			
			String out = "Crafted all " + hats.size() + " hats " + amt + " times."
					+ "\nAverage crafts done: " + twoDec.format(totalCraftsDone/amt)
					+ "\nAverage hats required: " + twoDec.format(totalHatsRequired/amt)
					+ "\nAverage extra hats: " + twoDec.format(totalExcessHats/amt);
			event.reply(out);
			return;
		}
		
		Random rand = new Random();
		double value = 0.0;
		int badHats = 0;
		int twoHats = 0;
		int profitHats = 0;
		double profitHatVal = 0.0;
		double[] set = hats.values().stream().mapToDouble(Double::doubleValue).toArray();
		for(int i = 0; i < amt; i++) {
			double val = set[rand.nextInt(hats.size())];
			value += val;
			if(val >= craftCost) {
				profitHats++;
				profitHatVal += val;
			} else if (val >= 2.0) {
				twoHats++;
			} else {
				badHats++;
			}
		}
		
		int recursiveCraftAmt = recursiveCraft(profitHatVal, 0);
		int totalWithRecursive = amt - profitHats + recursiveCraftAmt;
		double cost = amt*craftCost;
		double percent = (value-cost)/cost*100.0;
		
		
		String profit = "";
		if(value - cost > 0) {
			profit = twoDec.format(value-cost) + "";
		} else {
			profit = "(" + twoDec.format(value-cost) + ")";
		}
		event.reply("You crafted " + twoDec.format(amt) + " hats.\n"
						+ "Total cost: " + twoDec.format(cost) + " refined.\n"
						+ "Total value: " + twoDec.format(value) + " refined.\n"
						+ "Total profit: " + profit + " refined. (" + twoDec.format(percent) + "%)\n\n"
						+ profitHats + " hats were over " + craftCost + " ref. (Total value of " + twoDec.format(profitHatVal) + " ref)\n"
						+ twoHats + " hats were between 2 and 3 ref. (3>hat>=2)\n"
						+ badHats + " hats were under 2 ref.\n"
						+ "You were able to craft an additional " + twoDec.format(recursiveCraftAmt)
						+ " hats by selling any profit hats for a total of " + twoDec.format(totalWithRecursive));
	}
	
	private int recursiveCraft(double ref, int hats) {
		if(ref < craftCost) {
			return hats;
		}
		int craftAmt = (int) Math.floor(ref/craftCost);
		double profitRef = 0.0;
		
		Random rand = new Random();
		double[] set = this.hats.values().stream().mapToDouble(Double::doubleValue).toArray();
		for(int i = 0; i < craftAmt; i++) {
			double val = set[rand.nextInt(this.hats.size())];
			if(val > craftCost) {
				profitRef += val;
			} else {
				hats++;
			}
		}
		return recursiveCraft(profitRef, hats);
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
	
	private void updateHats() {
		hats.clear();
		InputStream is = Tour.class.getClassLoader().getResourceAsStream(Crate.HAT_FILE);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			JsonObject obj = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			JsonArray hats = obj.get("craft_hats").getAsJsonArray();
			
			JsonObject priceList = Utils.getLocalPriceList().get("items").getAsJsonObject();
			
			
			for(JsonElement hat : hats) {
				String name = hat.getAsString();
				double value = 0.0;
				if(priceList.has(name)) {
					try {
						JsonObject hatObj = priceList.get(name).getAsJsonObject()
								.get("prices").getAsJsonObject()
								.get("6").getAsJsonObject() //Unique quality = 6
								.get("Tradable").getAsJsonObject()
								.get("Craftable").getAsJsonArray()
								.get(0).getAsJsonObject();
						if(hatObj.has("value_high")) {
							value = hatObj.get("value").getAsDouble() + hatObj.get("value_high").getAsDouble();
							value = Math.floor((value/2)*100.0)/100.0;
						} else {
							value = hatObj.get("value").getAsDouble();
						}
					} catch(NullPointerException e) {
						log.error("NullPointer for hat: " + name);
						e.printStackTrace();
						continue;
					}
				} else {
					log.warn("NO PRICE FOUND FOR: " + name + ", SKIPPING.");
					continue;
				}
				this.hats.put(name, value);
			}
			//Add the 3 hats that I am CBA on fixing UTF encoding problems
			this.hats.put("Detective Noir", 1.22);
			this.hats.put("Brutal Bouffant", 1.22);
			this.hats.put("Ze Ubermensch", 1.22);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
