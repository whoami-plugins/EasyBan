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

import java.io.File;
import java.util.HashMap;
import org.bukkit.util.config.Configuration;

public class Message extends Configuration {

    private static Message singleton = null;
    private final HashMap<String, String> map = new HashMap<>();

    private Message(File folder) {
        super(new File(folder, "messages.yml"));
        loadDefaults();
        getMessages();
    }

    private void loadDefaults() {
        map.put(" has been kicked", " has been kicked");
        map.put("You have been kicked", "You have been kicked");
        map.put(" has been banned", " has been banned");
        map.put("You have been banned", "You have been banned");
        map.put(" has been unbanned", " has been unbanned");
        map.put("Invalid Subnet", "Invalid Subnet");
        map.put("Banned players: ", "Banned players: ");
        map.put("Banned subnets: ", "Banned subnets: ");
        map.put("Ips from ", "Ips from ");
        map.put(" is not banned", " is not banned");
        map.put(" is banned", " is banned");
        map.put("Reason: ", "Reason: ");
        map.put("Until: ", "Until: ");
        map.put("Admin: ", "Admin: ");
        map.put("Wrong time format", "Wrong time format");
        map.put("You are banned until: ", "You are banned until: ");
        map.put("You are banned", "You are banned");
        map.put("Your country has been banned", "Your country has been banned");
        map.put("Temporary bans: ", "Temporary bans: ");
        map.put("A country has been banned: ", "A country has been banned: ");
        map.put("A country has been unbanned: ", "A country has been unbanned: ");
        map.put("Banned countries: ", "Banned countries: ");
        map.put(" has been whitelisted", " has been whitelisted");
        map.put(" has been removed from the whitelist",
                " has been removed from the whitelist");
        map.put("Whitelist: ", "Whitelist: ");
        map.put("Alternative nicks of ", "Alternative nicks of ");
        map.put("Your subnet is banned", "Your subnet is banned");
        map.put("Users who connected from IP", "Users who connected from IP");
    }

    public String _(String message) {
        String ret = map.get(message);
        if(ret == null) {
            return message;
        }
        return ret;
    }

    public void updateMessages(Configuration conf) {
        for(String key : conf.getKeys()) {
            if(key.equals("database") || key.equals("maxmind") || key.equals(
                    "maxmindv6") || key.equals("host") || key.equals("port")
               || key.equals("username") || key.equals("password") || key.equals("schema")) {
                continue;
            }

            if(map.containsKey(key)) {
                map.put(key, conf.getString(key));
                this.setProperty(key, map.get(key));
            }
            conf.removeProperty(key);
            conf.save();
        }
        this.save();
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

    public static Message getInstance(File folder) {
        if(singleton == null) {
            singleton = new Message(folder);
        }
        return singleton;
    }

    public static Message getInstance() {
        return singleton;
    }
}
