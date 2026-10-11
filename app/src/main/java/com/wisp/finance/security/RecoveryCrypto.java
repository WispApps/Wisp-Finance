package com.wisp.finance.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;

/** Portable v1 archive. Random 184-bit recovery entropy; HKDF-SHA256 + AES-256-GCM. */
public final class RecoveryCrypto {
    public static final int MAX_BYTES = 32 * 1024 * 1024;
    private static final byte[] MAGIC = "WFINENC1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] INFO = "Wisp Finance archive key v1".getBytes(StandardCharsets.UTF_8);
    private static final SecureRandom RANDOM = new SecureRandom();
    private RecoveryCrypto() {}
    public static boolean isEncrypted(byte[] input) {
        return input.length >= 8 && Arrays.equals(MAGIC, Arrays.copyOf(input, 8));
    }
    private static byte[] key(byte[] entropy, byte[] salt) throws GeneralSecurityException {
        if (entropy.length != 23) throw new IllegalArgumentException("Invalid recovery key");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(entropy);
        try {
            mac.init(new SecretKeySpec(prk, "HmacSHA256"));
            mac.update(INFO);
            return mac.doFinal(new byte[]{1});
        } finally { Arrays.fill(prk, (byte)0); }
    }
    public static byte[] encrypt(byte[] plain, byte[] entropy) throws GeneralSecurityException {
        if (plain.length > MAX_BYTES) throw new IllegalArgumentException("Archive too large");
        byte[] header = new byte[36];
        System.arraycopy(MAGIC, 0, header, 0, 8);
        byte[] salt = new byte[16], iv = new byte[12];
        RANDOM.nextBytes(salt); RANDOM.nextBytes(iv);
        System.arraycopy(salt,0,header,8,16); System.arraycopy(iv,0,header,24,12);
        byte[] key = key(entropy,salt);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            cipher.updateAAD(header);
            byte[] encrypted = cipher.doFinal(plain), result = Arrays.copyOf(header,header.length+encrypted.length);
            System.arraycopy(encrypted,0,result,header.length,encrypted.length);
            return result;
        } finally { Arrays.fill(key,(byte)0); }
    }
    public static byte[] decrypt(byte[] input, byte[] entropy) throws GeneralSecurityException {
        if (!isEncrypted(input) || input.length < 52 || input.length > MAX_BYTES+52)
            throw new IllegalArgumentException("Invalid archive");
        byte[] key = key(entropy,Arrays.copyOfRange(input,8,24));
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,Arrays.copyOfRange(input,24,36)));
            cipher.updateAAD(Arrays.copyOf(input,36));
            return cipher.doFinal(input,36,input.length-36);
        } finally { Arrays.fill(key,(byte)0); }
    }
}
