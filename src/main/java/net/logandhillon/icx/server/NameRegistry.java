package net.logandhillon.icx.server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.net.InetAddress;
import java.util.HashMap;

public class NameRegistry {
    private static final Logger LOG = LoggerContext.getContext().getLogger(NameRegistry.class);
    private static final HashMap<String, InetAddress> REGISTRY = new HashMap<>();

    public void registerName(String name, InetAddress registrant) {
        if (REGISTRY.containsKey(name)) throw new RuntimeException("Name taken");
        LOG.info("Registering '{}' to {}", name, registrant);
        REGISTRY.put(name, registrant);
    }

    public void releaseName(String name) {
        if (name == null) return;
        LOG.info("Releasing '{}'", name);
        REGISTRY.remove(name);
    }

    public boolean verifyName(String name, InetAddress addr) {
        return REGISTRY.containsKey(name) && REGISTRY.get(name) == addr;
    }
}
