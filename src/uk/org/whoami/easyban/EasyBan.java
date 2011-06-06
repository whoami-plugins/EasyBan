/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.whoami.easyban;

import uk.org.whoami.easyban.datasource.Datasource;
import uk.org.whoami.easyban.datasource.YamlDatasource;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import uk.org.whoami.easyban.util.Network;

public class EasyBan extends JavaPlugin {

    private Datasource database;
    private Configuration config;
    private PermissionHandler permissionHandler;
    private Messages msg = null;

    private void initConfig() {
        config = new Configuration(new File(this.getDataFolder(),
                "easyban.yml"));
        config.load();
        if (config.getProperty("database") == null) {
            config.setProperty("database", "yaml");
            config.save();
        }
        if(msg == null) {
            msg = new Messages(this);
        }
    }

    @Override
    public void onDisable() {
        database.close();
    }

    @Override
    public void onEnable() {
        initConfig();
        setupPermission();
        if (config.getProperty("database").equals("yaml")) {
            database = new YamlDatasource(this);
        } else {
            this.getServer().getPluginManager().disablePlugin(this);
        }
        this.getServer().getPluginManager().registerEvent(
                Event.Type.PLAYER_JOIN, new EasyBanPlayerListener(database,msg),
                Event.Priority.Low, this);
        System.out.println(msg.getMessage("EasyBan enabled"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String label, String[] args) {
        boolean perm = true;

        if (sender instanceof Player) {
            if ((permissionHandler == null && !sender.isOp())
                    || !permissionHandler.has((Player) sender, "easyban." + label)) {
                perm = false;
            }
        }
        if (label.equals("ekick")) {
            if (args.length == 0 || !perm) {
                return true;
            }
            Player player = this.getServer().getPlayer(args[0]);
            if (player != null) {
                this.getServer().broadcastMessage(ChatColor.RED
                        + player.getDisplayName()
                        + msg.getMessage("has been kicked"));
                player.kickPlayer(msg.getMessage("You have been kicked"));
            }
            return true;
        }

        if (label.equals("eban")) {
            if (args.length == 0 || !perm) {
                return true;
            }
            Player player = this.getServer().getPlayer(args[0]);
            if (player == null) {
                database.banNick(args[0]);
                this.getServer().broadcastMessage(ChatColor.RED
                        + args[0]
                        + msg.getMessage("has been banned"));
            } else {
                database.banNick(player.getName());
                this.getServer().broadcastMessage(ChatColor.RED
                        + player.getDisplayName()
                        + msg.getMessage("has been banned"));
                player.kickPlayer(msg.getMessage("You have been banned"));
            }
            return true;
        }

        if (label.equals("eunban")) {
            if (args.length == 0 || !perm) {
                return true;
            }
            database.unbanNick(args[0]);
            this.getServer().broadcastMessage(args[0]
                    + msg.getMessage("has been unbanned"));
            return true;
        }

        if (label.equals("esubnetban")) {
            if (args.length == 0 || !perm) {
                return true;
            }
            if (Network.isValidSubnet(args[0])) {
                database.banSubnet(args[0]);
                this.getServer().broadcastMessage(ChatColor.RED
                        + args[0]
                        + msg.getMessage("has been banned"));
            } else {
                sender.sendMessage(msg.getMessage("Invalid Subnet"));
            }

            return true;
        }

        if (label.equals("esubnetunban")) {
            if (args.length == 0 || !perm) {
                return true;
            }
            database.unbanSubnet(args[0]);
            this.getServer().broadcastMessage(ChatColor.RED
                        + args[0]
                        + msg.getMessage("has been unbanned"));
            return true;
        }
        return false;
    }

    private void setupPermission() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().
                getPlugin("Permissions");
        if (permissionsPlugin != null) {
            permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        }
    }
}
