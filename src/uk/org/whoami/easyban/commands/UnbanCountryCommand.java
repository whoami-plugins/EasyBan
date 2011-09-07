package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;

public class UnbanCountryCommand extends EasyBanCommand {

    private DataSource database;

    public UnbanCountryCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        database.unbanCountry(args[0]);
        cs.getServer().broadcastMessage(m._("A country has been unbanned: ") + args[0]);
        ConsoleLogger.info(admin + " unbanned the country " + args[0]);
    }
}
