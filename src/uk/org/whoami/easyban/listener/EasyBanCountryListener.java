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
import java.net.UnknownHostException;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.settings.Message;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.geoip.GeoIPLookup;

public class EasyBanCountryListener extends PlayerListener {

    private DataSource database;
    private GeoIPLookup geo;
    private final Message m = Message.getInstance();

    public EasyBanCountryListener(DataSource data, GeoIPLookup geo) {
        this.database = data;
        this.geo = geo;
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent evt) {
        if (evt.getPlayer() == null || !evt.getResult().equals(Result.ALLOWED)) {
            return;
        }

        String nick = evt.getPlayer().getName();
        String ip = evt.getKickMessage();

        if (database.isNickWhitelisted(nick)) {
            return;
        }

        try {
            InetAddress inet = InetAddress.getByName(ip);
            String code = geo.getCountry(inet).getCode();

            if (database.isCountryBanned(code)) {
                ConsoleLogger.info("Player " + nick + "is from banned country " + code);
                evt.disallow(Result.KICK_BANNED, m._("Your country has been banned"));
            }
        } catch (UnknownHostException ex) {
            ConsoleLogger.info(ex.getMessage());
        }
    }
}
