package uk.org.whoami.easyban.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class AlternativeCommand extends EasyBanCommand {
    
    private DataSource database;

    public AlternativeCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
                return;
            }
            try {
                InetAddress.getByName(args[0]);
                cs.sendMessage(m._("Users who connected from IP") + args[0]);
                this.sendListToSender(cs, database.getNicks(args[0]));
            } catch (UnknownHostException ex) {
                ArrayList<String> nicks = new ArrayList<String>();

                for (String ip : database.getHistory(args[0])) {
                    Collections.addAll(nicks, database.getNicks(ip));
                }
                cs.sendMessage(m._("Alternative nicks of ") + args[0]);
                this.sendListToSender(cs, nicks.toArray(new String[0]));
            }
    }
    
}
