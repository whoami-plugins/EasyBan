package uk.org.whoami.easyban.commands;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class BanInfoCommand extends EasyBanCommand {
    
    private DataSource database;

    public BanInfoCommand(DataSource database) {
        this.database = database;
    }
    
    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
                return;
            }

            HashMap<String, String> info = database.getBanInformation(args[0]);
            if (info == null) {
                cs.sendMessage(args[0] + m._(" is not banned"));
                return;
            }

            cs.sendMessage(args[0] + m._(" is banned"));
            if (info.containsKey("admin")) {
                cs.sendMessage(m._("Admin: ") + info.get("admin"));
            }
            if (info.containsKey("reason")) {
                cs.sendMessage(m._("Reason: ") + info.get("reason"));
            }
            if (info.containsKey("until")) {
                Date until = new Date(new Long(info.get("until")));
                cs.sendMessage(m._("Until: ") + DateFormat.getDateTimeInstance().format(until));
            }
    }
}
