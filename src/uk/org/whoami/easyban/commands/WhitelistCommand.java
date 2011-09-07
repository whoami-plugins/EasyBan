package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;

public class WhitelistCommand extends EasyBanCommand {

    private DataSource database;

    public WhitelistCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        database.whitelist(args[0]);
        cs.getServer().broadcastMessage(args[0] + m._(" has been whitelisted"));
        ConsoleLogger.info(admin + " whitelisted " + args[0]);
    }
}
