package model;

import java.util.Random;

/**
 * Simple class to hold a crate item. Quality is item grade for case items.
 * @author jh34ghu43gu
 */
public class CrateItem {
	
	private String name;
	private String quality;
	private double weight;
	private boolean hat;
	
	/**
	 * @param name
	 * @param quality
	 * @param weight
	 * @param hat
	 */
	public CrateItem(String name, String quality, double weight, boolean hat) {
		super();
		this.name = name;
		this.quality = quality;
		this.weight = weight;
		this.hat = hat;
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
	 * @return the quality
	 */
	public String getQuality() {
		if(quality.equals("StrangeUnique10")) {
			Random rand = new Random();
			if(rand.nextDouble() <= 0.1) {
				return "Strange";
			} else {
				return "Unique";
			}
		}
		return quality;
	}

	/**
	 * @param quality the quality to set
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @return the hat
	 */
	public boolean isHat() {
		return hat;
	}

	/**
	 * @param hat the hat to set
	 */
	public void setHat(boolean hat) {
		this.hat = hat;
	}
	
	@Override
	public String toString() {
		return quality + " " + name + "\t\tHat: " + hat + " Weight: " + weight;
	}

}
