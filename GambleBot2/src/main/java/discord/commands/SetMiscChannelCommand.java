package discord.commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import beans.Server;
import bot.Utils;
import ch.qos.logback.classic.Logger;

public class SetMiscChannelCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(SetMiscChannelCommand.class);
	
	
	public SetMiscChannelCommand() {
		this.name = "setmisc";
		this.help = "Set the current channel to be the restricted channel for misc related commands. \n"
				+ "Can only be run by the server owner.";
		this.ownerCommand = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		String sID = event.getGuild().getId();
		String cID = event.getChannel().getId();
		log.debug("Server: " + sID + " is attempting to change misc channel to " + cID);
		Server server = Utils.getServer(sID);
		server.setMiscChannel(cID);
		server.save();
		event.getChannel().sendMessage("Restricted all misc commands to this channel. Use " + Utils.getPrefix() + "removemisc to remove the restriction.").queue();
	}

}
