package bot;

import java.awt.Color;
import java.util.ArrayList;

import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Player;
import beans.Server;
import files.ConfigHelper;
import model.Tour;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import repository.PlayerRepository;
import repository.ServerRepository;

/**
 * Utility class for static methods used in multiple other classes
 * @author jh34ghu43gu
 */
public class Utils {
	
	private static final String TICKET_URL = "https://wiki.teamfortress.com/w/images/9/9b/Backpack_Tour_of_Duty_Ticket.png";
	private static final String KEY_URL = "https://wiki.teamfortress.com/w/images/8/83/Backpack_Mann_Co._Supply_Crate_Key.png";
	
	/**
	 * @param event
	 * @param channel Channel restriction to check for; either 'mvm' or 'crate'
	 * @return If the command can be run in the given channel.
	 */
	public static boolean commandCheck(CommandEvent event, String channel) {
		Server server = getServer(event.getGuild().getId());
		String cID = event.getChannel().getId();
		if(channel.equalsIgnoreCase("mvm")) {
			if(server.getMvmChannel().isEmpty() || server.getMvmChannel().equals(cID)) {
				return true;
			}
		} else if(channel.equalsIgnoreCase("crate")) {
			if(server.getCrateChannel().isEmpty() || server.getCrateChannel().equals(cID)) {
				return true;
			}
		}
		return false; //Catch is always false
	}
	
	/**
	 * @param serverID
	 * @return Server object from database if it exists or a new object that will need to be saved later.
	 */
	public static Server getServer(String serverID) {
		ServerRepository SR = GambleBot.getContext().getBean(ServerRepository.class);
		Server server;
		if(SR.existsById(serverID)) {
			server = SR.getById(serverID);
		} else {
			server = new Server(serverID);
		}
		return server;
	}
	
	/**
	 * @param playerID
	 * @return Player object from database if it exists or a new object with a 0.0 balance that will need to be saved later.
	 */
	public static Player getPlayer(String playerID) {
		PlayerRepository PR = GambleBot.getContext().getBean(PlayerRepository.class);
		Player player;
		if(PR.existsById(playerID)) {
			player = PR.getById(playerID);
		} else {
			player = new Player(playerID, 0.0);
		}
		return player;
	}
	
	/**
	 * Shorter method to get command prefix, 
	 * also removes extra magic variables; only this and GambleBot's string needs changes
	 * @return
	 */
	public static String getPrefix() {
		return ConfigHelper.getOptionFromFile("DISCORD_PREFIX");
	}
	
	/**
	 * Convert an arraylist of items into an embed.
	 * @param items
	 * @param title 
	 * @param thumbnail mvm or crate
	 * @return
	 */
	public static MessageEmbed itemsToEmbed(Player player, ArrayList<Item> items, String title, String thumbnail) {
		EmbedBuilder EB = new EmbedBuilder();
		User owner = GambleBot.getJDA().getUserById(player.getId());
		EB.setColor(new Color(255,215,0));
		if(owner == null) {
			EB.setAuthor("");
		} else {
			EB.setAuthor(owner.getName());
		}
		//TODO maybe figure out how to add avatar pfp
		EB.setTitle(title);
		if(thumbnail.equalsIgnoreCase("mvm")) {
			EB.setThumbnail(TICKET_URL);
		} else if(thumbnail.equalsIgnoreCase("crate")) {
			EB.setThumbnail(KEY_URL);
		}
		//Off chance user has no pro fabs
		if(items.isEmpty()) {
			EB.addField("Unlucky! Did not recieve any stranges or pro ks fabs.", "", false);
			return EB.build();
		}
		String itemText = "";
		String emote = "";
		for(Item item : items) {
			if(Tour.australiumEmotes.containsKey(item.getName())) {
				emote = Tour.australiumEmotes.get(item.getName());
			}
			if(item.getKillstreakTier() == 3) {
				emote += ConfigHelper.getOptionFromFile("PRO_KS_EMOTE");
			}
			itemText += "• " + emote + " " + item.toDiscordString() + "\n";
			emote = "";
		}
		//Make sure message is valid size.
		EB.addField("", itemText, false);
		int i = 1;
		while(!EB.isValidLength()) {
			itemText = "";
			EB.clearFields();
			for(int j = 0; j < items.size()-i; j++) {
				if(Tour.australiumEmotes.containsKey(items.get(j).getName())) {
					emote = Tour.australiumEmotes.get(items.get(j).getName());
				}
				if(items.get(j).getKillstreakTier() == 3) {
					emote += ConfigHelper.getOptionFromFile("PRO_KS_EMOTE");
				}
				itemText += "• " + emote + " " + items.get(j).toDiscordString() + "\n";
				emote = "";
			}
			i++;
		}
		return EB.build();
	}

}
