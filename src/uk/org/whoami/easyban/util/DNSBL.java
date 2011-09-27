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
                    return true;
                }
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
            }
        }
        return false;
    }
}