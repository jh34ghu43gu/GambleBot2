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

public class GetPlayerAustraliumsCommand extends Command {
	
	public GetPlayerAustraliumsCommand() {
		this.name = "myaussies";
		this.help = "List your Australiums."; 
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
				if(i.getName().contains("Australium")) {
					items.add(i);
				}
			}
			if(!items.isEmpty()) {
				event.reply(Utils.itemsToEmbed(player, items, "You own the following australiums", "mvm")); //TODO new thumbnail
				return;
			}
			
		}
		event.reply("Could not find any australiums you own.");
	}
}
