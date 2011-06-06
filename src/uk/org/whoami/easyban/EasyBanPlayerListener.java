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

import uk.org.whoami.easyban.datasource.Datasource;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class EasyBanPlayerListener extends PlayerListener {

    private Datasource database;
    private Messages msg;

    public EasyBanPlayerListener(Datasource database,Messages msg) {
        this.database = database;
        this.msg = msg;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        database.addIpToHistory(name, ip);

        if(database.isNickBanned(name) || database.isIpBanned(ip)) {
            player.kickPlayer("Kebab has been removed from premises");
        }
    }
}
