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
package uk.org.whoami.easyban.listener;

import java.net.InetAddress;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.Message;
import uk.org.whoami.easyban.datasource.Datasource;
import uk.org.whoami.geoip.GeoIPLookup;

public class EasyBanCountryListener extends PlayerListener {

    private Datasource database;
    private GeoIPLookup geo;
    private final Message m = Message.getInstance();

    public EasyBanCountryListener(Datasource data, GeoIPLookup geo) {
        this.database = data;
        this.geo = geo;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(event.getPlayer() == null) {
            return;
        }

        if(database.isNickWhitelisted(event.getPlayer().getName())) {
            return;
        }

        InetAddress ip = event.getPlayer().getAddress().getAddress();
        String code = geo.getCountry(ip).getCode();

        if(database.isCountryBanned(code)) {
            ConsoleLogger.info("Player " + event.getPlayer().getName()
                               + "is from banned country " + code);
            event.getPlayer().kickPlayer(m._("Your country has been banned"));
        }
    }
}
