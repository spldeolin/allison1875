package com.spldeolin.allison1875.base.util;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Base64;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Deolin 2021-04-11
 */
public class ShortUUIDUtil {


    public static String generateKey() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidArray = toByteArray(uuid);
        byte[] encodedArray = Base64.getEncoder().encode(uuidArray);
        String returnValue = new String(encodedArray);
        returnValue = StringUtils.removeEnd(returnValue, "\r\n");
        return returnValue;
    }

    public static UUID convertKey(String key) {
        UUID returnValue = null;
        if (StringUtils.isNotBlank(key)) {
            // Convert base64 string to a byte array

            byte[] decodedArray = Base64.getDecoder().decode(key);
            returnValue = fromByteArray(decodedArray);
        }
        return returnValue;
    }

    private static byte[] toByteArray(UUID uuid) {
        byte[] byteArray = new byte[(Long.SIZE / Byte.SIZE) * 2];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        LongBuffer longBuffer = buffer.asLongBuffer();
        longBuffer.put(new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()});
        return byteArray;
    }

    private static UUID fromByteArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        LongBuffer longBuffer = buffer.asLongBuffer();
        return new UUID(longBuffer.get(0), longBuffer.get(1));
    }

}