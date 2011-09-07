package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class ListBansCommand extends EasyBanCommand {

    private DataSource database;

    public ListBansCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        cs.sendMessage(m._("Banned players: "));
        this.sendListToSender(cs, database.getBannedNicks());
    }
}
