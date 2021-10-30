package model;

import java.util.ArrayList;

import beans.Item;

/**
 * A class to help with item input logic
 * <p>
 * Inputs are stored as a 30 bit number in an int, converting to binary the breakdown is as follows<br>
 * 00 00000 00000 00000 000 000 000 00 00<br>
 * The 1st group specifies the amount of ks items left, the type of ks can be determined from the ks tier field (should be passed in from an Item) <br>
 * The 2nd group specifies the amount of KB-808 needed (orange part)<br>
 * The 3rd group specifies the amount of Taunt Processor needed (blue part)<br>
 * The 4th group specifies the amount of Money Furnace needed<br>
 * The 5th group specifies the amount of Emotion Detector needed (green part)<br>
 * The 6th group specifies the amount of Humor Suppression Pump needed (yellow part)<br>
 * The 7th group specifies the amount of Bomb Stabilizer needed (red part)<br>
 * The 8th group specifies the amount of Currency Digester needed (pink part)<br>
 * The 9th group specifies the amount of Brainstorm Bulb needed (lightbulb part)<br>
 *
 * @author jh34ghu43gu
 */
public class Inputs {
	
	/**
	 * Takes a "Unique", craftable, robo-part or (spec) KS item (returns immediately if not) 
	 * and applies it towards the inputs if applicable. Make sure to save item after calling!
	 * @param item	The item to be applied towards the inputs
	 * @param fabTier	Killstreak tier of the input's owner item
	 * @param inputs
	 * @return	modified input value
	 */
	public static int craftInputs(Item item, int fabTier, int inputs) {
		//Don't accept non-crafts (ks kits) or Non-unique items or <= 0 quantity
		if(!item.isCraftable() || !item.getQuality().equals("Unique") || item.getQuantity() <= 0) { return inputs; } 
		String inputBinary = Integer.toBinaryString(inputs);
		//Pad binary to 30 bits.
		while(inputBinary.length() < 30) {
			inputBinary = "0" + inputBinary;
		}
		
		//Check if we have a valid item then setup the matching substr vars
		int start = 0;
		int end = 0;
		int pad = 0;
		//KS item for spec fab for specKS item for prof fab
		if((item.getKillstreakTier() == 1 && fabTier == 2) || (item.getKillstreakTier() == 2 && fabTier == 3)) {
			start = 0;
			end = 2;
			pad = 2;
		} else { //Robo part?
			if(Inputs.getPartsNeeded(inputs).contains(item.getName())) {
				if(item.getName().equals("Battle-Worn Robot KB-808")) {
					start = 2;
					end = 7;
					pad= 5;
				} else if(item.getName().equals("Battle-Worn Robot Taunt Processor")) {
					start = 7;
					end = 12;
					pad= 5;
				} else if(item.getName().equals("Battle-Worn Robot Money Furnace")) {
					start = 12;
					end = 17;
					pad= 5;
				} else if(item.getName().equals("Reinforced Robot Emotion Detector")) {
					start = 17;
					end = 20;
					pad= 3;
				} else if(item.getName().equals("Reinforced Robot Humor Suppression Pump")) {
					start = 20;
					end = 23;
					pad= 3;
				} else if(item.getName().equals("Reinforced Robot Bomb Stabilizer")) {
					start = 23;
					end = 26;
					pad= 3;
				} else if(item.getName().equals("Pristine Robot Currency Digester")) {
					start = 26;
					end = 28;
					pad= 2;
				} else if(item.getName().equals("Pristine Robot Brainstorm Bulb")) {
					start = 28;
					end = 30;
					pad= 2;
				}
			}
		}
		//A valid item would have changed the end var off of 0
		if(end != 0) {
			int amt = Integer.parseInt(inputBinary.substring(start, end), 2); //amt of X from inputs
			if(amt > 0) { //We actually need it
				if(amt-item.getQuantity() >= 0) { //which amount will be set to 0
					amt = amt - item.getQuantity();
					item.setQuantity(0);
				} else {
					item.setQuantity(item.getQuantity()-amt);
					amt = 0;
				}
			}
			//Pad for input modification
			String sAmt = Integer.toBinaryString(amt);
			while(sAmt.length() < pad) {
				sAmt = "0" + sAmt;
			}
			inputBinary = inputBinary.substring(0, start) + sAmt + inputBinary.substring(end);
		}
		
		inputs = Integer.parseInt(inputBinary, 2);
		return inputs;
	}
	
