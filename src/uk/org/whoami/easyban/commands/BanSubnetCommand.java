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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.util.Subnet;

public class BanSubnetCommand extends EasyBanCommand {

    private DataSource database;

    public BanSubnetCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }
        Subnet subnet = null;
        try {
            subnet = new Subnet(args[0]);
        } catch (IllegalArgumentException ex) {
        }

        if (subnet != null) {
            String reason = null;
            if (args.length == 1) {
                database.banSubnet(subnet, admin, null);
            } else {
                reason = "";
                for (int i = 1; i < args.length; i++) {
                    reason += args[i] + " ";
                }
                database.banSubnet(subnet, admin, reason);
            }

            Settings settings = Settings.getInstance();
            if (settings.isSubnetBanPublic()) {
                cs.getServer().broadcastMessage(subnet.toString() + m._(" has been banned"));
            }

            if (reason != null && settings.isSubnetBanReasonPublic()) {
                cs.getServer().broadcastMessage(m._("Reason: ") + reason);
            }

            ConsoleLogger.info(subnet.toString() + " has been banned by "
                    + admin);
        } else {
            cs.sendMessage(m._("Invalid Subnet"));
        }
    }
}
