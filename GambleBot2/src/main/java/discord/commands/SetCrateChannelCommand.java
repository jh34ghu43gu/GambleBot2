package discord.commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Server;
import bot.Utils;
import ch.qos.logback.classic.Logger;

public class SetCrateChannelCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(SetCrateChannelCommand.class);
	
	
	public SetCrateChannelCommand() {
		this.name = "setcrate";
		this.help = "Set the current channel to be the restricted channel for crate/case related commands. \n"
				+ "Can only be run by the server owner.";
		this.ownerCommand = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		String sID = event.getGuild().getId();
		String cID = event.getChannel().getId();
		log.debug("Server: " + sID + " is attempting to change crate channel to " + cID);
		Server server = Utils.getServer(sID);
		server.setCrateChannel(cID);
		server.save();
		event.getChannel().sendMessage("Restricted all crate/case commands to this channel. Use " + Utils.getPrefix() + "removecrate to remove the restriction.").queue();
	}

}
