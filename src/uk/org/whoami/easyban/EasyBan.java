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

import java.net.UnknownHostException;
import uk.org.whoami.easyban.datasource.Datasource;
import uk.org.whoami.easyban.datasource.YamlDatasource;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.net.InetAddress;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.easyban.util.Subnet;

public class EasyBan extends JavaPlugin {

    private Datasource database;
    private PermissionHandler permissionHandler;

    private void initConfig() {
        if(this.getConfiguration().getProperty("database") == null) {
            this.getConfiguration().setProperty("database", "yaml");
            this.getConfiguration().save();
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
        if(this.getConfiguration().getProperty("database").equals("yaml")) {
            database = new YamlDatasource(this);
        } else {
            this.getServer().getPluginManager().disablePlugin(this);
        }
        Message.loadDefaults(this.getConfiguration());
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN,
                                                          new EasyBanPlayerListener(
                database, this), Event.Priority.Low, this);
        System.out.println(Message.getMessage("EasyBan enabled", this.
                getConfiguration()) + " Version: " + this.getDescription().
                getVersion());

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        boolean perm = true;
        String admin = "Console";
        if(sender instanceof Player) {
            if(permissionHandler == null || !permissionHandler.has(
                    (Player) sender, "easyban." + label)) {
                perm = false;
            }
            admin = ((Player) sender).getName();
        }

        if(sender.isOp()) {
            perm = true;
        }

        if(label.equalsIgnoreCase("ekick")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            Player player = this.getServer().getPlayer(args[0]);
            if(player != null) {
                this.getServer().broadcastMessage(ChatColor.RED + player.
                        getDisplayName() + Message.getMessage("has been kicked",
                                                              this.
                        getConfiguration()));
                player.kickPlayer(Message.getMessage("You have been kicked",
                                                     this.getConfiguration()));
            }
            return true;
        }

        if(label.equalsIgnoreCase("eban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            String playerNick = args[0];
            Player player = this.getServer().getPlayer(playerNick);

            if(player == null) {
                this.getServer().broadcastMessage(ChatColor.RED + args[0]
                                                  + Message.getMessage(
                        "has been banned", this.getConfiguration()));
            } else {
                playerNick = player.getName();
                this.getServer().broadcastMessage(ChatColor.RED + player.
                        getDisplayName() + Message.getMessage("has been banned",
                                                              this.
                        getConfiguration()));
                player.kickPlayer(Message.getMessage("You have been banned",
                                                     this.getConfiguration()));
            }

            if(args.length == 1) {
                database.banNick(playerNick, admin);
            } else {
                String reason = "";
                for(int i = 1; i < args.length; i++) {
                    reason += args[i] + " ";
                }
                database.banNick(playerNick, admin, reason);
            }

            return true;
        }

        if(label.equalsIgnoreCase("ebaninfo")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            String[] info = database.getBanInformation(args[0]);

            switch(info.length) {
                case 0:
                    sender.sendMessage(args[0] + Message.getMessage(
                            " is not banned", this.getConfiguration()));
                    break;
                case 1:
                    sender.sendMessage(info[0] + Message.getMessage(" is banned",
                                                                    this.
                            getConfiguration()));
                    break;
                case 2:
                    sender.sendMessage(info[0] + Message.getMessage(
                            " was banned by ", this.getConfiguration())
                                       + info[1]);
                    break;
                case 3:
                    sender.sendMessage(info[0] + Message.getMessage(
                            " was banned by ", this.getConfiguration())
                                       + info[1]);
                    sender.sendMessage(Message.getMessage("Reason: ", this.
                            getConfiguration()) + info[2]);
                    break;
            }
            return true;
        }

        if(label.equalsIgnoreCase("elistbans")) {
            if(!perm) {
                return true;
            }

            sender.sendMessage(Message.getMessage("Banned players", this.
                    getConfiguration()) + ":");
            this.sendListToSender(sender, database.getBannedNicks());
            return true;
        }

        if(label.equalsIgnoreCase("elistsubnets")) {
            if(!perm) {
                return true;
            }

            sender.sendMessage(Message.getMessage("Banned subnets", this.
                    getConfiguration()) + ":");
            this.sendListToSender(sender, database.getBannedSubnets());
            return true;
        }

        if(label.equalsIgnoreCase("elistips")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            sender.sendMessage(Message.getMessage("Ips from", this.
                    getConfiguration()) + " " + args[0]);
            this.sendListToSender(sender, database.getPlayerIps(args[0]));
            return true;
        }

        if(label.equalsIgnoreCase("eunban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.unbanNick(args[0]);
            this.getServer().broadcastMessage(args[0] + Message.getMessage(
                    "has been unbanned", this.getConfiguration()));
            return true;
        }

        if(label.equalsIgnoreCase("esubnetban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            String[] sub = args[0].split("/");
            Subnet subnet = null;
            if(sub.length == 2) {
                try {
                    subnet = new Subnet(InetAddress.getByName(sub[0]), Integer.
                            parseInt(sub[1]));
                } catch(UnknownHostException ex) {
                } catch(NumberFormatException ex) {
                    try {
                        subnet = new Subnet(InetAddress.getByName(sub[0]),
                                            InetAddress.getByName(sub[1]));
                    } catch(UnknownHostException ex1) {
                    }
                }
            }
            if(subnet != null) {
                if(args.length == 1) {
                    database.banSubnet(subnet, admin);
                } else {
                    String reason = "";
                    for(int i = 1; i < args.length; i++) {
                        reason += args[i] + " ";
                    }
                    database.banSubnet(subnet, admin, reason);
                }
                this.getServer().broadcastMessage(ChatColor.RED + subnet.
                        toString() + Message.getMessage("has been banned", this.
                        getConfiguration()));
            } else {
                sender.sendMessage(Message.getMessage("Invalid Subnet", this.
                        getConfiguration()));
            }

            return true;
        }

        if(label.equalsIgnoreCase("esubnetunban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            String[] sub = args[0].split("/");
            Subnet subnet = null;
            if(sub.length == 2) {
                try {
                    subnet = new Subnet(InetAddress.getByName(sub[0]),
                                        Integer.parseInt(sub[1]));
                } catch(UnknownHostException ex) {
                } catch(NumberFormatException ex) {
                    try {
                        subnet = new Subnet(InetAddress.getByName(sub[0]),
                                            InetAddress.getByName(sub[1]));
                    } catch(UnknownHostException ex1) {
                    }
                }
            }
            if(subnet != null) {
                database.unbanSubnet(subnet);
                this.getServer().broadcastMessage(ChatColor.RED + args[0]
                                                  + Message.getMessage(
                        "has been unbanned", this.getConfiguration()));
                return true;
            } else {
                sender.sendMessage(Message.getMessage("Invalid Subnet", this.
                        getConfiguration()));
            }
        }
        return false;
    }

    private void sendListToSender(CommandSender sender, String[] list) {
        for(int i = 0; i < list.length; i += 4) {
            String send = "";
            for(int y = 0; y < 4; y++) {
                if(i + y < list.length) {
                    send += (list[i + y] + ", ");
                } else {
                    break;
                }
            }
            sender.sendMessage(send);
        }
    }

    private void setupPermission() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().
                getPlugin("Permissions");
        if(permissionsPlugin != null) {
            permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        }
    }
}
