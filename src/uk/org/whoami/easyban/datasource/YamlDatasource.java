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
package uk.org.whoami.easyban.datasource;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import uk.org.whoami.easyban.util.Subnet;

public class YamlDatasource implements Datasource {

    private Configuration banFile;
    private final String banPath = "bans";
    private final String historyPath = "history";
    private final String subnetPath = "subnets";

    public YamlDatasource(JavaPlugin plugin) {
        banFile = new Configuration(new File(plugin.getDataFolder(),
                                             "bans.yml"));
        banFile.load();
        if(banFile.getProperty(banPath) == null) {
            banFile.setProperty(banPath,
                                new HashMap<String, HashMap<String, String>>());
        }
        if(banFile.getProperty(historyPath) == null) {
            banFile.setProperty(historyPath, new HashMap<String, List<String>>());
        }
        if(banFile.getProperty(subnetPath) == null) {
            banFile.setProperty(subnetPath,
                                new HashMap<String, HashMap<String, String>>());
        }
        banFile.save();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void addIpToHistory(String nick, String ip) {
        HashMap<String, List<String>> history =
                (HashMap<String, List<String>>) banFile.getProperty(historyPath);

        if(history.containsKey(nick)) {
            if(!history.get(nick).contains(ip)) {
                history.get(nick).add(ip);
            }
        } else {
            history.put(nick, new ArrayList<String>());
            history.get(nick).add(ip);
        }
        banFile.save();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void banNick(String nick, String admin) {
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);

        if(!bans.containsKey(nick)) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put("admin", admin);
            bans.put(nick, tmp);
            banFile.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void banNick(String nick, String admin, String reason) {
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);

        if(!bans.containsKey(nick)) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put("admin", admin);
            tmp.put("reason", reason);
            bans.put(nick, tmp);
            banFile.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void unbanNick(String nick) {
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);

        if(bans.containsKey(nick)) {
            bans.remove(nick);
            banFile.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void banSubnet(Subnet subnet, String admin) {
        HashMap<String, HashMap<String, String>> subnets =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                subnetPath);

        if(!subnets.containsKey(subnet.toString())) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put("admin", admin);
            subnets.put(subnet.toString(), tmp);
            banFile.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void banSubnet(Subnet subnet, String admin,
                                       String reason) {
        HashMap<String, HashMap<String, String>> subnets =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                subnetPath);

        if(!subnets.containsKey(subnet.toString())) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put("admin", admin);
            tmp.put("reason", reason);
            subnets.put(subnet.toString(), tmp);
            banFile.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void unbanSubnet(Subnet subnet) {
        HashMap<String, HashMap<String, String>> subnets =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                subnetPath);

        if(subnets.containsKey(subnet.toString())) {
            subnets.remove(subnet.toString());
            banFile.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean isIpBanned(String ip) {
        HashMap<String, List<String>> history =
                (HashMap<String, List<String>>) banFile.getProperty(historyPath);
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);
        HashMap<String, HashMap<String, String>> subnets =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                subnetPath);

        Iterator<String> itl = subnets.keySet().iterator();
        while(itl.hasNext()) {
            try {
                String[] sub = itl.next().split("/");

                Subnet subnet = new Subnet(InetAddress.getByName(sub[0]),
                                           InetAddress.getByName(sub[1]));
                if(subnet.isIpInSubnet(InetAddress.getByName(ip))) {
                    return true;
                }
            } catch(UnknownHostException ex) {
            }
        }

        Iterator<String> it = bans.keySet().iterator();

        while(it.hasNext()) {
            String bannedNick = it.next();
            if(history.containsKey(bannedNick) && history.get(bannedNick).
                    contains(ip)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean isNickBanned(String nick) {
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);
        if(bans.containsKey(nick)) {
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getBannedNicks() {
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);
        return bans.keySet().toArray(new String[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getBannedSubnets() {
        HashMap<String, HashMap<String, String>> subnets =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                subnetPath);
        return subnets.keySet().toArray(new String[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getPlayerIps(String nick) {
        HashMap<String, List<String>> history =
                (HashMap<String, List<String>>) banFile.getProperty(historyPath);

        if(history.containsKey(nick)) {
            List<String> list = history.get(nick);
            return list.toArray(new String[0]);
        } else {
            return new String[0];
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getBanInformation(String nick) {
        HashMap<String, HashMap<String, String>> bans =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                banPath);

        if(bans.containsKey(nick)) {
            HashMap<String, String> ban = bans.get(nick);
            if(ban.containsKey("admin") && ban.containsKey("reason")) {
                String[] ret = new String[3];
                ret[0] = nick;
                ret[1] = ban.get("admin");
                ret[2] = ban.get("reason");
                return ret;
            } else if(ban.containsKey("admin")) {
                String[] ret = new String[2];
                ret[0] = nick;
                ret[1] = ban.get("admin");

                return ret;
            } else {
                return new String[]{nick};
            }
        } else {
            return new String[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getBanInformation(Subnet subnet) {
        HashMap<String, HashMap<String, String>> subnets =
                (HashMap<String, HashMap<String, String>>) banFile.getProperty(
                subnetPath);

        if(subnets.containsKey(subnet.toString())) {
            HashMap<String, String> ban = subnets.get(subnet.toString());
            if(ban.containsKey("admin")) {
                String[] ret = new String[2];
                ret[0] = subnet.toString();
                ret[1] = ban.get("admin");
                return ret;
            } else if(ban.containsKey("admin") && ban.containsKey("reason")) {
                String[] ret = new String[3];
                ret[0] = subnet.toString();
                ret[1] = ban.get("admin");
                ret[2] = ban.get("reason");
                return ret;
            } else {
                return new String[]{subnet.toString()};
            }
        } else {
            return new String[0];
        }
    }

    @Override
    public void close() {
    }
}