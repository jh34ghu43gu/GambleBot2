package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.Utils;

public class UpdatePricelistCommand extends Command {
	
	public UpdatePricelistCommand() {
		this.name = "updateprices";
		this.aliases = new String[] {"updatepricelist"};
		this.help = "Update the local backpack.tf pricelist. \n"
				+ "Can only be run by the server owner.";
		this.ownerCommand = true;
		this.cooldown = 3600;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		event.reply("Updating pricelist.");
		if(Utils.updatePricelist()) {
			event.reply("Local pricelist updated.");
		} else {
			event.reply("Failed to update local pricelist.");
		}
	}

}
