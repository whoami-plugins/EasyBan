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
package uk.org.whoami.easyban.settings;

import java.io.File;
import java.util.HashMap;
import org.bukkit.util.config.Configuration;

public class Message extends Configuration {

    private static Message singleton = null;
    private final HashMap<String, String> map = new HashMap<String, String>();

    private Message() {
        super(new File(Settings.MESSAGE_FILE));
        loadDefaults();
        getMessages();
    }

    private void loadDefaults() {
        map.put(" has been kicked", " &chas been kicked");
        map.put("You have been kicked", "&cYou have been kicked");
        map.put(" has been banned", " &chas been banned");
        map.put("You have been banned", "&cYou have been banned");
        map.put(" has been unbanned", " &chas been unbanned");
        map.put("Invalid Subnet", "&cInvalid Subnet");
        map.put("Banned players: ", "&cBanned players: ");
        map.put("Banned subnets: ", "&cBanned subnets: ");
        map.put("Ips from ", "&cIps from ");
        map.put(" is not banned", " &cis not banned");
        map.put(" is banned", " &cis banned");
        map.put("Reason: ", "&cReason: ");
        map.put("Until: ", "&cUntil: ");
        map.put("Admin: ", "&cAdmin: ");
        map.put("Wrong time format", "&cWrong time format");
        map.put("You are banned until: ", "&cYou are banned until: ");
        map.put("You are banned", "&cYou are banned");
        map.put("Your country has been banned", "&cYour country has been banned");
        map.put("Temporary bans: ", "&cTemporary bans: ");
        map.put("A country has been banned: ", "&cA country has been banned: ");
        map.put("A country has been unbanned: ", "&cA country has been unbanned: ");
        map.put("Banned countries: ", "&cBanned countries: ");
        map.put(" has been whitelisted", " &chas been whitelisted");
        map.put(" has been removed from the whitelist",
                " &chas been removed from the whitelist");
        map.put("Whitelist: ", "&cWhitelist: ");
        map.put("Alternative nicks of ", "&cAlternative nicks of ");
        map.put("Your subnet is banned", "&cYour subnet is banned");
        map.put("Users who connected from IP", "&cUsers who connected from IP");
        map.put("You have been banned by ","&cYou have been banned by ");
        map.put("custom_kick", "&cComplain on http://example.com");
        map.put("custom_ban", "&cComplain on http://example.com");
        map.put("DNSBL Ban", "DNSBL Ban");
        map.put("Settings and database reloaded", "Settings and database reloaded");
    }

    public String _(String message) {
        String ret = map.get(message);
        if(ret != null) {
            return ret.replace("&", "\u00a7");
        }
        return message;
    }

    private void getMessages() {
        this.load();
        for(String key : map.keySet()) {
            if(this.getString(key) == null) {
                this.setProperty(key, map.get(key));
            } else {
                map.put(key, this.getString(key));
            }
        }
        this.save();
    }

    public static Message getInstance() {
        if(singleton == null) {
            singleton = new Message();
        }
        return singleton;
    }
}
