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

public class UnboxCrateCommand extends Command {
	
	private static int caseUnboxLimit = 500;
	private static int crateUnboxLimit = 1000;
	
	private GamblerManager manager;
	private static final Logger log = (Logger) LoggerFactory.getLogger(UnboxCrateCommand.class);
	
	public UnboxCrateCommand(GamblerManager gm) {
		this.name = "unbox";
		this.arguments = "{crate/case number or name} {amount 1-" + caseUnboxLimit + " for cases, " + crateUnboxLimit + "for crates}";
		this.help = "Unbox the specified crate/case."; 
		this.cooldown = 2;
		manager = gm;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		int amt = 1;
		String crateStr = "";
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			for(String s : args) {
				log.debug("Arg: " + s);
			}
			if(args.length == 1) {
				crateStr = args[0];
			} else {
				String amtStr = args[args.length-1]; //Last arg should be amt, if specified
				try {
					amt = Integer.parseInt(amtStr);
					log.debug("Amount = " + amt);
					for(int i = 0; i < args.length-1; i++) {
						crateStr += args[i];
					}
				} catch(Exception e) {
					log.debug("Amount string not int: " + amtStr);
					for(int i = 0; i < args.length; i++) {
						crateStr += args[i];
					}
				}
			}
		}
		//Must have a case/crate
		if(crateStr.isEmpty()) {
			event.reply("Crate/Case must be specified.");
			return;
		}
		
		//See if crate/case is valid
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
		
		//Must be between 1 and limit
		int unboxLimit = (manager.getAllCrates().get(crate).isCase() || manager.getAllCrates().get(crate).isKillstreakKits()) ? caseUnboxLimit : crateUnboxLimit;
		if(amt < 1 || amt > unboxLimit) {
			event.reply("Amount must be between 1 and " + unboxLimit + ".");
			log.debug("User wanted to do invalid amount of unboxes." + amt);
			return;
		}
		
		Player player = Utils.getPlayer(event.getAuthor().getId());
		player.save();
		
		ArrayList<Item> allLoot = new ArrayList<Item>();
		for(int i = 0; i < amt; i++) {
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
			}
		}
		JsonObject valueObj = Utils.getTotalItemsValue(allLoot); //Do this before subtracting balance in case we get an error
		double value = valueObj.get("value").getAsDouble();
		int priced = valueObj.get("items").getAsInt();
		
		player.setBalance(player.getBalance() - (Crate.unboxPrice * amt));
		player.save();
		
		for(Item item : allLoot) {
			log.debug("Saving item: " + item.toString());
			item.save();
		}
		
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		event.reply("Found prices for " + priced + "/" + allLoot.size() + " items. Total value in keys: " + twoDec.format(value));
		event.reply(Utils.itemsToEmbed(player, allLoot, "You unboxed the following", "crate"));
	}
}
