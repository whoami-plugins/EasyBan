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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource.DataSourceType;

public final class Settings {

    public static final String PLUGIN_FOLDER = "./plugins/EasyBan";
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages.yml";
    public static final String SETTINGS_FILE = Settings.PLUGIN_FOLDER + "/config.yml";
    public static final String DATABASE_FILE = Settings.PLUGIN_FOLDER + "/bans.yml";
    private static Settings singleton;

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    private Settings() {
        customConfigFile = new File(Settings.SETTINGS_FILE);
        reload();
    }

    public void reload() {
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        write();
        try {
            customConfig.save(customConfigFile);
        } catch (IOException ex) {
            uk.org.whoami.geoip.util.ConsoleLogger.info("Error:" + ex.getMessage());
        }
    }

    private void write() {
        getDatabase();
        getMySQLDatabaseName();
        getMySQLHost();
        getMySQLPort();
        getMySQLUsername();
        getMySQLPassword();
        isKickPublic();
        isKickReasonPublic();
        isBanPublic();
        isBanReasonPublic();
        isBanUntilPublic();
        isSubnetBanPublic();
        isSubnetBanReasonPublic();
        isCountryBanPublic();
        getBlockLists();
        isAppendCustomKickMessageEnabled();
        isAppendCustomBanMessageEnabled();
    }

    public DataSourceType getDatabase() {
        String key = "database";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, "yaml");
        }

        try {
            return DataSourceType.valueOf(customConfig.getString(key).toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.info("Unknown database type; default to YAML");
            return DataSourceType.YAML;
        }
    }

    public String getMySQLDatabaseName() {
        String key = "MySQLDatabaseName";
        if (customConfig.getString("schema") != null) {
            String s = customConfig.getString("schema");
            customConfig.set("schema", null);
            customConfig.set(key, s);
        }
        if (customConfig.getString(key) == null) {
            customConfig.set(key, "easyban");
        }
        return customConfig.getString(key);
    }

    public String getMySQLHost() {
        String key = "MySQLHost";
        if (customConfig.getString("host") != null) {
            String s = customConfig.getString("host");
            customConfig.set("host", null);
            customConfig.set(key, s);
        }
        if (customConfig.getString(key) == null) {
            customConfig.set(key, "127.0.0.1");
        }
        return customConfig.getString(key);
    }

    public String getMySQLPort() {
        String key = "MySQLPort";
        if (customConfig.getString("port") != null) {
            String s = customConfig.getString("port");
            customConfig.set("port", null);
            customConfig.set(key, s);
        }
        if (customConfig.getString(key) == null) {
            customConfig.set(key, "3306");
        }
        return customConfig.getString(key);
    }

    public String getMySQLUsername() {
        String key = "MySQLUsername";
        if (customConfig.getString("username") != null) {
            String s = customConfig.getString("username");
            customConfig.set("username", null);
            customConfig.set(key, s);
        }
        if (customConfig.getString(key) == null) {
            customConfig.set(key, "easyban");
        }
        return customConfig.getString(key);
    }

    public String getMySQLPassword() {
        String key = "MySQLPassword";
        if (customConfig.getString("password") != null) {
            String s = customConfig.getString("password");
            customConfig.set("password", null);
            customConfig.set(key, s);
        }
        if (customConfig.getString(key) == null) {
            customConfig.set(key, "CHANGEME");
        }
        return customConfig.getString(key);
    }

    public boolean isKickPublic() {
        String key = "settings.message.kick.public";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isKickReasonPublic() {
        String key = "settings.message.kick.publicReason";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isAppendCustomKickMessageEnabled() {
        String key = "settings.message.kick.appendCustomMessage";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, false);
        }
        return customConfig.getBoolean(key, false);
    }

    public boolean isBanPublic() {
        String key = "settings.message.ban.public";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isBanReasonPublic() {
        String key = "settings.message.ban.publicReason";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isBanUntilPublic() {
        String key = "settings.message.ban.publicUntil";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isAppendCustomBanMessageEnabled() {
        String key = "settings.message.ban.appendCustomMessage";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, false);
        }
        return customConfig.getBoolean(key, false);
    }

    public boolean isSubnetBanPublic() {
        String key = "settings.message.subnetBan.public";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isSubnetBanReasonPublic() {
        String key = "settings.message.subnetBan.publicReason";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isCountryBanPublic() {
        String key = "settings.message.countryBan.public";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public boolean isWhitelistPublic() {
        String key = "settings.message.whitelist.public";
        if (customConfig.getString(key) == null) {
            customConfig.set(key, true);
        }
        return customConfig.getBoolean(key, true);
    }

    public List<String> getBlockLists() {
        String key = "settings.dnsbl";

        List<String> def = new ArrayList<String>();
        def.add("dnsbl.proxybl.org");
        def.add("tor.dnsbl.sectoor.de");

        if(customConfig.getList(key) == null) {
            customConfig.set(key,def);
        }
        return customConfig.getStringList(key);
    }

    public static Settings getInstance() {
        if (singleton == null) {
            singleton = new Settings();
        }
        return singleton;
    }
}
