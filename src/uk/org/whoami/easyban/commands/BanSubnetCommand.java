/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.util.Subnet;

/**
 *
 * @author kart0ffelsack
 */
public class BanSubnetCommand extends EasyBanCommand {

    private DataSource database;

    public BanSubnetCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        Subnet subnet = null;
        try {
            subnet = new Subnet(args[0]);
        } catch (IllegalArgumentException ex) {
        }

        if (subnet != null) {
            if (args.length == 1) {
                database.banSubnet(subnet, admin, null);
            } else {
                String reason = "";
                for (int i = 1; i < args.length; i++) {
                    reason += args[i] + " ";
                }
                database.banSubnet(subnet, admin, reason);
            }
            cs.getServer().broadcastMessage(subnet.toString() + m._(" has been banned"));
            ConsoleLogger.info(subnet.toString() + " has been banned by "
                    + admin);
        } else {
            cs.sendMessage(m._("Invalid Subnet"));
        }
    }
}
