package discord.commands;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Leaderboard;
import beans.Player;
import bot.Utils;
import ch.qos.logback.classic.Logger;
import model.Tour;

public class MvmTourCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(MvmTourCommand.class);
	private static final int tourLimit = 10;

	public MvmTourCommand() {
		this.name = "tour";
		this.arguments = "{OS | ST | ME | TC | GG} <amount 1-" + tourLimit + ">";
		this.help = "Simulate the specified tour's loot. Defaults to 1 tour, can do up to " + tourLimit + " at once.\n"
				+ "Will add all the items to your inventory from all missions and the final tour loot."; 
		//TODO add reference to inventory command.
		this.cooldown = 5;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "mvm")) {
			event.getMessage().delete().queue();
			return;
		}
		//Check for tour arg
		if(event.getArgs().isEmpty()) {
			event.replyError("Please say which tour you would like to do. Valid options are: 'OS', 'ST', 'ME', 'TC', or 'GG'");
			return;
		}
		//Valid tour?
		String[] args = event.getArgs().split(" ");
		String tour = args[0];
		if(tour.equalsIgnoreCase("OS") || tour.equalsIgnoreCase("oilspill")) {
			tour = "OilSpill";
		} else if(tour.equalsIgnoreCase("ST") || tour.equalsIgnoreCase("steeltrap")) {
			tour = "SteelTrap";
		} else if(tour.equalsIgnoreCase("ME") || tour.equalsIgnoreCase("mechaengine")) {
			tour = "MechaEngine";
		} else if(tour.equalsIgnoreCase("TC") || tour.equalsIgnoreCase("2C") || tour.equalsIgnoreCase("twocities")) {
			tour = "TwoCities";
		} else if(tour.equalsIgnoreCase("GG") || tour.equalsIgnoreCase("geargrinder")) {
			tour = "GearGrinder";
		} else {
			event.replyError("I don't recognize that tour. Valid options are: 'OS', 'ST', 'ME', 'TC', or 'GG'");
			return;
		}
		
		//Amount to do
		int amt = 1;
		if(args.length > 1) {
			try {
				amt = Integer.parseInt(args[1]);
			} catch(Exception e) {
				event.replyError("You have entered an invalid number.");
				return;
			}
		}
		if(amt < 1 || amt > tourLimit) {
			event.replyError("You have enetered an invalid amount of tours. Valid amounts are 1-" + tourLimit + ".");
			return;
		}
		
		//JUST DO IT
		Player player = Utils.getPlayer(event.getMember().getId());
		player.save(); //Save immediately if player did not exist then adding items will give FK DB errors
		
		int tourNumber = 0;
		int dry = 0;
		int panDry = player.getPanDry();
		int overallDry = player.getOverallDry();
		double ticketMulti = 0;
		if(tour.equalsIgnoreCase("OilSpill")) {
			tourNumber = player.getOsTour();
			ticketMulti = 6;
		} else if(tour.equalsIgnoreCase("SteelTrap")) {
			tourNumber = player.getStTour();
			dry = player.getSteelDry();
			ticketMulti = 6;
		} else if(tour.equalsIgnoreCase("MechaEngine")) {
			tourNumber = player.getMeTour();
			dry = player.getMechaDry();
			ticketMulti = 3;
		} else if(tour.equalsIgnoreCase("TwoCities")) {
			tourNumber = player.getTcTour();
			dry = player.getTwoDry();
			ticketMulti = 4;
		} else if(tour.equalsIgnoreCase("GearGrinder")) {
			tourNumber = player.getGgTour();
			dry = player.getGearDry();
			ticketMulti = 3;
		}
		int lastDry = 0;
		int lastPanDry = 0;
		int lastOverallDry = 0;
		log.debug("Attempting to do " + amt + " " + tour + " tour(s) for player: " + player.getId());
		ArrayList<Item> allLoot = new ArrayList<Item>();
		for(int i = 0; i < amt; i++) {
			for(Item item : Tour.doTour(tour, tourNumber+1, player)) {
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
				if(item.getName().contains("Australium") && item.getQuality().equals("Strange")) { //Strange bc of paint
					if(lastDry == 0) { //Only change lastDry once.
						lastDry = dry;
					}
					if(lastOverallDry == 0) {
						lastOverallDry = overallDry;
					}
					overallDry = -1;
					dry = -1;
				} else if(item.getName().equals("Golden Frying Pan")) {
					if(lastDry == 0) {
						lastDry = dry;
					}
					if(lastPanDry == 0) {
						lastPanDry = panDry;
					}
					if(lastOverallDry == 0) {
						lastOverallDry = overallDry;
					}
					dry = -1;
					panDry = -1;
					overallDry = -1;
				}
			}
			dry++;
			overallDry++;
			panDry++;
			tourNumber++;
		}
		log.debug("Finished tours, saving items, drystreaks, and tour amounts.");
		for(Item i : allLoot) {
			log.debug("Saving item: " + i.toString());
			i.save();
		}
		if(tour.equalsIgnoreCase("OilSpill")) {
			player.setOsTour(tourNumber);
		} else if(tour.equalsIgnoreCase("SteelTrap")) {
			player.setStTour(tourNumber);
			player.setSteelDry(dry);
			player.setOverallDry(overallDry);
			player.setPanDry(panDry);
		} else if(tour.equalsIgnoreCase("MechaEngine")) {
			player.setMeTour(tourNumber);
			player.setMechaDry(dry);
			player.setOverallDry(overallDry);
			player.setPanDry(panDry);
		} else if(tour.equalsIgnoreCase("TwoCities")) {
			player.setTcTour(tourNumber);
			player.setTwoDry(dry);
			player.setOverallDry(overallDry);
			player.setPanDry(panDry);
		} else if(tour.equalsIgnoreCase("GearGrinder")) {
			player.setGgTour(tourNumber);
			player.setGearDry(dry);
			player.setOverallDry(overallDry);
			player.setPanDry(panDry);
		}
		player.setBalance(player.getBalance() - (ticketMulti * Tour.tourCost * amt));
		player.save();
		
		
		//Return updates
		ArrayList<Item> embedItems = new ArrayList<Item>();
		String out = "";
		if(lastPanDry > 0) {
			out += "Congrats on getting a pan. You went " + lastPanDry + " tours overall without one.\n";
			
			//Update leaderboards
			Leaderboard overallDryBoard = new Leaderboard("overall drystreak", lastOverallDry, new Timestamp(System.currentTimeMillis()), player);
			Leaderboard tourDryBoard = new Leaderboard(tour.toLowerCase() + " drystreak", lastDry, new Timestamp(System.currentTimeMillis()), player);
			Leaderboard panDryBoard = new Leaderboard("pan drystreak", panDry, new Timestamp(System.currentTimeMillis()), player);
			Utils.updateLeaderboard(overallDryBoard);
			Utils.updateLeaderboard(tourDryBoard);
			Utils.updateLeaderboard(panDryBoard);
		} else if(lastDry > 0) {
			out += "Congrats on beating your overall aussie drystreak of " + lastOverallDry + ". "
					+ "Your drystreak for this tour was " + lastDry + ".\n";
			
			//Update leaderboards
			Leaderboard overallDryBoard = new Leaderboard("overall drystreak", lastOverallDry, new Timestamp(System.currentTimeMillis()), player);
			Leaderboard tourDryBoard = new Leaderboard(tour.toLowerCase() + " drystreak", lastDry, new Timestamp(System.currentTimeMillis()), player);
			Utils.updateLeaderboard(overallDryBoard);
			Utils.updateLeaderboard(tourDryBoard);
		}
		
		
		if(allLoot.size() > 25) {
			out += "You have recieved too many items to display properly. Only displaying a limited number of stranges or pro ks fabs:\n";
			for(Item item : allLoot) {
				if(item.getQuality().equals("Strange") || item.getKillstreakTier() == 3) {
					embedItems.add(item);
				}
			}
		} else {
			embedItems = allLoot;
		}
		//event.reply(out);
		if(out.isEmpty()) {
			if(!tour.equalsIgnoreCase("OilSpill")) {
				out += "Your current drystreak is: " + overallDry + ".\n";
				out += "Your current drystreak in this tour is: " + dry + ".\n";
			}
			out += "Your loot is:\n";
		}
		event.reply(Utils.itemsToEmbed(player, embedItems, out, "mvm"));
	}
	
}
