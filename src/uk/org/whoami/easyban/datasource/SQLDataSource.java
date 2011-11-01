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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.hsqldb.types.Types;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.util.Subnet;

public abstract class SQLDataSource implements DataSource {

    protected Connection con;

    protected abstract void connect() throws ClassNotFoundException,
            SQLException;

    protected abstract void setup() throws SQLException;

    @Override
    public abstract void close();

    private synchronized void createNick(String nick) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT player FROM player WHERE player=?");
            pst.setString(1, nick);
            if (!pst.executeQuery().next()) {
                pst.close();
                pst = con.prepareStatement(
                        "INSERT INTO player (player) VALUES(?);");
                pst.setString(1, nick);
                pst.executeUpdate();
            }
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void addIpToHistory(String nick, String ip) {
        PreparedStatement pst = null;
        try {
            createNick(nick);
            pst = con.prepareStatement(
                    "SELECT ip FROM ip "
                    + "WHERE player_id=(SELECT player_id FROM player WHERE player= ?) AND ip=?;");
            pst.setString(1, nick);
            pst.setString(2, ip);
            if (!pst.executeQuery().next()) {
                pst.close();
                pst = con.prepareStatement("INSERT INTO ip (player_id,ip) VALUES("
                        + "(SELECT player_id FROM player WHERE player= ? ),"
                        + "?"
                        + ");");
                pst.setString(1, nick);
                pst.setString(2, ip);
                pst.executeUpdate();
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void banNick(String nick, String admin, String reason,
            Calendar until) {
        PreparedStatement pst = null;
        try {
            createNick(nick);
            pst = con.prepareStatement("INSERT INTO player_ban (player_id,admin,reason,until) VALUES("
                    + "(SELECT player_id FROM player WHERE player= ? ),"
                    + "?,"
                    + "?,"
                    + "?"
                    + ");");
            pst.setString(1, nick);
            pst.setString(2, admin);
            if (reason != null) {
                pst.setString(3, reason);
            } else {
                pst.setNull(3, Types.VARCHAR);
            }
            if (until != null) {
                pst.setTimestamp(4, new Timestamp(until.getTimeInMillis()));
            } else {
                pst.setTimestamp(4, new Timestamp(100000));
            }
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void unbanNick(String nick) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "DELETE FROM player_ban WHERE "
                    + "player_id=(SELECT player_id FROM player WHERE player=?);");
            pst.setString(1, nick);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void banSubnet(Subnet subnet, String admin,
            String reason) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("INSERT INTO subnet_ban (subnet,admin,reason) VALUES("
                    + "?,"
                    + "?,"
                    + "?"
                    + ");");
            pst.setString(1, subnet.toString());
            pst.setString(2, admin);
            if (reason != null) {
                pst.setString(3, reason);
            } else {
                pst.setNull(3, Types.VARCHAR);
            }
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void unbanSubnet(Subnet subnet) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "DELETE FROM subnet_ban WHERE subnet=?;");
            pst.setString(1, subnet.toString());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void banCountry(String code) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "INSERT INTO country_ban (country) VALUES(?);");
            pst.setString(1, code);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void unbanCountry(String code) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "DELETE FROM country_ban WHERE country=?;");
            pst.setString(1, code);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void whitelist(String nick) {
        PreparedStatement pst = null;
        try {
            createNick(nick);
            pst = con.prepareStatement("INSERT INTO whitelist (player_id) VALUES("
                    + "SELECT player_id FROM player WHERE player=?"
                    + ");");
            pst.setString(1, nick);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void unWhitelist(String nick) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "DELETE FROM whitelist "
                    + "WHERE player_id=(SELECT player_id FROM player WHERE player=?);");
            pst.setString(1, nick);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized boolean isIpBanned(String ip) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT ip FROM ip "
                    + "WHERE player_id IN (SELECT player_id FROM player_ban) AND ip=?;");
            pst.setString(1, ip);
            return pst.executeQuery().next();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized boolean isSubnetBanned(String ip) {
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT subnet FROM subnet_ban;");
            while (rs.next()) {
                try {
                    Subnet sub = new Subnet(rs.getString(1));
                    if (sub.isIpInSubnet(InetAddress.getByName(ip))) {
                        return true;
                    }
                } catch (UnknownHostException ex) {
                }
            }
            return false;
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
            return false;
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
    public synchronized boolean isNickBanned(String nick) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT player_id FROM player_ban "
                    + "WHERE player_id=(SELECT player_id FROM player WHERE player=?);");
            pst.setString(1, nick);
            return pst.executeQuery().next();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized boolean isCountryBanned(String code) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT country FROM country_ban WHERE country=?;");
            pst.setString(1, code);
            return pst.executeQuery().next();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized boolean isNickWhitelisted(String nick) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT player_id FROM whitelist "
                    + "WHERE player_id=(SELECT player_id FROM player WHERE player=?);");
            pst.setString(1, nick);
            return pst.executeQuery().next();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized String[] getHistory(String nick) {
        ArrayList<String> list = new ArrayList<String>();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT ip FROM ip "
                    + "WHERE player_id=(SELECT player_id FROM player WHERE player=?);");
            pst.setString(1, nick);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public synchronized String[] getBannedNicks() {
        ArrayList<String> list = new ArrayList<String>();
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT player FROM player "
                    + "WHERE player_id IN (SELECT player_id FROM player_ban);");
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public synchronized String[] getBannedSubnets() {
        ArrayList<String> list = new ArrayList<String>();
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT subnet FROM subnet_ban;");
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public synchronized String[] getBannedCountries() {
        ArrayList<String> list = new ArrayList<String>();
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT country FROM country_ban;");
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public synchronized String[] getWhitelistedNicks() {
        ArrayList<String> list = new ArrayList<String>();
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT player FROM player "
                    + "WHERE player_id IN (SELECT player_id FROM whitelist);");
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public synchronized String[] getNicks(String ip) {
        ArrayList<String> list = new ArrayList<String>();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT player FROM player "
                    + "WHERE player_id IN (SELECT player_id FROM ip WHERE ip=?);");
            pst.setString(1, ip);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public synchronized HashMap<String, Long> getTempBans() {
        HashMap<String, Long> map = new HashMap<String, Long>();
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT player,until FROM player_ban "
                    + "JOIN player ON player_ban.player_id=player.player_id "
                    + "WHERE until IS NOT NULL;");
            while (rs.next()) {
                if (rs.getTimestamp(2).getTime() == 100000) {
                    continue;
                }
                map.put(rs.getString(1), rs.getTimestamp(2).getTime());
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
        }

        return map;
    }

    @Override
    public synchronized HashMap<String, String> getBanInformation(String nick) {
        HashMap<String, String> map = new HashMap<String, String>();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT admin,reason,until FROM player_ban "
                    + "WHERE player_id=(SELECT player_id FROM player WHERE player=?);");
            pst.setString(1, nick);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                map.put("admin", rs.getString(1));
                if (rs.getString(2) != null) {
                    map.put("reason", rs.getString(2));
                }
                if (rs.getTimestamp(3) != null) {
                    map.put("until",
                            String.valueOf(rs.getTimestamp(3).getTime()));
                }
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }

        return map;
    }

    @Override
    public synchronized HashMap<String, String> getBanInformation(Subnet subnet) {
        HashMap<String, String> map = new HashMap<String, String>();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(
                    "SELECT admin,reason FROM subnet_ban WHERE subnet=?;");
            pst.setString(1, subnet.toString());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                map.put("admin", rs.getString(1));
                if (rs.getString(2) != null) {
                    map.put("reason", rs.getString(2));
                }
            }
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }

        if (map.isEmpty()){
            return null;
        }
        return map;
    }
}
