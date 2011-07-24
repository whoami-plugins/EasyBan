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

import uk.org.whoami.easyban.listener.EasyBanPlayerListener;
import uk.org.whoami.easyban.tasks.UnbanTask;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.datasource.YamlDataSource;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.easyban.listener.EasyBanCountryListener;
import uk.org.whoami.easyban.util.Subnet;
import uk.org.whoami.geoip.GeoIPLookup;
import uk.org.whoami.geoip.GeoIPTools;

public class EasyBan extends JavaPlugin {

    private DataSource database;
    private PermissionHandler permissionHandler;
    private final File data = new File(this.getDataFolder(), "plugins/EasyBan/");
    private Message m;

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
        if(!data.exists()) {
            data.mkdirs();
        }
        m = Message.getInstance(data);
        m.updateMessages(this.getConfiguration());

        if(this.getConfiguration().getProperty("database").equals("yaml")) {
            database = new YamlDataSource(this);
        } else {
            this.getServer().getPluginManager().disablePlugin(this);
        }

        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN,
                new EasyBanPlayerListener(database),
                Event.Priority.Highest, this);

        GeoIPLookup geo = getGeoIPLookup();
        if(geo != null) {
            this.getServer().getPluginManager().registerEvent(
                    Event.Type.PLAYER_JOIN,
                    new EasyBanCountryListener(database, geo),
                    Event.Priority.Low, this);
        }
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                new UnbanTask(database), 60L, 1200L);
        ConsoleLogger.info("EasyBan enabled; Version: " + this.getDescription().
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
                        getDisplayName() + m._(" has been kicked"));
                player.kickPlayer(m._("You have been kicked"));
                ConsoleLogger.info(player.getName() + " has been kicked by "
                                   + admin);
            }
            return true;
        }

        if(label.equalsIgnoreCase("ehistory")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            sender.sendMessage(m._("Ips from ") + args[0]);
            this.sendListToSender(sender, database.getHistory(args[0]));
            return true;
        }

        if(label.equalsIgnoreCase("ealternative")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            ArrayList<String> nicks = new ArrayList<String>();

            for(String ip : database.getHistory(args[0])) {
                Collections.addAll(nicks, database.getNicks(ip));
            }
            sender.sendMessage(m._("Alternative nicks of ") + args[0]);
            this.sendListToSender(sender, nicks.toArray(new String[0]));
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
                if(player != null) {
                    player.kickPlayer(m._("You have been banned"));
                }
                ConsoleLogger.info(playerNick + " has been banned by " + admin);
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
                        player.kickPlayer(m._("You have been banned"));
                    }
                    ConsoleLogger.info(playerNick + " has been banned by "
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
                    ConsoleLogger.info("Temporary ban for " + playerNick);
                }
            }
            this.getServer().broadcastMessage(ChatColor.RED + playerNick
                                              + m._(" has been banned"));
            return true;
        }

        if(label.equalsIgnoreCase("eunban")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.unbanNick(args[0]);
            this.getServer().broadcastMessage(args[0]
                                              + m._(" has been unbanned"));
            ConsoleLogger.info(args[0] + " has been unbanned");
            return true;
        }

        if(label.equalsIgnoreCase("ebaninfo")) {
            if(args.length == 0 || !perm) {
                return true;
            }

            HashMap<String, String> info = database.getBanInformation(args[0]);
            if(info == null) {
                sender.sendMessage(args[0] + m._(" is not banned"));
                return true;
            }

            sender.sendMessage(args[0] + m._(" is banned"));
            if(info.containsKey("admin")) {
                sender.sendMessage(m._("Admin: ") + info.get("admin"));
            }
            if(info.containsKey("reason")) {
                sender.sendMessage(m._("Reason: ") + info.get("reason"));
            }
            if(info.containsKey("until")) {
                Date until = new Date(new Long(info.get("until")));
                sender.sendMessage(m._("Until: ") + DateFormat.
                        getDateTimeInstance().format(until));
            }
            return true;
        }

        if(label.equalsIgnoreCase("elistbans")) {
            if(!perm) {
                return true;
            }

            sender.sendMessage(m._("Banned players: "));
            this.sendListToSender(sender, database.getBannedNicks());
            return true;
        }

        if(label.equalsIgnoreCase("elisttmpbans")) {
            if(!perm) {
                return true;
            }
            sender.sendMessage(m._("Temporary bans: "));
            for(String key : database.getTempBans().keySet()) {
                sender.sendMessage(key + " : "
                                   + DateFormat.getDateTimeInstance().format(new Date(database.
                        getTempBans().get(key))));
            }
            return true;
        }

        if(label.equalsIgnoreCase("ebansubnet")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            Subnet subnet = null;
            try {
                subnet = new Subnet(args[0]);
            } catch(IllegalArgumentException ex) {
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
                        toString() + m._(" has been banned"));
                ConsoleLogger.info(subnet.toString() + " has been banned by "
                                   + admin);
            } else {
                sender.sendMessage(m._("Invalid Subnet"));
            }

            return true;
        }

        if(label.equalsIgnoreCase("eunbansubnet")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            Subnet subnet = null;
            try {
                subnet = new Subnet(args[0]);
            } catch(IllegalArgumentException ex) {
            }

            if(subnet != null) {
                database.unbanSubnet(subnet);
                this.getServer().broadcastMessage(ChatColor.RED + args[0] + m._(
                        " has been unbanned"));
                ConsoleLogger.info(args[0] + " has been unbanned by " + admin);
                return true;
            } else {
                sender.sendMessage(m._("Invalid Subnet"));
            }
        }

        if(label.equalsIgnoreCase("elistsubnets")) {
            if(!perm) {
                return true;
            }

            sender.sendMessage(m._("Banned subnets: "));
            this.sendListToSender(sender, database.getBannedSubnets());
            return true;
        }

        if(label.equalsIgnoreCase("ebancountry")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.banCountry(args[0]);
            this.getServer().broadcastMessage(m._("A country has been banned: ")
                                              + args[0]);
            ConsoleLogger.info(admin + " banned the country " + args[0]);
            return true;
        }

        if(label.equalsIgnoreCase("eunbancountry")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.unbanCountry(args[0]);
            this.getServer().broadcastMessage(m._(
                    "A country has been unbanned: ") + args[0]);
            ConsoleLogger.info(admin + " unbanned the country " + args[0]);
            return true;
        }

        if(label.equalsIgnoreCase("elistcountries")) {
            if(!perm) {
                return true;
            }
            sender.sendMessage(m._("Banned countries: "));
            this.sendListToSender(sender, database.getBannedCountries());
            return true;
        }

        if(label.equalsIgnoreCase("ewhitelist")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.whitelist(args[0]);
            this.getServer().broadcastMessage(args[0] + m._(
                    " has been whitelisted"));
            ConsoleLogger.info(admin + " whitelisted " + args[0]);
            return true;
        }

        if(label.equalsIgnoreCase("eunwhitelist")) {
            if(args.length == 0 || !perm) {
                return true;
            }
            database.unWhitelist(args[0]);
            this.getServer().broadcastMessage(args[0] + m._(
                    " has been removed from the whitelist"));
            ConsoleLogger.info(admin + " unwhitelisted " + args[0]);
            return true;
        }

        if(label.equalsIgnoreCase("elistwhite")) {
            if(!perm) {
                return true;
            }
            sender.sendMessage(m._("Whitelist: "));
            this.sendListToSender(sender, database.getWhitelistedNicks());
            return true;
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

    private GeoIPLookup getGeoIPLookup() {
        Plugin pl = this.getServer().getPluginManager().getPlugin("GeoIPTools");
        if(pl != null) {
            return ((GeoIPTools) pl).getGeoIPLookup(GeoIPLookup.COUNTRYDATABASE
                                                    | GeoIPLookup.IPV6DATABASE);
        } else {
            return null;
        }
    }
}
