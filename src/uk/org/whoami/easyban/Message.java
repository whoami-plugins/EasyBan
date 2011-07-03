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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.util.config.Configuration;

public class Message {

    private static Message singleton = null;
    private Configuration conf;
    private static final Logger log = Logger.getLogger("Minecraft");

    private Message(Configuration conf) {
        this.conf = conf;
        loadDefaults();
    }

    private void loadDefaults() {
        if(conf.getString(" has been kicked") == null) {
            conf.setProperty(" has been kicked", " has been kicked");
        }
        if(conf.getString("You have been kicked") == null) {
            conf.setProperty("You have been kicked", "You have been kicked");
        }
        if(conf.getString(" has been banned") == null) {
            conf.setProperty(" has been banned", " has been banned");
        }
        if(conf.getString("You have been banned") == null) {
            conf.setProperty("You have been banned", "You have been banned");
        }
        if(conf.getString(" has been unbanned") == null) {
            conf.setProperty(" has been unbanned", " has been unbanned");
        }
        if(conf.getString("Invalid Subnet") == null) {
            conf.setProperty("Invalid Subnet", "Invalid Subnet");
        }
        if(conf.getString("Banned players: ") == null) {
            conf.setProperty("Banned players: ", "Banned players: ");
        }
        if(conf.getString("Banned subnets: ") == null) {
            conf.setProperty("Banned subnets: ", "Banned subnets: ");
        }
        if(conf.getString("Ips from ") == null) {
            conf.setProperty("Ips from ", "Ips from ");
        }
        if(conf.getString(" is not banned") == null) {
            conf.setProperty(" is not banned", " is not banned");
        }
        if(conf.getString(" is banned") == null) {
            conf.setProperty(" is banned", " is banned");
        }
        if(conf.getString("Reason: ") == null) {
            conf.setProperty("Reason: ", "Reason: ");
        }
        if(conf.getString("Until: ") == null) {
            conf.setProperty("Until: ", "Until: ");
        }
        if(conf.getString("Admin: ") == null) {
            conf.setProperty("Admin: ", "Admin: ");
        }
        if(conf.getString("Wrong time format") == null) {
            conf.setProperty("Wrong time format", "Wrong time format");
        }
        if(conf.getString("You are banned until: ") == null) {
            conf.setProperty("You are banned until: ", "You are banned until: ");
        }
        if(conf.getString("You are banned") == null) {
            conf.setProperty("You are banned", "You are banned");
        }
        conf.save();
    }

    public String _(String message) {
        return conf.getString(message, message);
    }

    public static Message getInstance(Configuration conf) {
        if(singleton == null) {
            singleton = new Message(conf);
        }
        return singleton;
    }

    public static Message getInstance() {
        return singleton;
    }
}
