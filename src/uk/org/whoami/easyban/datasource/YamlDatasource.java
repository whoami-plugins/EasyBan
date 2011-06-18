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
import java.util.Map;
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
            banFile.setProperty(banPath, new ArrayList<String>());
        }
        if(banFile.getProperty(historyPath) == null) {
            banFile.setProperty(historyPath, new HashMap<String, List<String>>());
        }
        if(banFile.getProperty(subnetPath) == null) {
            banFile.setProperty(subnetPath, new ArrayList<String>());
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
    public synchronized void banNick(String nick) {
        List<String> bans = (List<String>) banFile.getProperty(banPath);

        if(!bans.contains(nick)) {
            bans.add(nick);
        }
        banFile.save();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void unbanNick(String nick) {
        List<String> bans = (List<String>) banFile.getProperty(banPath);

        if(bans.contains(nick)) {
            bans.remove(nick);
        }
        banFile.save();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void banSubnet(String subnet) {
        List<String> subnets = (List<String>) banFile.getProperty(subnetPath);
        if(!subnets.contains(subnet)) {
            subnets.add(subnet);
        }
        banFile.save();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void unbanSubnet(String subnet) {
        List<String> subnets = (List<String>) banFile.getProperty(subnetPath);
        subnets.remove(subnet);
        banFile.save();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean isIpBanned(String ip) {
        HashMap<String, List<String>> history =
                (HashMap<String, List<String>>) banFile.getProperty(historyPath);
        List<String> bans = (List<String>) banFile.getProperty(banPath);
        List<String> subnets = (List<String>) banFile.getProperty(subnetPath);

        Iterator<String> itl = subnets.iterator();
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

        Iterator<Map.Entry<String, List<String>>> it =
                history.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            if(entry.getValue().contains(ip)) {
                if(bans.contains(entry.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean isNickBanned(String nick) {
        List<String> bans = (List<String>) banFile.getProperty(banPath);
        if(bans.contains(nick)) {
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getBannedNicks() {
        List<String> bans = (List<String>) banFile.getProperty(banPath);
        return bans.toArray(new String[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getBannedSubnets() {
        List<String> subnets = (List<String>) banFile.getProperty(subnetPath);
        return subnets.toArray(new String[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getPlayerIps(String nick) {
        HashMap<String, List<String>> history = (HashMap<String, List<String>>) banFile.
                getProperty(historyPath);

        if(history.containsKey(nick)) {
            List<String> list = history.get(nick);
            return list.toArray(new String[0]);
        } else {
            return new String[0];
        }

    }

    @Override
    public void close() {
    }
}
