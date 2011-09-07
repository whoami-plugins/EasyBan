package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.util.Subnet;

public class UnbanSubnetCommand extends EasyBanCommand {
    
    private DataSource database;

    public UnbanSubnetCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
                return;
            }
            Subnet subnet = null;
            try {
                subnet = new Subnet(args[0]);
            } catch (IllegalArgumentException ex) {
            }

            if (subnet != null) {
                database.unbanSubnet(subnet);
                cs.getServer().broadcastMessage(args[0] + m._(" has been unbanned"));
                ConsoleLogger.info(args[0] + " has been unbanned by " + admin);
            } else {
                cs.sendMessage(m._("Invalid Subnet"));
            }
    }
    
}
