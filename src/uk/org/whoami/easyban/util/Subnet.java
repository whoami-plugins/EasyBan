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
package uk.org.whoami.easyban.util;

import java.net.InetAddress;

public class Subnet {

    short[] subnet;
    short[] mask;

    public Subnet(short[] subnet, short[] mask) {
        if (subnet.length > mask.length) {
            throw new IllegalArgumentException("Invalid Subnet");
        }
        this.subnet = subnet;
        this.mask = mask;
    }

    public Subnet(short[] subnet, int bits) {
        this(subnet, Subnet.cidrToArray(bits));
    }

    public Subnet(InetAddress subnet, InetAddress mask) {
        this(Subnet.inetAddressToArray(subnet), Subnet.inetAddressToArray(mask));
    }

    public Subnet(InetAddress subnet, int cidr) {
        this(Subnet.inetAddressToArray(subnet), Subnet.cidrToArray(cidr));
    }

    public boolean isIpInSubnet(short[] ip) {
        if (mask.length < ip.length) {
            throw new IllegalArgumentException("Invalid Subnet");
        }

        for (int i = 0; i < ip.length; i++) {
            short tmp = (short) (255 - mask[i]);

            if (ip[i] - subnet[i] > tmp || ip[i] - subnet[i] < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isIpInSubnet(InetAddress ip) {
        return this.isIpInSubnet(Subnet.inetAddressToArray(ip));
    }

    @Override
    public String toString() {
        return subnet.length > 4 ? toIpv6String() : toIpv4String();
    }

    private String toIpv4String() {
        String ret = "";
        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                ret += ".";
            }
            ret += subnet[i];
        }
        ret += "/";
        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                ret += ".";
            }
            ret += mask[i];
        }
        return ret;
    }

    private String toIpv6String() {
        String ret = "";
        for (int i = 0; i < 16; i += 2) {
            if (i != 0) {
                ret += ":";
            }
            ret += Integer.toHexString(subnet[i]);

            String tmp = Integer.toHexString(subnet[i + 1]);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            ret += tmp;
        }
        ret += "/";
        for (int i = 0; i < 16; i += 2) {
            if (i != 0) {
                ret += ":";
            }
            ret += Integer.toHexString(mask[i]);
            String tmp = Integer.toHexString(mask[i + 1]);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            ret += tmp;
        }
        return ret;
    }

    public static short[] cidrToArray(int bits) {
        short[] mask = new short[16];

        for (int i = 0; i < 16; i++) {
            if (bits > 8) {
                mask[i] = 255;
                bits -= 8;
            } else {
                mask[i] = (short) (256 - Math.pow(2, (8 - bits)));
                bits = 0;
            }
        }
        return mask;
    }

    public static short[] inetAddressToArray(InetAddress ip) {
        byte[] addr = ip.getAddress();
        short[] ret = new short[addr.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (short) (0x000000FF & ((int) addr[i]));
        }
        return ret;
    }
}
