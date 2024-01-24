package discord.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Player;
import bot.GamblerManager;
import bot.Utils;
import ch.qos.logback.classic.Logger;
import model.Crate;
import model.CrateItem;

public class UnboxEliteCommand extends Command {

	private GamblerManager manager;
	private static final Logger log = (Logger) LoggerFactory.getLogger(UnboxCrateCommand.class);
	
	public UnboxEliteCommand(GamblerManager gm) {
		this.name = "unboxelite";
		this.aliases = new String[] {"ue"};
		this.arguments = "{case number or name} <unusual or u> <strange or s> <fn>";
		this.help = "Unbox the specified case until you get an elite grade item. Can specify if you want unusual, strange, FN, or any combination of."; 
		this.cooldown = 60;
		manager = gm;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String crateStr = "";
		boolean unusual = false;
		boolean strange = false;
		boolean fn = false;
		boolean nc = false;
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			for(String s : args) {
				log.debug("Arg: " + s);
			}
			if(args.length >= 1) {
				crateStr = args[0];
			}
			for(String s : args) {
				if(s.equalsIgnoreCase("unusual") || s.equalsIgnoreCase("u")) {
					unusual = true;
				}
				if(s.equalsIgnoreCase("strange") || s.equalsIgnoreCase("s")) {
					strange = true;
				}
				if(s.equalsIgnoreCase("fn")) {
					fn = true;
				}
				if(s.equalsIgnoreCase("nc")) {
					nc = true;
				}
			}
		}
		//Must have a case
		if(crateStr.isEmpty()) {
			event.reply("Case must be specified.");
			return;
		}
		
		//See if case is valid
		int crate = -1;
		int crateInt = -1;
		for(Crate c : manager.getAllCrates()) {
			if(c.getNames().toLowerCase().contains(crateStr.toLowerCase())) {
				crate = crateInt+1;
				log.debug("Crate found: " + c.getName());
				break;
			}
			crateInt++;
		}
		
		if(crate == -1) {
			event.reply("Could not find the specified crate/case.");
			log.debug("Could not find crate/case: " + crateStr);
			return;
		}
		
		if(fn && !manager.getAllCrates().get(crate).isSkin()) {
			event.reply("FN can only be used with skin/paint cases.");
			return;
		}
		
		if(nc && crate != 104) {
			event.reply("NC can only be used on the quarantined (96) case.");
			return;
		}
		
		//Check for case and contains elite.
		if(manager.getAllCrates().get(crate).isCase()) {
			boolean hasElite = false;
			boolean eliteUnu = false;
			for(CrateItem item : manager.getAllCrates().get(crate).getItemList()) {
				if(item.getQuality().equals("Elite")) {
					hasElite = true;
					if(unusual) {
						if(manager.getAllCrates().get(crate).isSkin() || item.isHat()) {
							eliteUnu = true;
							break;
						}
					} else {
						break;
					}
				}
			}
			if(!hasElite) {
				event.reply("The specified case does not contain an elite grade item.");
				return;
			} else if(unusual && !eliteUnu) {
				event.reply("This case does not have an elite grade item that can be unusual.");
				return;
			}
		} else {
			event.reply("This command can only use cases. g.cases to see a list.");
			return;
		}
		
		Player player = Utils.getPlayer(event.getAuthor().getId());
		player.save();
		
		ArrayList<Item> allLoot = new ArrayList<Item>();
		int unboxed = 0;
		boolean foundItem = false;
		Item unboxedItem = null;
		while(!foundItem) {
			unboxed++;
			for(Item item : manager.getAllCrates().get(crate).open(player)) {
				//Condense
				for(Item lootItem : allLoot) {
					if(lootItem.canCombine(item)) {
						lootItem.setQuantity(lootItem.getQuantity() + item.getQuantity());
						item.setQuantity(0);
						break;
					}
				}
				//If item has a quantity over 0 then it wasn't added to an item that already existed
				if(item.getQuantity() > 0) {
					allLoot.add(item);
				}
				
				//Check if matched stuff
				if(item.getTier() != null && item.getTier().equals("Elite")) {
					boolean checks = true;
					if(unusual) {
						if(!item.getQuality().equals("Unusual") || item.getName().contains("Unusualifier")) {
							checks = false;
						}
					}
					if(strange) {
						if(!item.getQuality().equals("Strange")) {
							if(item.getSecondaryQuality() == null || !item.getSecondaryQuality().equals("Strange")) {
								checks = false;
							}
						}
					}
					if(fn) {
						if(!item.getWear().equals("Factory New")) {
							checks = false;
						}
					}
					if(nc) {
						if(!item.getQuality().equals("Unusual") || !item.getEffect().equals("Nebula")) {
							checks = false;
						}
						if(!item.getName().equals("Corona Australis")) {
							checks = false;
						}
					}
					if(checks) {
						foundItem = true;
						unboxedItem = item;
					}
				}
				
			}
		}
		JsonObject valueObj = Utils.getTotalItemsValue(allLoot); //Do this before subtracting balance in case we get an error
		double value = valueObj.get("value").getAsDouble();
		int priced = valueObj.get("items").getAsInt();
		
		player.setBalance(player.getBalance() - (Crate.unboxPrice * unboxed));
		player.save();
		
		int i = 0;
		ArrayList<Item> displayItems = new ArrayList<Item>();
		for(Item item : allLoot) {
			i++;
			if(i<100) {
				displayItems.add(item);
			}
			log.debug("Saving item: " + item.toString());
			item.save();
		}
		
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		event.reply("Found prices for " + priced + "/" + allLoot.size() + " items. Total value in keys: " + twoDec.format(value));
		event.reply("It took you " + unboxed + " unboxes to get " + unboxedItem.toDiscordString()
				+ ". Up to 100 additional items are displayed in the following message.");
		event.reply(Utils.itemsToEmbed(player, displayItems, "You unboxed the following", "crate"));
	}

}
