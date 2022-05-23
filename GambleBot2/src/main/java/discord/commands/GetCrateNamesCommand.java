package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.GamblerManager;
import bot.Utils;

public class GetCrateNamesCommand extends Command {
	
	private GamblerManager manager;
	
	public GetCrateNamesCommand(GamblerManager gm) {
		this.name = "crates";
		this.help = "List all available crates.\n"
				+ "Use g.cases to view all cases."; 
		this.cooldown = 15;
		manager = gm;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String out = "Available crates are: \n";
		for(String c : manager.getNames(false)) {
			out += c + "\n";
		}
		event.reply(out);
	}

}
