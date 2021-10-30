package discord.commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Server;
import bot.Utils;
import ch.qos.logback.classic.Logger;

public class SetMvmChannelCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(SetMvmChannelCommand.class);
	
	
	public SetMvmChannelCommand() {
		this.name = "setmvm";
		this.help = "Set the current channel to be the restricted channel for mvm related commands. \n"
				+ "Can only be run by the server owner.";
		this.ownerCommand = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		String sID = event.getGuild().getId();
		String cID = event.getChannel().getId();
		log.debug("Server: " + sID + " is attempting to change mvm channel to " + cID);
		Server server = Utils.getServer(sID);
		server.setMvmChannel(cID);
		server.save();
		event.getChannel().sendMessage("Restricted all mvm commands to this channel. Use " + Utils.getPrefix() + "removemvm to remove the restriction.").queue();
	}

}
