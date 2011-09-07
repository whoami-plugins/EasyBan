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
package uk.org.whoami.easyban.datasource;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.settings.Settings;

/**
 *
 * @author Sebastian Köhler <sebkoehler@whoami.org.uk>
 */
public class MySQLDataSource extends SQLDataSource {

    private String databaseName;
    private String host;
    private String port;
    private String username;
    private String password;

    public MySQLDataSource(Settings settings) throws ClassNotFoundException, SQLException {
        this.databaseName = settings.getMySQLDatabaseName();
        this.host = settings.getMySQLHost();
        this.port = settings.getMySQLPort();
        this.username = settings.getMySQLUsername();
        this.password = settings.getMySQLPassword();
        connect();
        setup();
        ConsoleLogger.info("Database setup finished");
    }

    @Override
    final protected synchronized void connect() throws ClassNotFoundException,
            SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        ConsoleLogger.info("MySQL driver loaded");
        con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port
                + "/" + databaseName, username, password);
        ConsoleLogger.info("Connected to Database");
    }

    @Override
    final protected synchronized void setup() throws SQLException {
        Statement st = null;
        try {
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player ("
                    + "player_id INTEGER AUTO_INCREMENT,"
                    + "player VARCHAR(20) NOT NULL,"
                    + "CONSTRAINT player_const_prim PRIMARY KEY (player_id),"
                    + "CONSTRAINT player_const_uniq UNIQUE(player)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS ip ("
                    + "ip_id INTEGER AUTO_INCREMENT,"
                    + "player_id INTEGER NOT NULL,"
                    + "ip VARCHAR(40) NOT NULL,"
                    + "CONSTRAINT ip_const_prim PRIMARY KEY (ip_id),"
                    + "CONSTRAINT ip_const_ref FOREIGN KEY (player_id) REFERENCES player (player_id)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player_ban ("
                    + "player_ban_id INTEGER AUTO_INCREMENT,"
                    + "player_id INTEGER NOT NULL,"
                    + "admin VARCHAR(20) NOT NULL,"
                    + "reason VARCHAR(100),"
                    + "until TIMESTAMP,"
                    + "CONSTRAINT player_ban_const_prim PRIMARY KEY (player_ban_id),"
                    + "CONSTRAINT player_ban_const_uniq UNIQUE (player_id),"
                    + "CONSTRAINT player_ban_const_ref FOREIGN KEY (player_id) REFERENCES player (player_id)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS subnet_ban ("
                    + "subnet_ban_id INTEGER AUTO_INCREMENT,"
                    + "subnet VARCHAR(100) NOT NULL,"
                    + "admin VARCHAR(20) NOT NULL,"
                    + "reason VARCHAR(100),"
                    + "CONSTRAINT subnet_ban_const_prim PRIMARY KEY (subnet_ban_id),"
                    + "CONSTRAINT subnet_ban_const_uniq UNIQUE (subnet)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS country_ban ("
                    + "country_ban_id INTEGER AUTO_INCREMENT,"
                    + "country VARCHAR(2) NOT NULL,"
                    + "CONSTRAINT country_ban_const_prim PRIMARY KEY (country_ban_id),"
                    + "CONSTRAINT country_ban_const_uniq UNIQUE (country)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS whitelist ("
                    + "whitelist_id INTEGER AUTO_INCREMENT,"
                    + "player_id INTEGER NOT NULL,"
                    + "CONSTRAINT whitelist_const_prim PRIMARY KEY (whitelist_id),"
                    + "CONSTRAINT whitelist_const_ref FOREIGN KEY (player_id) REFERENCES player (player_id)"
                    + ");");
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void close() {
        try {
            con.close();
        } catch (SQLException ex) {
        }
    }
}
