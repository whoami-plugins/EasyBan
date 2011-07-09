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

import com.maxmind.geoip.LookupService;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.Message;
import uk.org.whoami.easyban.datasource.Datasource;

public class EasyBanCountryListener extends PlayerListener {

    private LookupService geo = null;
    private LookupService geov6 = null;
    private Datasource database;
    private final Message m = Message.getInstance();

    public EasyBanCountryListener(Datasource data, String geo, String geov6)
            throws IOException {
        this.database = data;
        if(geo != null) {
            this.geo = new LookupService(geo, LookupService.GEOIP_MEMORY_CACHE);
        }
        if(geov6 != null) {
            this.geov6 = new LookupService(geov6,
                    LookupService.GEOIP_MEMORY_CACHE);
        }
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
        String code;
        if(ip instanceof Inet6Address && geov6 != null) {
            code = geov6.getCountryV6(ip).getCode();
        } else if(geo != null) {
            code = geo.getCountry(ip).getCode();
        } else {
            return;
        }

        if(database.isCountryBanned(code)) {
            ConsoleLogger.info("Player " + event.getPlayer().getName()
                               + "is from banned country " + code);
            event.getPlayer().kickPlayer(m._("Your country has been banned"));
        }
    }
}
