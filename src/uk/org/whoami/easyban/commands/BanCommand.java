package uk.org.whoami.easyban.commands;

import java.text.DateFormat;
import java.util.Calendar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.util.Subnet;

public class BanCommand extends EasyBanCommand {
    
    private DataSource database;
    
    public BanCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
                return;
            }
            String playerNick = args[0];
            Player player = cs.getServer().getPlayer(playerNick);
            String reason = null;
            Calendar until = null;

            if (player != null) {
                playerNick = player.getName();
            }

            if (args.length > 1) {
                int to = args.length - 1;
                if (Subnet.isParseableInteger(args[args.length - 1])) {
                    until = Calendar.getInstance();
                    int min = Integer.parseInt(args[args.length - 1]);
                    until.add(Calendar.MINUTE, min);
                } else {
                    to = args.length;
                }
                
                String tmp = "";
                for (int i = 1; i < to; i++) {
                    tmp += args[i] + " ";
                }
                if (tmp.length() > 0) {
                    reason = tmp;
                }
            }

            if (player != null) {
                String kickmsg = m._("You have been banned by ") + admin;
                if (reason != null) {
                    kickmsg += " " + m._("Reason: ") + reason;
                }
                if (until != null) {
                    kickmsg += " " + m._("Until: ") + DateFormat.getDateTimeInstance().format(until.getTime());
                }
                player.kickPlayer(kickmsg);
            }
            database.banNick(playerNick, admin, reason, until);
            cs.getServer().broadcastMessage(playerNick + m._(" has been banned"));
            ConsoleLogger.info(playerNick + " has been banned by " + admin);
    }
    
}
