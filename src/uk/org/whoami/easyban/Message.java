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

import org.bukkit.util.config.Configuration;

public class Message {

    public Message() {
    }

    public static String getMessage(String string, Configuration msgFile) {
        return msgFile.getString(string);
    }

    public static synchronized void loadDefaults(Configuration msgFile) {
        if(null == msgFile.getString("EasyBan enabled")) {
            msgFile.setProperty("EasyBan enabled", "EasyBan enabled");
        }
        if(null == msgFile.getString("has been kicked")) {
            msgFile.setProperty("has been kicked", " has been kicked");
        }
        if(null == msgFile.getString("You have been kicked")) {
            msgFile.setProperty("You have been kicked", "You have been kicked");
        }
        if(null == msgFile.getString("has been banned")) {
            msgFile.setProperty("has been banned", " has been banned");
        }
        if(null == msgFile.getString("You have been banned")) {
            msgFile.setProperty("You have been banned", "You have been banned");
        }
        if(null == msgFile.getString("has been unbanned")) {
            msgFile.setProperty("has been unbanned", " has been unbanned");
        }
        if(null == msgFile.getString("Invalid Subnet")) {
            msgFile.setProperty("Invalid Subnet", "Invalid Subnet");
        }
        if(null == msgFile.getString("Banned players")) {
            msgFile.setProperty("Banned players", "Banned players");
        }
        if(null == msgFile.getString("Banned subnets")) {
            msgFile.setProperty("Banned subnets", "Banned subnets");
        }
        if(null == msgFile.getString("Ips from")) {
            msgFile.setProperty("Ips from", "Ips from");
        }

        msgFile.save();
    }
}
