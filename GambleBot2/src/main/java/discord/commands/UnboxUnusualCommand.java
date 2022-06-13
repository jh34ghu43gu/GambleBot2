package discord.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

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

public class UnboxUnusualCommand extends Command {

	private GamblerManager manager;
	private static final Logger log = (Logger) LoggerFactory.getLogger(UnboxCrateCommand.class);
	
	public UnboxUnusualCommand(GamblerManager gm) {
		this.name = "unboxunusual";
		this.aliases = new String[] {"uu"};
		this.arguments = "<crate/case number or name> <tc>";
		this.help = "Unbox the specified crate/case until you get an unusual. Omit number/name to randomly pick. tc to go until a team captain, must provide crate number."; 
		this.cooldown = 5;
		manager = gm;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		boolean tc = false;
		String crateStr = "";
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			if(args.length >= 1) {
				crateStr = args[0];
			}
			if(args.length > 1 && args[1].equalsIgnoreCase("tc")) {
				tc = true;
			}
		}
		
		int crate = -1;
		int crateInt = -1;
		String crateName = "";
		if(crateStr.isEmpty()) {
			Random rand = new Random();
			do {
				crate = rand.nextInt(manager.getAllCrates().size());
			} while(!manager.getAllCrates().get(crate).isUnusuals());
			crateName = manager.getAllCrates().get(crate).getName();
		} else {
			//See if crate/case is valid
			for(Crate c : manager.getAllCrates()) {
				if(c.getNames().toLowerCase().contains(crateStr.toLowerCase())) {
					crate = crateInt+1;
					log.debug("Crate found: " + c.getName());
					if(!c.isUnusuals()) {
						crate = -1;
						break;
					}
					break;
				}
				crateInt++;
			}
		}
		
		if(crate == -1) {
			event.reply("Could not find the specified crate/case.");
			log.debug("Could not find crate/case: " + crateStr);
			return;
		}
		
		if(tc) {
			if(!manager.getAllCrates().get(crate).getCrateHats().equalsIgnoreCase("hats") || manager.getAllCrates().get(crate).isCase()) {
				event.reply("Selected crate does not contain a Team Captain.");
				return; 
			}
		}
		
		Player player = Utils.getPlayer(event.getAuthor().getId());
		player.save();
		
		ArrayList<Item> allLoot = new ArrayList<Item>();
		boolean unusual = false;
		int unboxed = 0;
		Item unusualUnboxed = null;
		while(!unusual) {
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
				
				if(item.getQuality().equals("Unusual") && !item.getName().contains("Unusualifier")) {
					if(tc && item.getName().equalsIgnoreCase("Team Captain")) {
						unusual = true;
						unusualUnboxed = item;
					} else if(!tc) {
						unusual = true;
						unusualUnboxed = item;
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
		
		
		String firstMsg = "";
		if(!crateName.isEmpty()) {
			firstMsg = "Selected " + crateName + " to unbox until an unusual.\n";
		}
		
		firstMsg += "It took you " + unboxed + " unboxes to get " + unusualUnboxed.toDiscordString()
		+ ".\nAttempting to display " + displayItems.size() + " items out of " + allLoot.size() + " items total in the next message.";
		
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		event.reply("Found prices for " + priced + "/" + allLoot.size() + " items. Total value in keys: " + twoDec.format(value));
		event.reply(firstMsg);
		event.reply(Utils.itemsToEmbed(player, displayItems, "You unboxed the following", "crate"));
	}
	
}
