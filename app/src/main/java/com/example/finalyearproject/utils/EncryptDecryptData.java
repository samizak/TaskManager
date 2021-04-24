package com.example.finalyearproject.utils;

import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class EncryptDecryptData {

    private static final String password = "AES";
    private final Cipher cipher;
    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public EncryptDecryptData() throws Exception {
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        keySpec = GetKey();
        ivSpec = GetIV();
    }

    // Generate a key spec from the password
    private SecretKeySpec GetKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    // Generate random IV
    public IvParameterSpec GetIV() {
        byte[] iv = new byte[16];
        return new IvParameterSpec(iv);
    }

    public String EncryptData(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return new String(Base64.encode(encrypted, Base64.DEFAULT), StandardCharsets.UTF_8);
    }

    public String DecryptData(String encryptedText) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] bytes = Base64.decode(encryptedText, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(bytes);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}