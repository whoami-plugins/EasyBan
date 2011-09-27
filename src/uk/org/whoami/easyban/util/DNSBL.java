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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import sun.net.dns.ResolverConfiguration;
import uk.org.whoami.easyban.ConsoleLogger;

public class DNSBL {

    private static String[] RECORD_TYPES = {"A"};
    private DirContext ictx;
    private List<String> lookupServices = new ArrayList<String>();

    public DNSBL() throws NamingException {
        StringBuilder dnsServers = new StringBuilder("");
        List nameservers = ResolverConfiguration.open().nameservers();
        for (Object dns : nameservers) {
            dnsServers.append("dns://").append(dns).append(" ");
        }

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "4000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");
        env.put(Context.PROVIDER_URL, dnsServers.toString());

        ictx = new InitialDirContext(env);
    }

    public void addLookupService(String service) {
        lookupServices.add(service);
    }

    public boolean isBlocked(String ip) {
        String[] parts = ip.split("\\.");
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            buffer.insert(0, '.');
            buffer.insert(0, parts[i]);
        }
        ip = buffer.toString();

        for (String service : lookupServices) {
            String lookupHost = ip + service;
            try {
                Attributes attributes = ictx.getAttributes(lookupHost, RECORD_TYPES);
                Attribute attribute = attributes.get("A");

                if (attribute != null) {
                    ConsoleLogger.info("[DNSBL] " + ip + " listed on " + service);
                    return true;
                }
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
            }
        }
        ConsoleLogger.info("[DNSBL] " + ip + " is not listed");
        return false;
    }
}