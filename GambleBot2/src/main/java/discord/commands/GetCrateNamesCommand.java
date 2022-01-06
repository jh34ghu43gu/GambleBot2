package discord.commands;

import java.util.ArrayList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;
import model.Crate;

public class GetCrateNamesCommand extends Command {
	
	private ArrayList<Crate> crates;
	
	public GetCrateNamesCommand() {
		this.name = "crates";
		this.help = "List all available crates and cases."; 
		this.cooldown = 15;
		crates = Crate.getCases();
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String out = "Available crates/cases are: \n";
		for(Crate c : crates) {
			out += c.getNames() + "\n";
		}
		event.reply(out);
	}

}
