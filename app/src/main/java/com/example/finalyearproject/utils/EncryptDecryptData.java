package com.example.finalyearproject.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class is used for Encrypting or Decrypting JSON data
 */
public class EncryptDecryptData {

    private static final String password = "AES";

    private final Cipher cipher;
    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    /**
     * Initialises the cipher, keySpec and ivSpec for encryption or decryption
     */
    public EncryptDecryptData() throws Exception {
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        keySpec = GetKey();
        ivSpec = GetIV();
    }

    /**
     * Generate a key spec from the password
     * @return created and returns a new SecretKeySpec
     */
    private SecretKeySpec GetKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Generate random IV
     * @return created and returns a new IvParameterSpec
     */
    public IvParameterSpec GetIV() {
        byte[] iv = new byte[16];
        return new IvParameterSpec(iv);
    }

    /**
     *  Encrypts a JSON String
     * @param plainText a JSON String to be encrypted
     * @return the encrypted JSON String
     */
    public String EncryptData(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return new String(Base64.encode(encrypted, Base64.DEFAULT), StandardCharsets.UTF_8);
    }

    /**
     * Decrypts an Encrypted JSON String
     * @param encryptedText an encrypted String to be decrypted
     * @return the decrypted JSON String
     */
    public String DecryptData(String encryptedText) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] bytes = Base64.decode(encryptedText, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(bytes);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}