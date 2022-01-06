package bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import beans.Item;
import beans.Player;
import ch.qos.logback.classic.Logger;
import discord.commands.*;
import files.ConfigHelper;
import model.Crate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

@SpringBootApplication
@EntityScan("beans")
@EnableJpaRepositories("repository")
public class GambleBot {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(GambleBot.class);
	
	private static ApplicationContext context;
	private static JDA jda;

	public static void main(String[] args) {
		//Setup config
		ConfigHelper.buildEmptyConfig();
		ConfigHelper.setOptionToFile("DISCORD_TOKEN", "", false);
		ConfigHelper.setOptionToFile("DISCORD_PREFIX", "g.", false);
		ConfigHelper.setOptionToFile("DISCORD_OWNER", "", false);
		ConfigHelper.setOptionToFile("PRO_KS_EMOTE", "", false);
		ConfigHelper.setOptionToFile("UNUSUAL_EMOTE", "", false);
		ConfigHelper.setOptionToFile("UNUSUALIFIER_EMOTE", "", false);
		ConfigHelper.setOptionToFile("STRANGE_EMOTE", "", false);
		
		
		/*ArrayList<Crate> cases = Crate.getCases();
		HashMap<Integer, Integer> distr = new HashMap<Integer, Integer>() {{
			put(0,0);
			put(1,0);
			put(2,0);
			put(3,0);
			put(4,0);
			put(5,0);
			put(6,0);
			put(7,0);
			put(8,0);
			put(9,0);
		}};
		ArrayList<String> bCases = new ArrayList<String>() {{
			add("Creepy Crawly Case");
			add("Crimson Cache Case");
			add("Violet Vermin Case");
			add("Wicked Windfall Case");
		}};
		int j = 0;
		for(Crate c : cases) {
			if(c.getName().contains("Winter") || bCases.contains(c.getName())) {continue;}
			for(int i = 0; i < 5000; i ++) {
				j++;
				ArrayList<Item> bItems = c.drawBonusItems(new Player("jh34", 0.0));
				distr.put(bItems.size(), distr.get(bItems.size())+1);
				for(Item item : bItems) {
					if(item.getQuality().equals("Unusual")) {
						log.debug("Unusual item got: " + item.toDiscordString());
					}
				}
			}
		}
		System.out.println("Total cases opened: " + j);
		for(Entry<Integer, Integer> e : distr.entrySet()) {
			double p = e.getValue().doubleValue()/Double.parseDouble(Integer.toString(j));
			p = Math.round(p*10000)/(double)100;
			System.out.println("Bonus drops: " + e.getKey() + "  Amount: " + e.getValue() + " (" + p + "%)");
		}
		
		if(1 == 1) {
			return;
		}*/
		
		//Start spring application
		context = new SpringApplicationBuilder(GambleBot.class)
				.run(args);
		
		//Setup discord commands
		CommandClientBuilder commandBuilder = new CommandClientBuilder();
		commandBuilder.setPrefix(Utils.getPrefix());
		commandBuilder.setOwnerId(ConfigHelper.getOptionFromFile("DISCORD_OWNER"));
		commandBuilder.addCommand(new SetMvmChannelCommand());
		commandBuilder.addCommand(new RemoveMvmChannelCommand());
		commandBuilder.addCommand(new SetCrateChannelCommand());
		commandBuilder.addCommand(new RemoveCrateChannelCommand());
		commandBuilder.addCommand(new MvmTourCommand());
		commandBuilder.addCommand(new MvmStatsCommand());
		commandBuilder.addCommand(new GetMvmOddsCommand());
		commandBuilder.addCommand(new GetCrateOddsCommand());
		commandBuilder.addCommand(new UnboxCrateCommand());
		commandBuilder.addCommand(new GetPlayerBalance());
		commandBuilder.addCommand(new GetCrateNamesCommand());
		commandBuilder.addCommand(new GetPlayerUnusualsCommand());
		commandBuilder.addCommand(new GetPlayerAustraliumsCommand());
		commandBuilder.addCommand(new GetPlayerPansCommand());
		CommandClient commandClient = commandBuilder.build();
		
		//Launch discord bot
		JDABuilder builder = JDABuilder.createDefault(ConfigHelper.getOptionFromFile("DISCORD_TOKEN"));
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
		builder.addEventListeners(commandClient);
		
		try {
			jda = builder.build();	
		} catch (Exception e) {
			log.error("Error logging in.");
			e.printStackTrace();
		} 
		
		//SchemaHelper.fixSchema("itemSchema.json", "itemSchema2.json");
		//SchemaHelper.fixSchema("items_game.json", "itemSchema2.json");
		
		/*ConfigurableApplicationContext ctx = new SpringApplicationBuilder(GambleBot.class)
												.run(args);
		PlayerRepository PR = ctx.getBean(PlayerRepository.class);
		//PR.save(new player); 
		Player p = PR.getById("jh34");
		p.setBalance(20.0);
		PR.save(p); 
		
		String s = "100010010001000110010100001100";
		System.out.println(s.substring(30));
		System.out.println(Inputs.inputsToString(3, Integer.parseInt("100010010001000110010100001100", 2))); */

	}
	
	/**
	 * @return instance of our application context
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * Possibly not needed
	 * @return instance of our JDA
	 */
	public static JDA getJDA() {
		return jda;
	}
}
