package discord.commands;

import java.util.ArrayList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;

public class GetCaseNamesCommand extends Command {
	
	private ArrayList<String> cases;
	
	public GetCaseNamesCommand(ArrayList<String> names) {
		this.name = "cases";
		this.help = "List all available cases.\n"
				+ "Use g.crates to view all crates."; 
		this.cooldown = 15;
		cases = names;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String out = "Available cases are: \n";
		for(String c : cases) {
			out += c + "\n";
		}
		event.reply(out);
	}

}