	/**
	 * @param inputs
	 * @return	An ArrayList<String> of robot part names
	 */
	public static ArrayList<String> getPartsNeeded(int inputs) {
		ArrayList<String> itemNames = new ArrayList<String>();
		
		String inputBinary = Integer.toBinaryString(inputs);
		//Pad binary to 30 bits.
		while(inputBinary.length() < 30) {
			inputBinary = "0" + inputBinary;
		}
		
		//KB-808
		int part = Integer.parseInt(inputBinary.substring(2,7), 2);
		if(part > 0) {
			itemNames.add("Battle-Worn Robot KB-808");
		}
		//Taunt processor
		part = Integer.parseInt(inputBinary.substring(7,12), 2);
		if(part > 0) {
			itemNames.add("Battle-Worn Robot Taunt Processor");
		}
		//Money furnace
		part = Integer.parseInt(inputBinary.substring(12,17), 2);
		if(part > 0) {
			itemNames.add("Battle-Worn Robot Money Furnace");
		}
		//Emotion
		part = Integer.parseInt(inputBinary.substring(17,20), 2);
		if(part > 0) {
			itemNames.add("Reinforced Robot Emotion Detector");
		}
		//Humor
		part = Integer.parseInt(inputBinary.substring(20,23), 2);
		if(part > 0) {
			itemNames.add("Reinforced Robot Humor Suppression Pump");
		}
		//Bomb
		part = Integer.parseInt(inputBinary.substring(23,26), 2);
		if(part > 0) {
			itemNames.add("Reinforced Robot Bomb Stabilizer");
		}
		//Currency
		part = Integer.parseInt(inputBinary.substring(26,28), 2);
		if(part > 0) {
			itemNames.add("Pristine Robot Currency Digester");
		}
		//Brainstorm
		part = Integer.parseInt(inputBinary.substring(28), 2);
		if(part > 0) {
			itemNames.add("Pristine Robot Brainstorm Bulb");
		}
		
		return itemNames;
	}

	/**
	 * @param killstreakTier
	 * @param inputs
	 * @return	A human readable string of input requirements.
	 */
	public static String inputsToString(int killstreakTier, int inputs) {
		String out = "";
		String inputBinary = Integer.toBinaryString(inputs);
		//Pad binary to 30 bits.
		while(inputBinary.length() < 30) {
			inputBinary = "0" + inputBinary;
		}
		//Ks items ?
		int ks = Integer.parseInt(inputBinary.substring(0,2), 2);
		if(ks > 0) {
			out += ks + " Unique ";
			if(killstreakTier == 2) {
				out += "Killstreak item\n";
			} else if(killstreakTier == 3) {
				out += "Specialized Killstreak item";
				if(ks > 1) {
					out += "s\n";
				} else {
					out += "\n";
				}
			} else {
				//Shouldn't get here
				out = "";
			}
		}
		//KB-808
		int part = Integer.parseInt(inputBinary.substring(2,7), 2);
		if(part > 0) {
			out += part + " Battle-Worn Robot KB-808";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Taunt processor
		part = Integer.parseInt(inputBinary.substring(7,12), 2);
		if(part > 0) {
			out += part + " Battle-Worn Robot Taunt Processor";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Money furnace
		part = Integer.parseInt(inputBinary.substring(12,17), 2);
		if(part > 0) {
			out += part + " Battle-Worn Robot Money Furnace";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Emotion
		part = Integer.parseInt(inputBinary.substring(17,20), 2);
		if(part > 0) {
			out += part + " Reinforced Robot Emotion Detector";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Humor
		part = Integer.parseInt(inputBinary.substring(20,23), 2);
		if(part > 0) {
			out += part + " Reinforced Robot Humor Suppression Pump";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Bomb
		part = Integer.parseInt(inputBinary.substring(23,26), 2);
		if(part > 0) {
			out += part + " Reinforced Robot Bomb Stabilizer";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Currency
		part = Integer.parseInt(inputBinary.substring(26,28), 2);
		if(part > 0) {
			out += part + " Pristine Robot Currency Digester";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		//Brainstorm
		part = Integer.parseInt(inputBinary.substring(28), 2);
		if(part > 0) {
			out += part + " Pristine Robot Brainstorm Bulb";
			if(part > 1) {
				out += "s";
			}
			out += "\n";
		}
		
		return out;
	}
}
