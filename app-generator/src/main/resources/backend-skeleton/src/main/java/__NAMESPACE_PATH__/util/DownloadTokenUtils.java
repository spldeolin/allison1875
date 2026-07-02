package __NAMESPACE__.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.common.ErrorCode;

/**
 * 无状态HMAC-SHA256签名下载令牌工具
 *
 * <p>token = base64url(payload) + "." + base64url(HMAC-SHA256(secret, payload))
 * <p>payload = fileKey + "|" + expireAtEpochMilli
 *
 * @author Deolin 2026-07-02
 */
public class DownloadTokenUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private DownloadTokenUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 签发下载令牌
     *
     * @param fileKey    文件Key
     * @param secret     签名密钥
     * @param ttlSeconds 有效期（秒）
     * @return 令牌字符串
     */
    public static String sign(String fileKey, String secret, long ttlSeconds) {
        long expireAt = System.currentTimeMillis() + ttlSeconds * 1000L;
        String payload = fileKey + "|" + expireAt;
        String signature = hmac(secret, payload);
        return base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8)) + "." + signature;
    }

    /**
     * 校验下载令牌并返回fileKey
     *
     * @param token  令牌字符串
     * @param secret 签名密钥
     * @return fileKey
     * @throws BizException 令牌非法、签名不符或已过期
     */
    public static String verify(String token, String secret) {
        if (token == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌为空");
        }
        int dot = token.lastIndexOf('.');
        if (dot < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        String payloadPart = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(payloadPart), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        String expected = hmac(secret, payload);
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌签名校验失败");
        }
        int bar = payload.lastIndexOf('|');
        if (bar < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        String fileKey = payload.substring(0, bar);
        long expireAt;
        try {
            expireAt = Long.parseLong(payload.substring(bar + 1));
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        if (System.currentTimeMillis() > expireAt) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌已过期");
        }
        return fileKey;
    }

    private static String hmac(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(raw);
        } catch (Exception e) {
            throw new RuntimeException("HMAC计算失败", e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
