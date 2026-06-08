package __NAMESPACE__.util;

import java.security.SecureRandom;

/**
 * @author Deolin 2026-05-26
 */
public class SecretKeyUtils {

    /**
     * URL 路径安全字符集：A-Z、a-z、0-9、连字符、下划线。
     * 不包含斜线、问号、井号、百分号等 URL 特殊字符。
     */
    private static final String URL_SAFE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecretKeyUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 生成指定长度的高熵、URL 路径安全的密钥字符串。
     *
     * <p>使用 {@link SecureRandom} 从 64 字符的字符集（字母、数字、
     * {@code -}、{@code _}）中随机选取。
     *
     * @param length 期望的密钥长度（必须为正整数）
     * @return 可安全用于 URL 路径的随机密钥字符串
     */
    public static String generateUrlSafeKey(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = URL_SAFE_ALPHABET.charAt(SECURE_RANDOM.nextInt(URL_SAFE_ALPHABET.length()));
        }
        return new String(buf);
    }

}
