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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
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
    private Message msg;
    private static final Logger log = Logger.getLogger("Minecraft");

    private void initConfig() {
        if(this.getConfiguration().getProperty("database") == null) {
            this.getConfiguration().setProperty("database", "yaml");
            this.getConfiguration().save();
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        database.close();
    }

    @Override
    public void onEnable() {
        initConfig();
        setupPermission();
        msg = Message.getInstance(this.getConfiguration());
        if(this.getConfiguration().getProperty("database").equals("yaml")) {
            database = new YamlDatasource(this);
        } else {
            this.getServer().getPluginManager().disablePlugin(this);
        }
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN,
                new EasyBanPlayerListener(database),
                Event.Priority.Low, this);
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                new UnbanTask(database), 60L, 1200L);
        log.info("[EasyBan] EasyBan " + this.getDescription().getVersion()
                 + " enabled");
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
                        getDisplayName() + msg._(" has been kicked"));
                player.kickPlayer(msg._("You have been kicked"));
                log.info("[EasyBan] " + player.getName()
                         + " has been kicked by " + admin);
            }
            return true;
        }

        if(label.equalsIgnoreCase("eban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            String playerNick = args[0];
            Player player = this.getServer().getPlayer(playerNick);

            if(player != null) {
                playerNick = player.getName();
            }

            if(args.length == 1) {
                database.banNick(playerNick, admin, null, null);
            } else {
                int to = args.length - 1;
                Integer min = null;
                try {
                    min = Integer.parseInt(args[args.length - 1]);
                } catch(NumberFormatException ex) {
                    to = args.length;
                }

                String reason = "";
                for(int i = 1; i < to; i++) {
                    reason += args[i] + " ";
                }
                if(reason.equals("")) {
                    reason = null;
                }

                if(min == null) {
                    database.banNick(playerNick, admin, reason, null);
                    if(player != null) {
                        player.kickPlayer(msg._("You have been banned"));
                    }
                    log.info("[EasyBan] " + playerNick + " has been banned by "
                             + admin);
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, min);
                    database.banNick(playerNick, admin, reason, cal.
                            getTimeInMillis());
                    if(player != null) {
                        player.kickPlayer("You are banned until: " + DateFormat.
                                getDateTimeInstance().format(cal.getTime()));
                    }
                    log.info("[EasyBan] Temporary ban for " + playerNick);
                }
                this.getServer().broadcastMessage(ChatColor.RED + playerNick
                                                  + msg._(" has been banned"));
            }
            return true;
        }

        if(label.equalsIgnoreCase("ebaninfo")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            HashMap<String, String> info = database.getBanInformation(args[0]);
            if(info == null) {
                sender.sendMessage(args[0] + msg._(" is not banned"));
                return true;
            }

            sender.sendMessage(args[0] + msg._(" is banned"));
            if(info.containsKey("admin")) {
                sender.sendMessage(msg._("Admin: ") + info.get("admin"));
            }
            if(info.containsKey("reason")) {
                sender.sendMessage(msg._("Reason: ") + info.get("reason"));
            }
            if(info.containsKey("until")) {
                Date until = new Date(new Long(info.get("until")));
                sender.sendMessage(msg._("Until: ") + DateFormat.
                        getDateTimeInstance().format(until));
            }
            return true;
        }

        if(label.equalsIgnoreCase("elistbans")) {
            if(!perm) {
                return true;
            }

            sender.sendMessage(msg._("Banned players: "));
            this.sendListToSender(sender, database.getBannedNicks());
            return true;
        }

        if(label.equalsIgnoreCase("elistsubnets")) {
            if(!perm) {
                return true;
            }

            sender.sendMessage(msg._("Banned subnets: "));
            this.sendListToSender(sender, database.getBannedSubnets());
            return true;
        }

        if(label.equalsIgnoreCase("elistips")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            sender.sendMessage(msg._("Ips from " + args[0]));
            this.sendListToSender(sender, database.getPlayerIps(args[0]));
            return true;
        }

        if(label.equalsIgnoreCase("eunban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.unbanNick(args[0]);
            this.getServer().broadcastMessage(args[0] + msg._(
                    " has been unbanned"));
            log.info("[EasyBan] " + args[0] + " has been unbanned");
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
                    database.banSubnet(subnet, admin, null);
                } else {
                    String reason = "";
                    for(int i = 1; i < args.length; i++) {
                        reason += args[i] + " ";
                    }
                    database.banSubnet(subnet, admin, reason);
                }
                this.getServer().broadcastMessage(ChatColor.RED + subnet.
                        toString() + msg._(" has been banned"));
                log.info("[EasyBan] " + subnet.toString()
                         + " has been banned by " + admin);
            } else {
                sender.sendMessage(msg._("Invalid Subnet"));
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
                this.getServer().broadcastMessage(ChatColor.RED + args[0] + msg.
                        _(" has been unbanned"));
                log.info("[EasyBan] " + args[0] + " has been unbanned by "
                         + admin);
                return true;
            } else {
                sender.sendMessage(msg._("Invalid Subnet"));
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
