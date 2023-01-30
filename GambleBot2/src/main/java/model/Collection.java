package model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class Collection {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Collection.class);
	
	public static final String CASE_FILE = Crate.CASE_FILE;
	
	private String name;
	private int associatedCase;
	private boolean skins;
	private ArrayList<String> civilian;
	private ArrayList<String> freelance;
	private ArrayList<String> mercenary;
	private ArrayList<String> commando;
	private ArrayList<String> assassin;
	private ArrayList<String> elite;
	private ArrayList<String>[] grades;
	
	
	public Collection(String name, boolean skins) {
		super();
		this.name = name;
		this.skins = skins;
		civilian = new ArrayList<String>();
		freelance = new ArrayList<String>();
		mercenary = new ArrayList<String>();
		commando = new ArrayList<String>();
		assassin = new ArrayList<String>();
		elite = new ArrayList<String>();
		grades = new ArrayList[] {civilian, freelance, mercenary, commando, assassin, elite};
	}

	public Collection(String name, int associatedCase, boolean skins) {
		super();
		this.name = name;
		this.associatedCase = associatedCase;
		this.skins = skins;
		civilian = new ArrayList<String>();
		freelance = new ArrayList<String>();
		mercenary = new ArrayList<String>();
		commando = new ArrayList<String>();
		assassin = new ArrayList<String>();
		elite = new ArrayList<String>();
		grades = new ArrayList[] {civilian, freelance, mercenary, commando, assassin, elite};
	}
	
	/**
	 * @return Fetch all the collections from file and return as a list
	 */
	public static ArrayList<Collection> getCollections() {
		ArrayList<Collection> collections = new ArrayList<Collection>();
		InputStream is = Tour.class.getClassLoader().getResourceAsStream(CASE_FILE);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			JsonObject obj = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);			
			
			for(Entry<String, JsonElement> caseElement : obj.entrySet()) {
				if(caseElement.getKey().equals("GlobalEffects") || caseElement.getKey().equals("GlobalBonus")) { continue; } //Not a case
				boolean skin = false;
				if(caseElement.getValue().getAsJsonObject().has("collection")) {
					skin = true;
					if(!caseElement.getValue().getAsJsonObject().get("collection").getAsBoolean()) { 
						continue;
					}
				}
				
				
				JsonObject caseObj = caseElement.getValue().getAsJsonObject();
				String name = caseElement.getKey();
				skin = name.contains("Weapons Case") || name.contains("War Paint Case");
				int num = -1;
				if(caseObj.has("number")) {
					num = caseObj.get("number").getAsInt();
				}
				Collection c = new Collection(name, num, skin);

				//Get all the item names
				for(String grade : Crate.rarities) {
					grade = grade.toLowerCase();
					for(JsonElement s : caseObj.get(grade).getAsJsonArray()) {
						c.addToGrade(s.getAsString(), grade);
					}
				}
				collections.add(c);
			}
		} catch(Exception e) {
			log.error("Something went wrong getting collections.");
			e.printStackTrace();
		}
		return collections;
	}
	
	
	
	/**
	 * @return the highest grade in the collection. Crate.rarities[i] for string version
	 */
	public int highestGrade() {
		for(int i = grades.length-1; i >= 0; i--) {
			if(grades[i].size() > 0) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Single method to add an name to a grade
	 * @param s
	 * @param grade
	 */
	public void addToGrade(String s, String grade) {
		if(grade.equalsIgnoreCase("civilian")) {
			addCivilian(s);
		} else if(grade.equalsIgnoreCase("freelance")) {
			addFreelance(s);
		} else if(grade.equalsIgnoreCase("mercenary")) {
			addMercenary(s);
		} else if(grade.equalsIgnoreCase("commando")) {
			addCommando(s);
		} else if(grade.equalsIgnoreCase("assassin")) {
			addAssassin(s);
		} else if(grade.equalsIgnoreCase("elite")) {
			addElite(s);
		}
	}
	
	/*
	 * Methods for adding to the lists
	 */
	
	public void addCivilian(String s) {
		civilian.add(s);
	}
	public void addFreelance(String s) {
		freelance.add(s);
	}
	public void addMercenary(String s) {
		mercenary.add(s);
	}
	public void addCommando(String s) {
		commando.add(s);
	}
	public void addAssassin(String s) {
		assassin.add(s);
	}
	public void addElite(String s) {
		elite.add(s);
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
	 * @return the associatedCase
	 */
	public int getAssociatedCase() {
		return associatedCase;
	}

	/**
	 * @param associatedCase the associatedCase to set
	 */
	public void setAssociatedCase(int associatedCase) {
		this.associatedCase = associatedCase;
	}

	/**
	 * @return the skins
	 */
	public boolean isSkins() {
		return skins;
	}

	/**
	 * @param skins the skins to set
	 */
	public void setSkins(boolean skins) {
		this.skins = skins;
	}

	/**
	 * @return the civilian
	 */
	public ArrayList<String> getcivilian() {
		return civilian;
	}

	/**
	 * @param civilian the civilian to set
	 */
	public void setcivilian(ArrayList<String> civilian) {
		this.civilian = civilian;
	}

	/**
	 * @return the freelance
	 */
	public ArrayList<String> getFreelance() {
		return freelance;
	}

	/**
	 * @param freelance the freelance to set
	 */
	public void setFreelance(ArrayList<String> freelance) {
		this.freelance = freelance;
	}

	/**
	 * @return the mercenary
	 */
	public ArrayList<String> getMercenary() {
		return mercenary;
	}

	/**
	 * @param mercenary the mercenary to set
	 */
	public void setMercenary(ArrayList<String> mercenary) {
		this.mercenary = mercenary;
	}

	/**
	 * @return the commando
	 */
	public ArrayList<String> getCommando() {
		return commando;
	}

	/**
	 * @param commando the commando to set
	 */
	public void setCommando(ArrayList<String> commando) {
		this.commando = commando;
	}

	/**
	 * @return the assassin
	 */
	public ArrayList<String> getAssassin() {
		return assassin;
	}

	/**
	 * @param assassin the assassin to set
	 */
	public void setAssassin(ArrayList<String> assassin) {
		this.assassin = assassin;
	}

	/**
	 * @return the elite
	 */
	public ArrayList<String> getElite() {
		return elite;
	}

	/**
	 * @param elite the elite to set
	 */
	public void setElite(ArrayList<String> elite) {
		this.elite = elite;
	}

	/**
	 * @return the grades
	 */
	public ArrayList<String>[] getGrades() {
		return grades;
	}

	/**
	 * @param grades the grades to set
	 */
	public void setGrades(ArrayList<String>[] grades) {
		this.grades = grades;
	}
	
	

}
