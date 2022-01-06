package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;
import model.Tour;

public class GetMvmOddsCommand extends Command {

	public GetMvmOddsCommand() {
		this.name = "mvmodds";
		this.arguments = "<aus | australiums | aussies | australium>";
		this.help = "Display current odds related to mvm tours. Add 'Aus' to show odds of getting a certain australium."; 
		this.cooldown = 300;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "mvm")) {
			event.getMessage().delete().queue();
			return;
		}
		
		//Are we getting aussie stats or normal stats?
		boolean aus = false;
		if(!event.getArgs().isEmpty()) {
			String[] args = event.getArgs().split(" ");
			String ausString = args[0];
			if(ausString.equalsIgnoreCase("aus") || ausString.equalsIgnoreCase("australiums") || ausString.equalsIgnoreCase("aussies") || ausString.equalsIgnoreCase("australium")) {
				aus = true;
			}
		}
		
		if(aus) {
			String out = Tour.getAussieInfo();
			out += "Use 'mvmodds' to get tour related odds.";
			event.reply(out);
		} else {
			String out = Tour.getTourInfo();
			out += "Use 'mvmodds aus' for odds of recieving a specific australium.";
			event.reply(out);
		}
	}

}
