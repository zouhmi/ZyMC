package com.zouhmi.zymc.network.crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public final class RSAEngine {
    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 1024;

    private final KeyPair keyPair;

    public RSAEngine() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
            kpg.initialize(KEY_SIZE);
            this.keyPair = kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA keypair", e);
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public byte[] encryptPublicKeyDer() {
        return keyPair.getPublic().getEncoded(); // X.509 SubjectPublicKeyInfo DER
    }

    public byte[] encryptWithPublicKey(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    public byte[] decryptWithPrivateKey(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }

    public static byte[] encodePublicKeyDer(PublicKey publicKey) {
        return publicKey.getEncoded();
    }

    public static PublicKey decodePublicKeyDer(byte[] der) {
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode RSA public key", e);
        }
    }
}
