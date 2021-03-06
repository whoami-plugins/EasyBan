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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.util.Subnet;
import uk.org.whoami.geoip.util.ConsoleLogger;

public class YamlDataSource implements DataSource {

    private final String banPath = "bans";
    private final String historyPath = "history";
    private final String subnetPath = "subnets";
    private final String countryPath = "countries";
    private final String whitelistPath = "whitelist";
    private HashMap<String, List<String>> history;
    private HashMap<String, HashMap<String, String>> bans;
    private HashMap<String, HashMap<String, String>> subnets;
    private ArrayList<String> countries;
    private ArrayList<String> whitelist;

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    @SuppressWarnings("unchecked")
    public YamlDataSource() {
        customConfigFile = new File(Settings.DATABASE_FILE);
        reload();
    }

    @Override
    public synchronized void addIpToHistory(String nick, String ip) {
        if(history.containsKey(nick)) {
            if(!history.get(nick).contains(ip)) {
                history.get(nick).add(ip);
            }
        } else {
            history.put(nick, new ArrayList<String>());
            history.get(nick).add(ip);
        }
        save();
    }

    @Override
    public synchronized void banNick(String nick, String admin, String reason,
            Calendar until) {
        if(!bans.containsKey(nick)) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put("admin", admin);
            if(reason != null) {
                tmp.put("reason", reason);
            }
            if(until != null) {
                tmp.put("until", String.valueOf(until.getTimeInMillis()));
            }
            bans.put(nick, tmp);
            save();
        }
    }

    @Override
    public synchronized void unbanNick(String nick) {
        if(bans.containsKey(nick)) {
            bans.remove(nick);
            save();
        }
    }

    @Override
    public synchronized void banSubnet(Subnet subnet, String admin,
            String reason) {
        if(!subnets.containsKey(subnet.toString())) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put("admin", admin);
            tmp.put("reason", reason);
            subnets.put(subnet.toString(), tmp);
            save();
        }
    }

    @Override
    public synchronized void unbanSubnet(Subnet subnet) {
        if(subnets.containsKey(subnet.toString())) {
            subnets.remove(subnet.toString());
            save();
        }
    }

    @Override
    public synchronized void banCountry(String code) {
        if(!countries.contains(code)) {
            countries.add(code);
            save();
        }
    }

    @Override
    public synchronized void whitelist(String player) {
        if(!whitelist.contains(player)) {
            whitelist.add(player);
            save();
        }
    }

    @Override
    public synchronized void unWhitelist(String player) {
        whitelist.remove(player);
    }

    @Override
    public synchronized void unbanCountry(String code) {
        countries.remove(code);
    }

    @Override
    public synchronized boolean isIpBanned(String ip) {
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
    public boolean isSubnetBanned(String ip) {
        Iterator<String> itl = subnets.keySet().iterator();
        while(itl.hasNext()) {
            try {
                Subnet subnet = new Subnet(itl.next());
                if(subnet.isIpInSubnet(InetAddress.getByName(ip))) {
                    return true;
                }
            } catch(UnknownHostException ex) {
            }
        }
        return false;
    }

    @Override
    public synchronized boolean isNickBanned(String nick) {
        return bans.containsKey(nick);
    }

    @Override
    public synchronized boolean isCountryBanned(String code) {
        return countries.contains(code);
    }

    @Override
    public synchronized boolean isNickWhitelisted(String player) {
        return whitelist.contains(player);
    }

    @Override
    public String[] getHistory(String nick) {
        if(history.containsKey(nick)) {
            return history.get(nick).toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public synchronized String[] getBannedNicks() {
        return bans.keySet().toArray(new String[0]);
    }

    @Override
    public synchronized String[] getBannedSubnets() {
        return subnets.keySet().toArray(new String[0]);
    }

    @Override
    public synchronized String[] getBannedCountries() {
        return countries.toArray(new String[0]);
    }

    @Override
    public synchronized String[] getWhitelistedNicks() {
        return whitelist.toArray(new String[0]);
    }

    @Override
    public String[] getNicks(String ip) {
        ArrayList<String> nicks = new ArrayList<String>();

        for(Entry<String,List<String>> entry:history.entrySet()) {
            if(entry.getValue().contains(ip)) {
                nicks.add(entry.getKey());
            }
        }
        return nicks.toArray(new String[0]);
    }

    @Override
    public synchronized HashMap<String, Long> getTempBans() {
        HashMap<String, Long> tmpBans = new HashMap<String, Long>();
        Iterator<String> it = bans.keySet().iterator();
        while(it.hasNext()) {
            String nick = it.next();
            if(bans.get(nick).containsKey("until")) {
                tmpBans.put(nick, new Long(bans.get(nick).get("until")));
            }
        }
        return tmpBans;
    }

    @Override
    public synchronized HashMap<String, String> getBanInformation(String nick) {
        if(bans.containsKey(nick)) {
            return bans.get(nick);
        } else {
            return null;
        }
    }

    @Override
    public synchronized HashMap<String, String> getBanInformation(Subnet subnet) {
        if(subnets.containsKey(subnet.toString())) {
            return subnets.get(subnet.toString());
        } else {
            return null;
        }
    }

    @Override
    public final void reload() {
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        if(customConfig.get(banPath) == null) {
            customConfig.set(banPath,
                    new HashMap<String, HashMap<String, String>>());
        }
        if(customConfig.get(historyPath) == null) {
            customConfig.set(historyPath, new HashMap<String, List<String>>());
        }
        if(customConfig.get(subnetPath) == null) {
            customConfig.set(subnetPath,
                    new HashMap<String, HashMap<String, String>>());
        }
        if(customConfig.get(countryPath) == null) {
            customConfig.set(countryPath, new ArrayList<String>());
        }
        if(customConfig.get(whitelistPath) == null) {
            customConfig.set(whitelistPath, new ArrayList<String>());
        }
        history = (HashMap<String, List<String>>) customConfig.get(historyPath);
        bans = (HashMap<String, HashMap<String, String>>) customConfig.get(banPath);
        subnets = (HashMap<String, HashMap<String, String>>) customConfig.get(
                subnetPath);
        countries = (ArrayList<String>) customConfig.get(countryPath);
        whitelist = (ArrayList<String>) customConfig.get(whitelistPath);
        save();
    }

    private void save() {
        try {
            customConfig.save(customConfigFile);
        } catch (IOException ex) {
            ConsoleLogger.info("Error:" + ex.getMessage());
        }
    }

    @Override
    public void close() {
    }
}