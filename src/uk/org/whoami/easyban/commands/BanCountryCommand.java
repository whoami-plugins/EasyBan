package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;

public class BanCountryCommand extends EasyBanCommand {

    private DataSource database;

    public BanCountryCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        database.banCountry(args[0]);
        cs.getServer().broadcastMessage(m._("A country has been banned: ")
                + args[0]);
        ConsoleLogger.info(admin + " banned the country " + args[0]);
    }
}
