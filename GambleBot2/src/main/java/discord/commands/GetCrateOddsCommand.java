package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;
import model.Crate;

public class GetCrateOddsCommand extends Command {
	
	public GetCrateOddsCommand() {
		this.name = "crateodds";
		this.arguments = "<crate/case number or name>";
		this.help = "Display current odds related to crates/cases. Specific crate/case odds not yet implimented."; 
		this.cooldown = 300;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		event.reply(Crate.getAllOdds());
	}

}
