package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class ListWhitelistCommand extends EasyBanCommand {

    private DataSource database;

    public ListWhitelistCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        cs.sendMessage(m._("Whitelist: "));
        this.sendListToSender(cs, database.getWhitelistedNicks());
    }
}
