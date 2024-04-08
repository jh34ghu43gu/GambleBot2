package discord.commands;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/**
 * Command to get avg. price breakdown of non-case effects based on the current hat pool.
 */
public class GenerationValueReportCommand extends Command {

	private static final Logger log = (Logger) LoggerFactory.getLogger(GenerationValueReportCommand.class);
	
	public GenerationValueReportCommand() {
		this.name = "genreport";
		this.aliases = new String[] {"gr"};
		this.arguments = "<generation (1,2,3,robo,eotl,spooky,spectral,scary,VI)>";
		this.help = "Get a report of average effect prices on unboxable hats per generation."; 
		this.cooldown = 10;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String effect = "";
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			for(String s : args) {
				log.debug("Arg: " + s);
			}
			String tmp = args[0].toLowerCase();
			switch(tmp) {
			case "1":
				effect = "1";
				break;
			case "2":
				effect = "2";
				break;
			case "3":
				effect = "3";
				break;
			case "eotl":
				effect = "EOTL";
				break;
			case "spooky":
				effect = "SPOOKY";
				break;
			case "spectral":
				effect = "SPECTRAL";
				break;
			case "robo":
				effect = "ROBO";
				break;
			case "scary":
				effect = "SCARY";
				break;
			case "vi":
				effect = "VI";
				break;
			}
		}
		
		if(effect.isEmpty()) {
			event.reply("Please select a valid generation: 1, 2, 3, eotl, robo, spooky, spectral, scary, VI");
			return;
		}
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			InputStream hatStream = Tour.class.getClassLoader().getResourceAsStream(Crate.HAT_FILE);
			JsonObject obj = gson.fromJson(new InputStreamReader(hatStream, "UTF-8"), JsonObject.class);
			JsonArray hats = obj.get("hats").getAsJsonArray();
			hatStream.close();
			
			InputStream crateStream = Tour.class.getClassLoader().getResourceAsStream(Crate.CRATE_FILE);
			obj = gson.fromJson(new InputStreamReader(crateStream, "UTF-8"), JsonObject.class);
			JsonObject effects = obj.get("effects").getAsJsonObject();
			crateStream.close();
			
			JsonObject priceList = Utils.getLocalPriceList().get("items").getAsJsonObject();
			
			JsonArray effectNames = effects.get(effect).getAsJsonArray();
			JsonArray effectIds = effects.get(effect+"_ids").getAsJsonArray();

			//Create an id:name map and fill the tracking map with names and 0 value
			LinkedHashMap<String, ArrayList<Double>> medianValuesMap = new LinkedHashMap<String, ArrayList<Double>>();
			LinkedHashMap<String, Double> medianMap = new LinkedHashMap<String, Double>(); //Using for medians later
			LinkedHashMap<String, Double> valueMap = new LinkedHashMap<String, Double>();
			LinkedHashMap<String, Double> amtMap = new LinkedHashMap<String, Double>(); //Use this to get our avgs later
			HashMap<String, String> effectIdToName = new HashMap<String, String>();
			for(int i = 0; i < effectIds.size(); i++) {
				effectIdToName.put(effectIds.get(i).getAsString(), effectNames.get(i).getAsString());
				valueMap.put(effectNames.get(i).getAsString(), 0.0);
				amtMap.put(effectNames.get(i).getAsString(), 0.0);
				medianValuesMap.put(effectNames.get(i).getAsString(), new ArrayList<Double>());
				medianMap.put(effectNames.get(i).getAsString(), 0.0);
			}
			
			//Actually get prices now
			for(JsonElement hatElement : hats) {
				if(priceList.has(hatElement.getAsString())) {
					JsonObject hat = priceList.get(hatElement.getAsString()).getAsJsonObject();
					if(hat.has("prices")) {
						hat = hat.get("prices").getAsJsonObject();
						if(hat.has("5")) {
							hat = hat.get("5").getAsJsonObject(); //5 is unusual
							if(hat.has("Tradable")) {
								hat = hat.get("Tradable").getAsJsonObject();
								if(hat.has("Craftable")) {
									hat = hat.get("Craftable").getAsJsonObject();
									for(JsonElement effectId : effectIds) {
										if(hat.has(effectId.getAsString())) {
											//System.out.println(hatElement.getAsString() + " " + effectId.toString() + "\n" + hat.toString());
											String effectName = effectIdToName.get(effectId.getAsString());
											double value = valueMap.get(effectName);
											double tmpValue = 0.0;
											JsonObject tmpHat = hat.get(effectId.getAsString()).getAsJsonObject();
											if(hat.has("value_high")) {
												tmpValue = tmpHat.get("value").getAsDouble() + tmpHat.get("value_high").getAsDouble();
												tmpValue = Math.floor((tmpValue/2)*100.0)/100.0;
											} else {
												tmpValue = tmpHat.get("value").getAsDouble();
											}
											value += tmpValue;
											valueMap.put(effectName, value);
											amtMap.put(effectName, amtMap.get(effectName)+1);
											medianValuesMap.get(effectName).add(tmpValue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			//Do the averages and medians
			for(JsonElement nameElement : effectNames) {
				String name = nameElement.getAsString();
				double tmpValue = valueMap.get(name);
				tmpValue = Math.floor((tmpValue/amtMap.get(name))*1000.0)/1000.0;
				valueMap.put(name, tmpValue);
				
				Collections.sort(medianValuesMap.get(name));
				medianMap.put(name, medianValuesMap.get(name).get(medianValuesMap.get(name).size()/2));
			}
			
			//Sort
			LinkedHashMap<String, Double> sorted = (LinkedHashMap<String, Double>) sortByValue(valueMap);
			DecimalFormat twoDec = new DecimalFormat("###,###.##");
			String out = "**Effect price report for generation: " + effect + "**\n";
			Object[] keys = sorted.keySet().toArray();
			double total = 0.0;
			for(int i = keys.length-1; i >= 0; i--) {
				out += keys[i] + ": Avg " + twoDec.format(sorted.get(keys[i]))
						+ " | Median " + twoDec.format(medianMap.get(keys[i])) + " keys\n";
				total += sorted.get(keys[i]);
			};
			total = total/keys.length;
			out += "**Average for the whole generation: " + twoDec.format(total) + "**";
			event.reply(out);
			
		} catch(Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		
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

}
