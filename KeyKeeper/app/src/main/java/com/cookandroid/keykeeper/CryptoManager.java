package com.cookandroid.keykeeper;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptoManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "keykeeper_aes_key"; //key store에서 사용할 aes 키 이름
    private static final int GCM_TAG_LENGTH = 128;  //aes-gcm 인증 태그 길이

    //키 가져옴
    private static SecretKey getOrCreateSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        //키 존재 시 해당 키 반환
        if(keyStore.containsAlias(KEY_ALIAS)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            return entry.getSecretKey();
        }

        //key store에 aes 키 없을 시 생성
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES
                , ANDROID_KEYSTORE
        );

        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS
                , KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)   //aes-gcm 모드 사용
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)   //gcm에서는 NoPadding 사용
        .setUserAuthenticationRequired(false)   //인증 미요구
        .build();

        keyGenerator.init(keySpec);

        return keyGenerator.generateKey();
    }

    //pw를 aes-gcm 암호화
    public static EncryptResultDto encryptPw(String pw) throws Exception {
        SecretKey secretKey = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(pw.getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();

        /*
        //iv 생성
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        byte[] encryptedBytes = cipher.doFinal(pw.getBytes("UTF-8"));
        */

        //base64 문자열로 변환 (android.util.Base64)
        String encryptedPw = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
        String strIv = Base64.encodeToString(iv, Base64.NO_WRAP);

        return new EncryptResultDto(encryptedPw, strIv);
    }

    //암호화된 pw와 iv로 복호화
    public static String decryptPw(String encryptedPw, String strIv) throws Exception {
        SecretKey secretKey = getOrCreateSecretKey();

        byte[] encryptedBytes = Base64.decode(encryptedPw, Base64.NO_WRAP);
        byte[] iv = Base64.decode(strIv, Base64.NO_WRAP);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
