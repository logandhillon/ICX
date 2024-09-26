package net.logandhillon.icx.server;

import net.logandhillon.icx.common.SNVS;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Objects;

public class NameRegistry {
    private static final Logger LOG = LoggerContext.getContext().getLogger(NameRegistry.class);
    private static final HashMap<String, SNVS.InetToken> REGISTRY = new HashMap<>();
    public static final SNVS.Token SERVER = new SNVS.Token("SERVER", SNVS.genToken());

    public void registerName(SNVS.Token snvs, InetAddress registrant) {
        if (REGISTRY.containsKey(snvs.name())) throw new RuntimeException("Name taken");
        if (!snvs.validate()) throw new RuntimeException("Malformed or invalid SNVS");
        LOG.info("Registering '{}' to {}", snvs.name(), registrant);
        REGISTRY.put(snvs.name(), new SNVS.InetToken(registrant, snvs.token()));
    }

    public void releaseName(SNVS.Token snvs) {
        if (snvs == null || snvs.name() == null) return;
        LOG.info("Releasing '{}'", snvs.name());
        REGISTRY.remove(snvs.name());
    }

    public boolean verifyName(SNVS.Token snvs, InetAddress origin) {
        return REGISTRY.containsKey(snvs.name()) && REGISTRY.get(snvs.name()).registrant() == origin && Objects.equals(REGISTRY.get(snvs.name()).token(), snvs.token());
    }
}
