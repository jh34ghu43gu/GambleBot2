package bot;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import ch.qos.logback.classic.Logger;
import discord.commands.*;
import files.ConfigHelper;
import files.SchemaHelper;
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
		ConfigHelper.setOptionToFile("BACKPACK_API_KEY", "", false);
		
		//Start spring application
		context = new SpringApplicationBuilder(GambleBot.class)
				.run(args);
		
		GamblerManager gManager = new GamblerManager();
		//Setup discord commands
		CommandClientBuilder commandBuilder = new CommandClientBuilder();
		commandBuilder.setPrefix(Utils.getPrefix());
		commandBuilder.setOwnerId(ConfigHelper.getOptionFromFile("DISCORD_OWNER"));
		commandBuilder.addCommand(new SetMvmChannelCommand());
		commandBuilder.addCommand(new RemoveMvmChannelCommand());
		commandBuilder.addCommand(new SetCrateChannelCommand());
		commandBuilder.addCommand(new RemoveCrateChannelCommand());
		commandBuilder.addCommand(new SetMiscChannelCommand());
		commandBuilder.addCommand(new RemoveMiscChannelCommand());
		commandBuilder.addCommand(new MvmTourCommand());
		commandBuilder.addCommand(new MvmStatsCommand());
		commandBuilder.addCommand(new GetMvmOddsCommand());
		commandBuilder.addCommand(new GetCrateOddsCommand());
		commandBuilder.addCommand(new UnboxCrateCommand(gManager));
		commandBuilder.addCommand(new GetPlayerBalance());
		commandBuilder.addCommand(new GetCrateNamesCommand(gManager));
		commandBuilder.addCommand(new GetCaseNamesCommand(gManager));
		commandBuilder.addCommand(new GetPlayerUnusualsCommand());
		commandBuilder.addCommand(new GetPlayerAustraliumsCommand());
		commandBuilder.addCommand(new GetPlayerPansCommand());
		commandBuilder.addCommand(new UnboxUnusualCommand(gManager));
		commandBuilder.addCommand(new UnboxEliteCommand(gManager));
		commandBuilder.addCommand(new UpdatePricelistCommand());
		commandBuilder.addCommand(new CraftHatsCommand());
		commandBuilder.addCommand(new GenerationValueReportCommand());
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
