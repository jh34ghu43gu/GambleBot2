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

public class GetPlayerPansCommand extends Command {
	
	public GetPlayerPansCommand() {
		this.name = "mypans";
		this.help = "List your Golden Frying Pans."; 
		this.cooldown = 60;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "mvm")) {
			event.getMessage().delete().queue();
			return;
		}
		
		Player player = Utils.getPlayer(event.getAuthor().getId());
		ItemRepository IR = GambleBot.getContext().getBean(ItemRepository.class);
		Optional<List<Item>> oList = IR.findByOwnerAndQuality(player, "Strange");
		if(oList.isPresent()) {
			List<Item> itemList = oList.get();
			ArrayList<Item> items = new ArrayList<Item>();
			for(Item i : itemList) {
				if(i.getName().contains("Golden Frying")) {
					items.add(i);
				}
			}
			if(!items.isEmpty()) {
				event.reply(Utils.itemsToEmbed(player, items, "You own the following pans", "mvm")); //TODO new thumbnail
				return;
			}
			
		}
		event.reply("You own 0 Golden Frying pans, HA! Poor!");
	}

}
