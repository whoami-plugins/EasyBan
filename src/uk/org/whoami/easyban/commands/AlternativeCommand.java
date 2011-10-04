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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import uk.org.whoami.easyban.datasource.DataSource;

public class AlternativeCommand extends EasyBanCommand {

    private DataSource database;

    public AlternativeCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
                return;
            }
            try {
                InetAddress.getByName(args[0]);
                cs.sendMessage(m._("Users who connected from IP") + args[0]);
                this.sendListToSender(cs, database.getNicks(args[0]));
            } catch (UnknownHostException ex) {
                ArrayList<String> nicks = new ArrayList<String>();

                for (String ip : database.getHistory(args[0])) {
                    Collections.addAll(nicks, database.getNicks(ip));
                }
                cs.sendMessage(m._("Alternative nicks of ") + args[0]);
                this.sendListToSender(cs, nicks.toArray(new String[0]));
            }
    }

}
