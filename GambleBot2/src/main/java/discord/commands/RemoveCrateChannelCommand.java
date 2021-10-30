package discord.commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Server;
import bot.Utils;
import ch.qos.logback.classic.Logger;

public class RemoveCrateChannelCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(RemoveCrateChannelCommand.class);
	
	
	public RemoveCrateChannelCommand() {
		this.name = "removecrate";
		this.help = "Remove any existing crate command channel restriction. \n"
				+ "Can only be run by the server owner.";
		this.ownerCommand = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		String sID = event.getGuild().getId();
		log.debug("Server: " + sID + " is attempting to clear crate channel restrictions.");
		Server server = Utils.getServer(sID);
		server.setMvmChannel("");
		server.save();
		event.getChannel().sendMessage("Crate/case command restrictions have been lifted. Use " + Utils.getPrefix() + "setcrate in a channel to have them restricted.").queue();
	}

}
