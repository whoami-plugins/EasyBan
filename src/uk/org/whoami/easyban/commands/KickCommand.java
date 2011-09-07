package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.easyban.ConsoleLogger;

public class KickCommand extends EasyBanCommand {

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }

        Player player = cs.getServer().getPlayer(args[0]);
        if (player != null) {
            String name = player.getDisplayName();
            
            String kickmsg = m._("You have been kicked");
            if(args.length > 1) {
                kickmsg += " " + m._("Reason: ");
                for (int i = 1; i < args.length; i++) {
                    kickmsg += args[i] + " ";
                }
            }
            player.kickPlayer(kickmsg);
            cs.getServer().broadcastMessage(name + m._(" has been kicked"));
            ConsoleLogger.info(player.getName() + " has been kicked by " + admin);
        }
    }
}
