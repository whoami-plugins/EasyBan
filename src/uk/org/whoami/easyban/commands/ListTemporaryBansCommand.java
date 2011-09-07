package uk.org.whoami.easyban.commands;

import java.text.DateFormat;
import java.util.Date;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class ListTemporaryBansCommand extends EasyBanCommand {

    private DataSource database;

    public ListTemporaryBansCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        cs.sendMessage(m._("Temporary bans: "));
        for (String key : database.getTempBans().keySet()) {
            cs.sendMessage(key + " : "
                    + DateFormat.getDateTimeInstance().format(new Date(database.getTempBans().get(key))));
        }
    }
}
