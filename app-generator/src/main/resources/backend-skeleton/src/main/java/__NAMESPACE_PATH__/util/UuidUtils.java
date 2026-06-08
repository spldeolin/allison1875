package __NAMESPACE__.util;

import java.util.UUID;

/**
 * @author Deolin 2025-11-25
 */
public class UuidUtils {

    private UuidUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String generateShort() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }

}
