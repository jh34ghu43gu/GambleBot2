package discord.commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Server;
import bot.Utils;
import ch.qos.logback.classic.Logger;

public class RemoveMiscChannelCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(RemoveMiscChannelCommand.class);
	
	
	public RemoveMiscChannelCommand() {
		this.name = "removemisc";
		this.help = "Remove any existing misc command channel restriction. \n"
				+ "Can only be run by the server owner.";
		this.ownerCommand = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		String sID = event.getGuild().getId();
		log.debug("Server: " + sID + " is attempting to clear misc channel restrictions.");
		Server server = Utils.getServer(sID);
		server.setMiscChannel("");
		server.save();
		event.getChannel().sendMessage("Misc command restrictions have been lifted. Use " + Utils.getPrefix() + "setmisc in a channel to have them restricted.").queue();
	}

}
