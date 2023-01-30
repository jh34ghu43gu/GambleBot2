package files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import ch.qos.logback.classic.Logger;

/**
 * A class to deal with the tf2 item schema.
 * @author jh34ghu43gu
 */
public class SchemaHelper {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(SchemaHelper.class);
	private static String TEMP_FILE_NAME = "schemaTemp";
	
	/**
	 * Turn the tf2 schema into a .json readable file.
	 * @param filename	Schema file name
	 * @param newFileName	The output json file name including .json extension
	 */
	public static void fixSchema(String filename, String newFileName) {
		log.debug("Attempting to fix schema file " + filename);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			JsonReader jsonReader = new JsonReader(new FileReader(filename));
			JsonObject obj = gson.fromJson(jsonReader, JsonObject.class);
			jsonReader.close();
			obj.toString();
			log.debug("Can already read schema.");
		} catch(Exception e) {
			log.debug(e.getMessage());
			log.debug("Could not read schema, applying fix...");
			File temp = null;
			try {
				//I hate this very much
				
				String old = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
				old = old.replaceAll("\\\\", "/");
				String noTab ="{\n" + old.replaceAll("\t", " ");
				String noDoubleSpace = noTab.replaceAll("(  +)", " ") + "}";
				String attributeFix = noDoubleSpace.replaceAll("\" \"", "\":\"");
				attributeFix = attributeFix.replaceAll("([^:])\"\"", "$1\":\"");
				//This is because regex hates me and cannot tell if something comes after a newline "\"\n *\\{"
				String noNewLines = attributeFix.replaceAll(System.getProperty("line.separator"), "NEWLINE");
				
				noNewLines = noNewLines.replaceAll(" *NEWLINE( *NEWLINE)+", "NEWLINE");
				noNewLines = noNewLines.replaceAll(" +NEWLINE", "NEWLINE");
				String newLineFix = noNewLines.replaceAll("\"NEWLINE *\\{", "\":NEWLINE{");
				newLineFix = newLineFix.replaceAll("\" *NEWLINE *\"", "\",NEWLINE\"");
				String closingBracketFix = newLineFix.replaceAll("\\} *NEWLINE *(\"[a-zA-Z0-9_ \\-:\\.]*\":)", "},NEWLINE $1");
				String revertNEWLINE = closingBracketFix.replaceAll("NEWLINE", "\n");
				
				
				//Convert into a temp file, if anything goes wrong in future schema conversions it can be troubleshooted.
				temp = File.createTempFile(TEMP_FILE_NAME, ".json");
				log.debug("Created temp file: " + temp.toString());
				BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
				bw.write(revertNEWLINE);
				bw.close();
				
				//Convert back into a pretty json file if it's good.
				FileReader reader = new FileReader(temp);
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				String s = gson.toJson(obj);
				FileWriter writer = new FileWriter(newFileName);
				writer.write(s);
				writer.flush();
				writer.close();
				reader.close();
				
				log.debug("Finished converting " + filename + " into the json file: " + newFileName);
			} catch(Exception e2) {
				log.error("Something else went wrong applying the fix");
				log.error(e2.getMessage());
			} finally {
				if(!temp.delete()) {
					log.warn("Failed to delete temp file");
				}
			}
		}
	}
	
	/**
	 * @param filename	Name of the json readable item schema file
	 * @return	a hashmap of item qualities and their int representation, empty map if error reading file.
	 */
	public static HashMap<String, Integer> getItemQualities(String filename) {
		log.debug("Attempting to get item qualities from schema.");
		Gson gson = new Gson();
		HashMap<String, Integer> out = new HashMap<String, Integer>();
		try {
			JsonReader jsonReader = new JsonReader(new FileReader(filename));
			JsonObject obj = gson.fromJson(jsonReader, JsonObject.class);
			JsonObject qualities = obj.get("items_game").getAsJsonObject().get("qualities").getAsJsonObject();
			for(Entry<String, JsonElement> entry : qualities.entrySet()) {
				out.put(entry.getKey(), entry.getValue().getAsJsonObject().get("value").getAsInt());
			}
		} catch(Exception e) {
			log.error("Problem getting item qualities from schema, returning empty map.");
			log.error(e.getMessage());
		}
		return out;
	}
	
	
	/**
	 * Match the schema requirements for craft hats and generate a list of them. Size also included at the bottom.
	 */
	public static void getHatsFromSchema() {
		String schemaFile = "itemSchema.json";
		String hatFileName = "craft hats list.txt";
		Gson gson = new Gson();
		try {
			JsonReader jsonReader = new JsonReader(new FileReader(schemaFile));
			JsonObject obj = gson.fromJson(jsonReader, JsonObject.class);
			
			//Figure out which prefabs we can and can't use
			JsonObject prefabs = obj.get("items_game").getAsJsonObject().get("prefabs").getAsJsonObject();
			ArrayList<String> blacklistPrefabs = new ArrayList<String>();
			ArrayList<String> whitelistPrefabs = new ArrayList<String>() {{
				this.add("hat");
				this.add("misc");
				this.add("grenades");
				this.add("hat_decoration");
				this.add("mask");
				this.add("beard");
				this.add("backpack");
			}};
			for(Entry<String, JsonElement> fab : prefabs.entrySet()) {
				if(fab.getValue().getAsJsonObject().has("craft_class")
						&& fab.getValue().getAsJsonObject().get("craft_class").getAsString().equals("")) {
					blacklistPrefabs.add(fab.getKey());
				}
				if(fab.getValue().getAsJsonObject().has("craft_class")
						&& fab.getValue().getAsJsonObject().get("craft_class").getAsString().equals("hat")) {
					whitelistPrefabs.add(fab.getKey());
				}
			}
			
			JsonObject items = obj.get("items_game").getAsJsonObject().get("items").getAsJsonObject();
			ArrayList<String> hats = new ArrayList<String>();
			for(Entry<String, JsonElement> item : items.entrySet()) {
				boolean canCraft = false;
				if(item.getValue().getAsJsonObject().has("craft_class")) {
					String[] val = item.getValue().getAsJsonObject().get("craft_class").getAsString().split(" ");
					for(String s : val) {
						if(s.equalsIgnoreCase("hat")) {
							canCraft = true;
						}
					}
				} else if(item.getValue().getAsJsonObject().has("prefab")) {
					if(item.getValue().getAsJsonObject().has("craft_class")
							&& item.getValue().getAsJsonObject().get("craft_class").getAsString().equalsIgnoreCase("")) {
						//no
					} else {
						String[] val = item.getValue().getAsJsonObject().get("prefab").getAsString().split(" ");
						for(String s : val) {
							if(whitelistPrefabs.contains(s)) {
								canCraft = true;
							}
							if(blacklistPrefabs.contains(s)) {
								canCraft = false;
								break;
							}
						}
					}
				}
				if(canCraft) {
					String hatName = "";
					if(item.getValue().getAsJsonObject().has("item_name")) {
						hatName = item.getValue().getAsJsonObject().get("item_name").getAsString();
					} else {
						hatName = item.getValue().getAsJsonObject().get("name").getAsString();
					}
					hats.add(hatName);
				}
			}
			jsonReader.close();
			
			//Get non-translated first in case someone wants to translate their own.
			FileWriter writer = new FileWriter(hatFileName);
			String out = "";
			for(String s : hats) {
				out += "\"" + s + "\",\n";
			}
			writer.write(out);
			writer.flush();
			writer.close();
			System.out.println("Wrote craft hat list of size " + hats.size() + " to file " + hatFileName);
			
			//Translate to english
			String englishJson = "tfEnglish.json";
			jsonReader = new JsonReader(new FileReader(englishJson));
			JsonObject english = gson.fromJson(jsonReader, JsonObject.class);
			
			HashMap<String, String> exceptions = new HashMap<String, String>() {{ //Some casing mismatches between lang and schema
				this.put("#TF_CrocLeather_Slouch", "Crocleather Slouch");
				this.put("#TF_VoodooJuju", "Voodoo JuJu (Slight Return)");
				this.put("#TF_fall2013_popeyes", "Pop-Eyes");
				//Remove "The" for our purposes
				this.put("The Triad Trinket", "Triad Trinket");
				this.put("The Champ Stamp", "Champ Stamp");
				this.put("The Marxman", "Marxman");
				this.put("The Human Cannonball", "Human Cannonball");
				this.put("#TF_MNC_Hat", "Athletic Supporter");
				this.put("#TF_MNC_Mascot_Hat", "Superfan");
			}};
			writer = new FileWriter("ENGLISH " + hatFileName);
			out = "";
			for(String s : hats) {
				if(s.startsWith("#") || exceptions.containsKey(s)) {
					if(exceptions.containsKey(s)) {
						out += "\"" + exceptions.get(s) + "\",\n";
						continue;
					} else if(english.has(s.substring(1))) {
						out += "\"" + english.get(s.substring(1)).getAsString() + "\",\n";
						continue;
					} else {
						log.warn(s.substring(1) + " not found.");
					}
				}
				out += "\"" + s + "\",\n";
			}
			writer.write(out);
			writer.flush();
			writer.close();
			System.out.println("Wrote craft hat list of size " + hats.size() + " to file ENGLISH " + hatFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	/* JS for running on https://wiki.teamfortress.com/wiki/Random_crafting_recipes to compare lists
	 	//td valign="top"
		var els = document.querySelectorAll(`[valign="top"]`);
		var names = "";
		var i = 0;
		els.forEach(el => {
		  var name = el.children[0].innerHTML;
		  if(!names.includes(name)) {
		    names += name + "\n";
		  	i++;
		  }
		});
		console.log(names);
		console.log(i);
	 */
	}

}
