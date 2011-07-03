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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.org.whoami.easyban.datasource.Datasource;

public class UnbanTask implements Runnable {

    private Datasource data;
    private static final Logger log = Logger.getLogger("Minecraft");

    public UnbanTask(Datasource data) {
        this.data = data;
    }

    @Override
    public void run() {
        Calendar cal = Calendar.getInstance();
        HashMap<String,Long> tmpBans = data.getTempBans();
        Iterator<String> it = tmpBans.keySet().iterator();
        while(it.hasNext()) {
            String name = it.next();
            if(cal.getTimeInMillis() > tmpBans.get(name)) {
                data.unbanNick(name);
                log.info("[EasyBan] Temporary for "+ name +" ban has been removed");
            }
        }
    }

}
