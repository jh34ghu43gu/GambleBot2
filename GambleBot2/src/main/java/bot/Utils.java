package bot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Item;
import beans.Leaderboard;
import beans.Player;
import beans.Server;
import files.ConfigHelper;
import model.Tour;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import repository.LeaderboardRepository;
import repository.PlayerRepository;
import repository.ServerRepository;

/**
 * Utility class for static methods used in multiple other classes
 * @author jh34ghu43gu
 */
public class Utils {
	
	private static final String TICKET_URL = "https://wiki.teamfortress.com/w/images/9/9b/Backpack_Tour_of_Duty_Ticket.png";
	private static final String KEY_URL = "https://wiki.teamfortress.com/w/images/8/83/Backpack_Mann_Co._Supply_Crate_Key.png";
	private static final int MAX_BOARDS = 100;
	
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
		if(thumbnail.equalsIgnoreCase("mvm")) {
			EB.setThumbnail(TICKET_URL);
		} else if(thumbnail.equalsIgnoreCase("crate")) {
			EB.setThumbnail(KEY_URL);
		}
		//Off chance user didn't get enough stuff
		if(items.isEmpty()) {
			if(thumbnail.equalsIgnoreCase("mvm")) {
				EB.addField("Unlucky! Did not recieve any stranges or pro ks fabs.", "", false);
			} else if(thumbnail.equalsIgnoreCase("crate")) {
				EB.addField("Unlucky! Did not recieve enough significant items to display.", "", false);
			}
			return EB.build();
		}
		ArrayList<String> itemText = new ArrayList<String>();
		int itemTextIndex = 0;
		String emote = "";
		for(Item item : items) {
			if(Tour.australiumEmotes.containsKey(item.getName())) {
				emote = Tour.australiumEmotes.get(item.getName());
			}
			if(item.getKillstreakTier() == 3) {
				emote += ConfigHelper.getOptionFromFile("PRO_KS_EMOTE");
			}
			if(item.getEffect() != null) {
				emote += ConfigHelper.getOptionFromFile("UNUSUAL_EMOTE");
			} else if(item.getQuality().equals("Unusual") && item.getName().contains("Unusualifier")) {
				emote += ConfigHelper.getOptionFromFile("UNUSUALIFIER_EMOTE");
			}
			String itemStr = "• " + emote + " " + item.toDiscordString();
			if(itemText.isEmpty()) {
				itemText.add(itemStr);
			} else if(itemText.get(itemTextIndex).length()+ itemStr.length() >= 1024) {
				itemTextIndex++;
				itemText.add(itemStr);
			} else {
				itemText.set(itemTextIndex, itemText.get(itemTextIndex) + "\n" + itemStr);
			}
			emote = "";
		}
		//Make sure message is valid size.
		int i = 0;
		boolean modified = false;
		String modifiedTitle = "Could not display " + (itemText.size() - i) + " items.\n" + title;
		for(String s : itemText) {
			if(EB.length() + s.length() + modifiedTitle.length() + 4 < 6000) { //+4 for increased digits itemText.size might be
				EB.addField("", s, false);
				i++;
			} else {
				modified = true;
				break;
			}
		}
		
		
		if(modified) {
			EB.setTitle("Could not display " + (itemText.size() - i) + " items.\n" + title);
		} else {
			EB.setTitle(title);
		}
		return EB.build();
	}
	
	/**
	 * Return the arraylist of all leaderboard entries for the given event. List is sorted by value
	 * @param event "{tour OR overall} drystreak", "unusual drought"
	 * @return
	 */
	public static ArrayList<Leaderboard> getLeaderboard(String event) {
		ArrayList<Leaderboard> boards = new ArrayList<Leaderboard>();
		LeaderboardRepository LR = GambleBot.getContext().getBean(LeaderboardRepository.class);
		Optional<List<Leaderboard>> result = LR.findByEventOrderByValueAsc(event);
		if(result.isPresent()) {
			for(Leaderboard board : result.get()) {
				boards.add(board);
			}
		}
		return boards;
	}
	
	/**
	 * Attempt to add a board to database if it is a new top #MAX_BOARDS, delete bottom board if MAX_BOARDS has been reached
	 * @param board
	 * @return
	 */
	public static boolean updateLeaderboard(Leaderboard board) {
		String event = board.getEvent();
		ArrayList<Leaderboard> boards = getLeaderboard(event);
		
		//Empty board just add the event
		if(boards.isEmpty()) {
			board.save();
			return true;
		}
		
		if(boards.size() <= MAX_BOARDS) { //Can add board regardless
			board.save();
			return true;
		} else if(board.getValue() > boards.get(boards.size()-1).getValue()) {
			//Board value is higher than the lowest value and we need to remove the lowest one
			board.save();
			Leaderboard lowest = boards.get(0);
			//Select lowest one
			for(Leaderboard l : boards) {
				if(l.getValue() < lowest.getValue()) {
					lowest = l;
				} else if(l.getValue() == lowest.getValue()) { //Tie, set newest one
					if(l.getDate().compareTo(lowest.getDate()) > 0) {
						lowest = l;
					}
				}
			}
			lowest.delete();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the rank of the board relative to the event list
	 * @param board
	 * @return
	 */
	public int getLeaderboardRanking(Leaderboard board) {
		String event = board.getEvent();
		ArrayList<Leaderboard> boards = getLeaderboard(event);
		
		if(boards.isEmpty()) { //Shouldn't happen
			return -1;
		}
		
		int rank = 1;
		for(Leaderboard LB : boards) {
			if(LB.getValue() < board.getValue()) {
				rank++;
			} else if(LB.getValue() == board.getValue()) { //Tie, oldest one has rank priority
				if(LB.getDate().compareTo(board.getDate()) < 0) {
					rank++;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return rank;
	}

}
