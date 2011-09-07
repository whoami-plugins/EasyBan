package uk.org.whoami.easyban.permission;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Permission {

    private static PermissionHandler handler;

    public static boolean hasPermission(CommandSender sender, String cmd) {
        boolean perm = sender.hasPermission("easyban." + cmd.toLowerCase());
        
        if (handler == null) {
            Plugin permissionsPlugin = sender.getServer().getPluginManager().getPlugin("Permissions");
            if (permissionsPlugin != null) {
                handler = ((Permissions) permissionsPlugin).getHandler();
            }
        }
        
        if ((sender instanceof Player) && handler != null) {
            perm = handler.has((Player) sender, "easyban." + cmd.toLowerCase());
        }

        return perm;
    }
}
