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
import org.bukkit.util.config.Configuration;

public final class Settings extends Configuration {
    
    public static final String PLUGIN_FOLDER = "./plugins/EasyBan";
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages.yml";
    public static final String SETTINGS_FILE = Settings.PLUGIN_FOLDER + "/config.yml";
    public static final String DATABASE_FILE = Settings.PLUGIN_FOLDER + "/bans.yml";
    
    private static Settings singleton;
    
    private Settings() {
        super(new File(Settings.SETTINGS_FILE));
        reload();
    }
    
    public void reload() {
        load();
        write();
    }
    
    private void write() {
        getDatabase();
        getMySQLDatabaseName();
        getMySQLHost();
        getMySQLPort();
        getMySQLUsername();
        getMySQLPassword();
    }

    public String getDatabase() {
        String key = "database";
        if(getString(key) == null) {
            setProperty(key, "yaml");
        }
        return getString(key);
    }
    
    public String getMySQLDatabaseName() {
        String key = "MySQLDatabaseName";
        if(getString("schema") != null) {
            String s = getString("schema");
            removeProperty("schema");
            setProperty(key, s);
        }
        if(getString(key) == null) {
            setProperty(key, "easyban");
        }
        return getString(key);
    }
    
    public String getMySQLHost() {
        String key = "MySQLHost";
        if(getString("host") != null) {
            String s = getString("host");
            removeProperty("host");
            setProperty(key, s);
        }
        if(getString(key) == null) {
            setProperty(key, "127.0.0.1");
        }
        return getString(key);
    }
    
    public String getMySQLPort() {
        String key = "MySQLPort";
        if(getString("port") != null) {
            String s = getString("port");
            removeProperty("port");
            setProperty(key, s);
        }
        if(getString(key) == null) {
            setProperty(key, "3306");
        }
        return getString(key);
    }
    
    public String getMySQLUsername() {
        String key = "MySQLUsername";
        if(getString("username") != null) {
            String s = getString("username");
            removeProperty("username");
            setProperty(key, s);
        }
        if(getString(key) == null) {
            setProperty(key, "easyban");
        }
        return getString(key);
    }
    
    public String getMySQLPassword() {
        String key = "MySQLPassword";
        if(getString("password") != null) {
            String s = getString("password");
            removeProperty("password");
            setProperty(key, s);
        }
        if(getString(key) == null) {
            setProperty(key, "CHANGEME");
        }
        return getString(key);
    }
    
    public static Settings getInstance() {
        if(singleton != null) {
            singleton = new Settings();
        }
        return singleton;
    }
}
