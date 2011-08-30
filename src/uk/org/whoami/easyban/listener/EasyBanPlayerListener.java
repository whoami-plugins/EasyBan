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

import uk.org.whoami.easyban.datasource.DataSource;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.Message;

public class EasyBanPlayerListener extends PlayerListener {

    private DataSource database;
    private Message msg;

    public EasyBanPlayerListener(DataSource database) {
        this.database = database;
        this.msg = Message.getInstance();
    }
    
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if(event.getPlayer() == null || !event.getResult().equals(Result.ALLOWED)) {
            return;
        }
        
        String name = event.getPlayer().getName();
        String ip = event.getKickMessage();

        database.addIpToHistory(name, ip);

        if (database.isNickBanned(name) || database.isIpBanned(ip)) {
            event.disallow(Result.KICK_BANNED, msg._("You are banned"));
            ConsoleLogger.info("Ban for " + name + " detected");
        }

        if(database.isNickWhitelisted(event.getPlayer().getName())) {
            return;
        }

        if(database.isSubnetBanned(ip)) {
            event.disallow(Result.KICK_BANNED, msg._("Your subnet is banned"));
            ConsoleLogger.info("Subnet ban for " + name + "/"+ ip +" detected");
        }
    }
}
