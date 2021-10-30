package files;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class ConfigHelper {
	
	private static final String FILE_NAME = "config.json";
	private static final Logger log = (Logger) LoggerFactory.getLogger(ConfigHelper.class);

	/**
	 * @return	if config.json is found
	 */
	public static boolean exists() {
		File config = new File(FILE_NAME);
		return(config.exists());
	}
	
	/**
	 * Create an empty config file
	 */
	public static void buildEmptyConfig() {
		if(exists()) { return; } //Do not overwrite an existing config
		JsonObject configObj = new JsonObject();
		configObj.add("Options", new JsonObject());
		try {
			log.debug("Attempting to create empty config file.");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String s = gson.toJson(configObj);
			FileWriter writer = new FileWriter(FILE_NAME);
			writer.write(s);
			writer.flush();
			writer.close();
			log.debug("Succesfully created empty config file.");
		} catch (Exception e) {
			log.error("Error occured writing config file.\n" + e.toString());
			log.debug(e.getMessage(), e);
		}
	}

	/**
	 * Pull an option from config.json
	 * @param	option
	 * @return	the option's value or blank if not found, file doesn't exist, or error.
	 */
	public static String getOptionFromFile(String option) {
		String out = "";
		if(!exists()) { return out; }
		log.debug("Attempting to get option \"" + option + "\" from config.");
		Gson gson = new Gson();

		try(FileReader reader = new FileReader(FILE_NAME)) {
			JsonObject options = gson.fromJson(reader, JsonObject.class).getAsJsonObject("Options");
			if(options.get(option) != null) {
				out = options.get(option).getAsString();
				log.debug("Found option \"" + option + "\" with value: " + out);
				return out;
			} else {
				log.warn("Could not find option \"" + option + "\" in config file.");
			}
		} catch(Exception e) {
			log.error("Error finding option \"" + option + "\" in config file.\n" + e.toString());
			log.debug(e.getMessage(), e);
		}
		return out;
	}

	/**
	 * Set an option/value pair to the config
	 * @param option
	 * @param value
	 * @param overwrite Should the option's current value be overwritten if it exists?
	 * @return False if the file does not exist or there was an error. True if successful or not overwriting.
	 */
	public static boolean setOptionToFile(String option, String value, boolean overwrite) {
		if(!exists()) { return false; }
		log.debug("Attempting to set option \"" + option + "\" with value \"" + value + "\" to config.");
		Gson gson = new Gson();
		try(FileReader reader = new FileReader(FILE_NAME)) {
			JsonObject configObj = gson.fromJson(reader, JsonObject.class);
			JsonObject options = configObj.getAsJsonObject("Options");
			if(options.get(option) != null) {
				if(overwrite) {
					options.addProperty(option, value);
				} else {
					log.debug("Option already exists, not overwriting.");
					return true;
				}
			} else {
				options.addProperty(option, value);
			}
			gson = new GsonBuilder().setPrettyPrinting().create();
			String s = gson.toJson(configObj);
			FileWriter writer = new FileWriter(FILE_NAME);
			writer.write(s);
			writer.flush();
			writer.close();
			log.debug("Succesfully set option \"" + option + "\" to config file.");
			return true;
		} catch(Exception e) {
			log.error("Error setting option \"" + option + "\" to config file.\n" + e.toString());
			log.debug(e.getMessage(), e);
			return false;
		}
	}
}
