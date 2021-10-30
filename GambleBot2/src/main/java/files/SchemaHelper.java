package files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

}
