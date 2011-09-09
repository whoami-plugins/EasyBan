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
package uk.org.whoami.easyban.commands;

import java.text.DateFormat;
import java.util.Calendar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.util.Subnet;

public class BanCommand extends EasyBanCommand {

    private DataSource database;

    public BanCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        String playerNick = args[0];
        Player player = cs.getServer().getPlayer(playerNick);
        String reason = null;
        Calendar until = null;

        if (player != null) {
            playerNick = player.getName();
        }

        if (args.length > 1) {
            int to = args.length - 1;
            if (Subnet.isParseableInteger(args[args.length - 1])) {
                until = Calendar.getInstance();
                int min = Integer.parseInt(args[args.length - 1]);
                until.add(Calendar.MINUTE, min);
            } else {
                to = args.length;
            }

            String tmp = "";
            for (int i = 1; i < to; i++) {
                tmp += args[i] + " ";
            }
            if (tmp.length() > 0) {
                reason = tmp;
            }
        }
        Settings settings = Settings.getInstance();
        if (player != null) {
            String kickmsg = m._("You have been banned by ") + admin;
            if (reason != null) {
                kickmsg += " " + m._("Reason: ") + reason;
            }
            if (until != null) {
                kickmsg += " " + m._("Until: ") + DateFormat.getDateTimeInstance().format(until.getTime());
            }
            if(settings.isAppendCustomBanMessageEnabled()) {
                kickmsg += " " + m._("custom_ban");
            }
            
            player.kickPlayer(kickmsg);
        }
        database.banNick(playerNick, admin, reason, until);
        

        if (settings.isBanPublic()) {
            cs.getServer().broadcastMessage(playerNick + m._(" has been banned"));
            if (settings.isBanReasonPublic() && reason != null) {
                cs.getServer().broadcastMessage(m._("Reason: ") + reason);
            }
            if (settings.isBanUntilPublic() && until != null) {
                cs.getServer().broadcastMessage(m._("Until: ") + DateFormat.getDateTimeInstance().format(until.getTime()));
            }
        }
        ConsoleLogger.info(playerNick + " has been banned by " + admin);
    }
}
