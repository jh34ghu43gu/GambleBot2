package discord.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Player;
import bot.GambleBot;
import bot.Utils;
import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import repository.ItemRepository;

public class MvmStatsCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(MvmStatsCommand.class);
	
	public MvmStatsCommand() {
		this.name = "mvmstats";
		this.help = "Retrive your mvm related statistics."; 
		this.cooldown = 300;
	}

	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "mvm")) {
			event.getMessage().delete().queue();
			return;
		}
		
		//Get items
		String[] tours = {"OilSpill", "SteelTrap", "MechaEngine", "TwoCities", "GearGrinder"};
		Player player = Utils.getPlayer(event.getAuthor().getId());
		ItemRepository IR = GambleBot.getContext().getBean(ItemRepository.class);
		ArrayList<Item> items = new ArrayList<Item>();
		for(String t : tours) {
			Optional<List<Item>> ol = IR.findByOwnerAndOrigin(player, t);
			if(ol.isPresent()) {
				items.addAll(ol.get());
			}
		}
		
		//OS
		double rusts = 0;
		double bloods = 0;
		//ST
		double silverI = 0;
		double goldI = 0;
		double stAussies = 0;
		//ME
		double silverII = 0;
		double goldII = 0;
		double meAussies = 0;
		//2c
		double battleWorns = 0;
		double reinforceds = 0;
		double pristines = 0;
		double kits = 0;
		double specFabs = 0;
		double proFabs = 0;
		double tcAussies = 0;
		//GG
		double carbonados = 0;
		double diamonds = 0;
		double ggAussies = 0;
		//Pans
		double pans = 0;
		
		for(Item item : items) {
			log.debug("Item Name Debug: " + item.getName());
			if(item.getName().contains("Rust Botkiller")) {
				rusts += item.getQuantity();
			} else if(item.getName().contains("Blood Botkiller")) {
				bloods += item.getQuantity();
			} else if(item.getName().contains("Silver Botkiller")) {
				if(item.getOrigin().equals("SteelTrap")) {
					silverI += item.getQuantity();
				} else {
					silverII += item.getQuantity();
				}
			} else if(item.getName().contains("Gold Botkiller")) {
				if(item.getOrigin().equals("SteelTrap")) {
					goldI += item.getQuantity();
				} else {
					goldII += item.getQuantity();
				}
			}else if(item.getName().contains("Battle-Worn")) {
				battleWorns += item.getQuantity();
			} else if(item.getName().contains("Reinforced")) {
				reinforceds += item.getQuantity();
			} else if(item.getName().contains("Pristine")) {
				pristines += item.getQuantity();
			} else if(item.getKillstreakTier() == 1 && item.getOrigin().equals("TwoCities")) {
				kits += item.getQuantity();
			} else if(item.getKillstreakTier() == 2 && item.getOrigin().equals("TwoCities")) {
				specFabs += item.getQuantity();
			} else if(item.getKillstreakTier() == 3 && item.getOrigin().equals("TwoCities")) {
				proFabs += item.getQuantity();
			} else if(item.getName().contains("Carbonado Botkiller")) {
				carbonados += item.getQuantity();
			} else if(item.getName().contains("Diamond Botkiller")) {
				diamonds += item.getQuantity();
			} else if(item.getName().contains("Australium") && item.getQuality().equals("Strange")) {
				String origin = item.getOrigin();
				if(origin.equals("SteelTrap")) {
					stAussies += item.getQuantity();
				} else if(origin.equals("MechaEngine")) {
					meAussies += item.getQuantity();
				} else if(origin.equals("TwoCities")) {
					tcAussies += item.getQuantity();
				} else if(origin.equals("GearGrinder")) {
					ggAussies += item.getQuantity();
				}
			} else if(item.getName().contains("Golden Frying Pan")) {
				pans += item.getQuantity();
			}
		}
			
		double totalAussies = stAussies + meAussies + tcAussies + ggAussies;
		double totalAusTours = player.getStTour() + player.getMeTour() + player.getTcTour() + player.getGgTour();
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		DecimalFormat noDec = new DecimalFormat("###,###");
		String allText = "";
		String osText = "";
		String stText = "";
		String meText = "";
		String tcText = "";
		String ggText = "";
		//Divide by 0 check
		if((player.getOsTour()+totalAusTours) == 0) {
			event.reply("You have not completed any tours.");
			return;
		}
		allText = "Total tours: " + noDec.format((player.getOsTour()+totalAusTours)) + " (Australium dropping tours: " + noDec.format(totalAusTours) + ").\n"
				+ "Total Australiums: " + noDec.format(totalAussies) + " (" + twoDec.format((totalAussies/totalAusTours)*100) + "%)\n"
				+ "Total Golden Frying Pans: " + noDec.format(pans) + " (" + twoDec.format((pans/totalAusTours)*100) + "%)\n";
		
		if(player.getOsTour() != 0) {
			osText = "Total tours: " + noDec.format(player.getOsTour()) + "\n"
					+ "Total Rust Botkillers: " + noDec.format(rusts) + " (" + twoDec.format(rusts/(double)player.getOsTour()*100) + "%)\n"
					+ "Total Blood Botkillers: " + noDec.format(bloods) + " (" + twoDec.format(bloods/(double)player.getOsTour()*100) + "%)\n";
		}
		if(player.getStTour() != 0) {
			stText = "Total tours: " + noDec.format(player.getStTour()) + "\n"
					+ "Total Silver Mk.I Botkillers: " + noDec.format(silverI) + " (" + twoDec.format(silverI/(double)player.getStTour()*100) + "%)\n"
					+ "Total Gold Mk.I Botkillers: " + noDec.format(goldI) + " (" + twoDec.format(goldI/(double)player.getStTour()*100) + "%)\n"
					+ "Total Australiums: " + noDec.format(stAussies) + " (" + twoDec.format(stAussies/(double)player.getStTour()*100) + "%)\n";
		}	
		if(player.getMeTour() != 0) {	
			meText = "Total tours: " + noDec.format(player.getMeTour()) + "\n"
					+ "Total Silver Mk.II Botkillers: " + noDec.format(silverII) + " (" + twoDec.format(silverII/(double)player.getMeTour()*100) + "%)\n"
					+ "Total Gold Mk.II Botkillers: " + noDec.format(goldII) + " (" + twoDec.format(goldII/(double)player.getMeTour()*100) + "%)\n"
					+ "Total Australiums: " + noDec.format(meAussies) + " (" + twoDec.format(meAussies/(double)player.getMeTour()*100) + "%)\n";
		}
		if(player.getGgTour() != 0) {
			ggText = "Total tours: " + noDec.format(player.getGgTour()) + "\n"
					+ "Total Carbonado Botkillers: " + noDec.format(carbonados) + " (" + twoDec.format(carbonados/(double)player.getGgTour()*100) + "%)\n"
					+ "Total Diamond Botkillers: " + noDec.format(diamonds) + " (" + twoDec.format(diamonds/(double)player.getGgTour()*100) + "%)\n"
					+ "Total Australiums: " + noDec.format(ggAussies) + " (" + twoDec.format(ggAussies/(double)player.getGgTour()*100) + "%)\n";
		}
		if(player.getTcTour() != 0) {
			tcText = "Total tours: " + noDec.format(player.getTcTour()) + "\n"
					+ "Total Battle-Worn Parts: " + noDec.format(battleWorns) + " (Avg per mission: " + twoDec.format(battleWorns/((double)player.getTcTour()*4)) + ")\n"
					+ "Total Reinforced Parts: " + noDec.format(reinforceds) + " (Avg per mission: " + twoDec.format(reinforceds/((double)player.getTcTour()*4)) + ")\n"
					+ "Total Pristine Parts: " + noDec.format(pristines) + " (Avg per mission: " + twoDec.format(pristines/((double)player.getTcTour()*4)) + ")\n"
					+ "Total Killstreak kits: " + noDec.format(kits) + "\n"
					+ "Total Specialized Fabs: " + noDec.format(specFabs) + " (Avg per tour: " + twoDec.format(specFabs/((double)player.getTcTour())) + ")\n"
					+ "Total Professional Fabs: " + noDec.format(proFabs) + " (" + twoDec.format(proFabs/(double)player.getTcTour()*100) + "%)\n"
					+ "Total Australiums: " + noDec.format(tcAussies) + " (" + twoDec.format(tcAussies/(double)player.getTcTour()*100) + "%)\n";
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(event.getAuthor().getName());
		eb.setTitle("Your current mvm stats.\n"
				+ "Current overall drystreak: " + noDec.format(player.getOverallDry()) + "\n"
				+ "Balance: $" + twoDec.format(player.getBalance()));
		eb.addField("Overall stats", allText, false);
		if(!osText.isEmpty()) {
			eb.addField("Oil Spill stats", osText, false);
		}
		if(!stText.isEmpty()) {
			eb.addField("Steel Trap stats", stText, false);
		}
		if(!meText.isEmpty()) {
			eb.addField("Mecha Engine stats", meText, false);
		}
		if(!tcText.isEmpty()) {
			eb.addField("Two Cities stats", tcText, false);
		}
		if(!ggText.isEmpty()) {
			eb.addField("Gear Grinder stats", ggText, false);
		}
		event.reply(eb.build());
		
	}

}
