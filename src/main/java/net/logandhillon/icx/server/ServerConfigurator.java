package net.logandhillon.icx.server;

import org.apache.logging.log4j.core.jmx.Server;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;

public class ServerConfigurator {
    private static final String KS_FILE = "icx.jks";
    private static final String ALIAS = "icx_cert";
    private static final String KEY_ALG = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final int VALIDITY_DAYS = 36500; // 100 years

    public static void launch() {
        System.out.println("ICX Server Configurator [bundled]\nCopyright (c) 2024 Logan Dhillon\n");

        // generate keystore password
        System.out.print("Generating secure key... ");
        SecureRandom random = new SecureRandom();
        char[] password = new char[32];

        for (int i = 0; i < 32; i++) {
            int randomAscii = 32 + random.nextInt(126 - 32 + 1);
            password[i] = (char) randomAscii;
        }
        System.out.println("[OK]");

        try {
            System.out.print("Obtaining certificate... ");
            createKeystore(password);
            System.out.println("[OK]");
        } catch (Exception e) {
            System.out.println("[ERR]\nReason: " + e.getMessage());
        }

        // store certificate information in server.properties
        System.out.print("Storing credentials... ");
        Properties properties = new Properties();
        properties.setProperty("keystore.file", KS_FILE);
        properties.setProperty("keystore.password", new String(password));
        System.out.println("[OK]");

        // save server.properties
        System.out.print("Saving server configuration to disk... ");
        try (FileOutputStream fos = new FileOutputStream(ServerProperties.FILENAME)) {
            properties.store(fos, "Auto-generated ICX server configuration");
            System.out.println("[OK]");
        } catch (IOException e) {
            System.out.println("[ERR]\nReason: " + e.getMessage());
        }

        System.out.println("Done! Cleaning up and exiting SC mode.");
    }

    private static void createKeystore(char[] password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, OperatorCreationException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, password);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALG);
        keyGen.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        X509Certificate cert = generateSelfSignedCertificate(keyPair);

        ks.setKeyEntry(ALIAS, privateKey, password, new X509Certificate[]{cert});

        try (FileOutputStream fos = new FileOutputStream(KS_FILE)) {
            ks.store(fos, password);
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws OperatorCreationException, CertificateException {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + VALIDITY_DAYS * 24L * 60L * 60L * 1000L);

        // Distinguished Name (DN)
        X500Name dn = new X500Name("CN=icx_cert");

        BigInteger serialNumber = BigInteger.valueOf(now);
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dn, serialNumber, startDate, endDate, dn, keyPair.getPublic());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());

        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
    }

}
