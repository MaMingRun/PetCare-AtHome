package com.mr.common.utils;

import com.mr.common.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class CryptoUtil {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public CryptoUtil(@Value("${pet.crypto.aes-key}") String aesKey) {
        byte[] kb = aesKey.getBytes(StandardCharsets.UTF_8);
        if (kb.length != 32) {
            throw new IllegalStateException("AES 密钥必须 32 字节，当前: " + kb.length);
        }
        this.key = new SecretKeySpec(kb, "AES");
    }

    /** 明文 -> Base64(IV(12) || 密文+tag) */
    public String encrypt(String plain) {
        byte[] iv = new byte[IV_LEN];
        random.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LEN + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, IV_LEN);
            System.arraycopy(ciphertext, 0, combined, IV_LEN, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new BaseException("凭证加密失败");
        }
    }

    /** Base64(IV || 密文+tag) -> 明文 */
    public String decrypt(String cipherText) {
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            if (combined.length < IV_LEN) {
                throw new BaseException("凭证密文格式错误");
            }
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LEN);
            byte[] ciphertext = Arrays.copyOfRange(combined, IV_LEN, combined.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("凭证解密失败（密钥变更或密文被篡改）");
        }
    }
}
