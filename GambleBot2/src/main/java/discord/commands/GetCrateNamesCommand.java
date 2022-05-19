package discord.commands;

import java.util.ArrayList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;

public class GetCrateNamesCommand extends Command {
	
	private ArrayList<String> crates;
	
	public GetCrateNamesCommand(ArrayList<String> names) {
		this.name = "crates";
		this.help = "List all available crates.\n"
				+ "Use g.cases to view all cases."; 
		this.cooldown = 15;
		crates = names;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String out = "Available crates are: \n";
		for(String c : crates) {
			out += c + "\n";
		}
		event.reply(out);
	}

}
