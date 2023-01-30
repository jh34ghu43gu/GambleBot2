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

public class UnboxAllCommand extends Command {

	private GamblerManager manager;
	private static final Logger log = (Logger) LoggerFactory.getLogger(UnboxAllCommand.class);
	private static int defaultAmount = 100;
	private static int[] retiredCrates = {6, 22, 35, 36, 46, 48, 51, 52, 53, 74, 78, 79, 86, 88, 89};
	
	public UnboxAllCommand(GamblerManager gm) {
		this.name = "unboxall";
		this.aliases = new String[] {"ua"};
		this.arguments = "<amt 1-1000>";
		this.help = "This command DOES NOT effect your inventory!\n"
				+ "Unboxes the specified amount of every active series *CRATE* and returns a report with summaries on each one.\n"
				+ "If amount is unspecified then defaults to 100."; 
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
		
		String amtStr = "";
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			if(args.length >= 1) {
				amtStr = args[0];
			}
		}
		
		int amt = defaultAmount;
		try {
			amt = Integer.parseInt(amtStr);
		} catch(NumberFormatException e) {
			
		}
		
		//Even though we won't save the items we still need to pass a player arg
		Player player = Utils.getPlayer(event.getAuthor().getId());
		player.save();
		String tempOut = "";
		String tempOut2 = "";
		String tempOut3 = "";
		double totalValue = 0.0;
		int totalPriced = 0;
		int totalItems = 0;
		int totalCheckedItems = 0;
		
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		for(Crate c : manager.getCratesOrCases(false)) {
			//Skip the crates we can't open
			boolean skip = false;
			for(int i : retiredCrates) {
				if(c.getNumber() == i) {
					skip = true;
					continue;
				}
			}
			if(skip) {
				continue;
			}
			
			ArrayList<Item> totalCrateLoot = new ArrayList<Item>();
			int unusuals = 0;
			int stranges = 0;
			for(int i = 0; i < amt; i++) {
				for(Item item : c.open(player)) {
					totalItems++;
					//Condense
					for(Item lootItem : totalCrateLoot) {
						if(lootItem.canCombine(item)) {
							lootItem.setQuantity(lootItem.getQuantity() + item.getQuantity());
							item.setQuantity(0);
							break;
						}
					}
					//If item has a quantity over 0 then it wasn't added to an item that already existed
					if(item.getQuantity() > 0) {
						totalCrateLoot.add(item);
					}
					
					if(item.getQuality().equalsIgnoreCase("Unusual")) {
						unusuals++;
					}
					if(item.getQuality().equals("Strange")) {
						stranges++;
					}
				}
			}
			JsonObject valueObj = Utils.getTotalItemsValue(totalCrateLoot);
			double value = valueObj.get("value").getAsDouble();
			int priced = valueObj.get("items").getAsInt();
			double spent = manager.getCratePrice(c.getNumber()) * (double)amt;
			
			
			String result = "#" + c.getNumber() + " | " + priced + "/" + totalCrateLoot.size() 
						+ " | " + twoDec.format(value) + " keys | "
						+ unusuals + " | " + stranges + " | "
						+ twoDec.format(spent) + " " + manager.getCratePriceCurrency(c.getNumber());
			if(tempOut.length()+result.length() > 1990) {
				if(tempOut2.length() > 1) {
					tempOut3 = tempOut2;
				}
				tempOut2 = tempOut;
				tempOut = "";
			}
			tempOut += result + "\n";
			totalValue += value;
			totalPriced += priced;
			totalCheckedItems += totalCrateLoot.size();
		}
		
		event.reply(totalPriced + "/" + totalCheckedItems + " items priced and valued at " + twoDec.format(totalValue) + " keys.\n"
				+ "Unboxed " + totalItems + " items.\n"
				+ "Crate # | priced items/total items | priced items' key value | Unusuals | Stranges | Crate cost\n");
		if(tempOut3.length() > 1) {
			event.reply(tempOut3);
		}
		event.reply(tempOut2);
		event.reply(tempOut);
		
	}

}
