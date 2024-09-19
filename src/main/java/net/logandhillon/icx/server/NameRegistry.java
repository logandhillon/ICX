package net.logandhillon.icx.server;

import java.net.InetAddress;
import java.util.HashMap;

public class NameRegistry {
    private static final HashMap<String, InetAddress> REGISTRY = new HashMap<>();

    public void registerName(String name, InetAddress registrant) {
        if (REGISTRY.containsKey(name)) throw new RuntimeException("Name taken");
        REGISTRY.put(name, registrant);
    }

    public void releaseName(String name) {
        REGISTRY.remove(name);
    }

    public boolean verifyName(String name, InetAddress addr) {
        return REGISTRY.containsKey(name) && REGISTRY.get(name) == addr;
    }
}
