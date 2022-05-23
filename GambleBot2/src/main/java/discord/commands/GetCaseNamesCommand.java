package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.GamblerManager;
import bot.Utils;

public class GetCaseNamesCommand extends Command {
	
	private GamblerManager manager;
	
	public GetCaseNamesCommand(GamblerManager gm) {
		this.name = "cases";
		this.help = "List all available cases.\n"
				+ "Use g.crates to view all crates."; 
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
		
		String out = "Available cases are: \n";
		for(String c : manager.getNames(true)) {
			out += c + "\n";
		}
		event.reply(out);
	}

}
