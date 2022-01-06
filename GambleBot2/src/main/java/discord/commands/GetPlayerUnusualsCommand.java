package discord.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Player;
import bot.GambleBot;
import bot.Utils;
import repository.ItemRepository;

public class GetPlayerUnusualsCommand extends Command {

	public GetPlayerUnusualsCommand() {
		this.name = "myunusuals";
		this.help = "List your unusuals."; 
		this.cooldown = 60;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate")) {
			event.getMessage().delete().queue();
			return;
		}
		
		Player player = Utils.getPlayer(event.getAuthor().getId());
		ItemRepository IR = GambleBot.getContext().getBean(ItemRepository.class);
		Optional<List<Item>> oList = IR.findByOwnerAndQuality(player, "Unusual");
		if(oList.isPresent()) {
			List<Item> itemList = oList.get();
			ArrayList<Item> items = new ArrayList<Item>();
			for(Item i : itemList) {
				items.add(i);
			}
			event.reply(Utils.itemsToEmbed(player, items, "You own the following unusuals", "crate")); //TODO new thumbnail
		} else {
			event.reply("Could not find any unusuals you own.");
		}
		
	}
}
