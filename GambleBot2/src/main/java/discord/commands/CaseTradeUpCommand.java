package discord.commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.GamblerManager;
import bot.Utils;
import ch.qos.logback.classic.Logger;
import model.Crate;

public class CaseTradeUpCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(CaseTradeUpCommand.class);
	
	private GamblerManager manager;
	
	public CaseTradeUpCommand(GamblerManager gm) {
		this.name = "tradeup";
		this.aliases = new String[] {"tu"};
		this.arguments = "{case number} {item grade that is being traded up [ci fr me co as]} {s for stranges}";
		this.help = "Do a 100% trade up with a specific case. These items are not saved.\n"
				+ "Note: elite trade ups aren't possible."; 
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
		
		String crateStr = "";
		String gradeStr = "";
		String strangeStr = "";
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			for(String s : args) {
				log.debug("Arg: " + s);
			}
			
			if(args.length >= 2) {
				crateStr = args[0];
				gradeStr = args[1];
				if(args.length >= 3) {
					strangeStr = args[2];
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
		
		//See if grade is valid ci fr me co as el
		if(gradeStr.equalsIgnoreCase("ci")) {
			gradeStr = "civilian";
		} else if(gradeStr.equalsIgnoreCase("fr")) {
			gradeStr = "Freelance";
		} else if(gradeStr.equalsIgnoreCase("me")) {
			gradeStr = "Mercenary";
		} else if(gradeStr.equalsIgnoreCase("co")) {
			gradeStr = "Commando";
		} else if(gradeStr.equalsIgnoreCase("as")) {
			gradeStr = "Assassin";
		} else if(gradeStr.equalsIgnoreCase("el")) {
			gradeStr = "Elite";
		}
		
		Crate crateObj = manager.getAllCrates().get(crate);
		if(crateObj.getTiers(false).contains(gradeStr) && !crateObj.getHighestTier(false).equalsIgnoreCase(gradeStr)) {
			String quality = "Unique";
			if(strangeStr.equalsIgnoreCase("s")) {
				quality = "Strange";
			}
			event.reply(crateObj.caseTradeUp(gradeStr, quality, "random").toDiscordString());
		} else {
			event.reply("Invalid grade.");
		}
	}

}
