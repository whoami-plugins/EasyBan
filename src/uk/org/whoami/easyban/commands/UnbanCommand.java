/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;

/**
 *
 * @author kart0ffelsack
 */
public class UnbanCommand extends EasyBanCommand {

    private DataSource database;

    public UnbanCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        database.unbanNick(args[0]);
        cs.getServer().broadcastMessage(args[0]
                + m._(" has been unbanned"));
        ConsoleLogger.info(args[0] + " has been unbanned");
    }
}
