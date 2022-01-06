package discord.commands;

import java.text.DecimalFormat;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Player;
import bot.Utils;
import ch.qos.logback.classic.Logger;

public class GetPlayerBalance extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(GetPlayerBalance.class);
	
	public GetPlayerBalance() {
		this.name = "balance";
		this.arguments = "<Another user>";
		this.help = "Get a player's balance. Defaults to your own."; 
		this.cooldown = 15;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		//Delete message if in a non-restricted channel
		if(!Utils.commandCheck(event, "crate") && !Utils.commandCheck(event, "mvm")) {
			event.getMessage().delete().queue();
			return;
		}
		
		String playerID = event.getAuthor().getId();
		DecimalFormat twoDec = new DecimalFormat("###,###.##");
		if(!event.getArgs().isEmpty()) {
			event.getGuild().retrieveMembersByPrefix(event.getArgs(), 1).onSuccess(members -> {
				if(members.isEmpty()) {
					event.reply("Could not find a user by that name.");
					return;
				} else {
					final String altPlayerID = members.get(0).getId();
					Player player = Utils.getPlayer(altPlayerID);
					player.save();
					String dollar = player.getBalance()<0 ? "-$" : "$";
					double bal = player.getBalance()<0 ? player.getBalance()*-1.0 : player.getBalance();
					
					event.reply("User balance: " + dollar + twoDec.format(bal));
					return;
				}
			}).onError(callback -> {
				log.error(callback.getMessage());
			});
			return;
		}
		
		
		Player player = Utils.getPlayer(playerID);
		player.save();
		String dollar = player.getBalance()<0 ? "-$" : "$";
		double bal = player.getBalance()<0 ? player.getBalance()*-1.0 : player.getBalance();
		
		event.reply("User balance: " + dollar + twoDec.format(bal));
	}

}
