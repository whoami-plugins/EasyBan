package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class HistoryCommand extends EasyBanCommand {

    private DataSource database;

    public HistoryCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        cs.sendMessage(m._("Ips from ") + args[0]);
        this.sendListToSender(cs, database.getHistory(args[0]));
    }
}
