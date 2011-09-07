package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.easyban.settings.Message;
import uk.org.whoami.easyban.permission.Permission;


public abstract class EasyBanCommand implements CommandExecutor {
    
    protected String admin;
    protected Message m = Message.getInstance();
    
    protected abstract void execute(CommandSender cs, Command cmnd, String cmd, String[] args);
       
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String cmd, String[] args) {
        admin = cs instanceof Player ? ((Player) cs).getName() : "Console";
        
        if(Permission.hasPermission(cs, cmd)) {
            this.execute(cs, cmnd, cmd, args);
        }                
        return true;
    }
    
    protected final void sendListToSender(CommandSender sender, String[] list) {
        for (int i = 0; i < list.length; i += 4) {
            String send = "";
            for (int y = 0; y < 4; y++) {
                if (i + y < list.length) {
                    send += (list[i + y] + ", ");
                } else {
                    break;
                }
            }
            sender.sendMessage(send);
        }
    }
}
