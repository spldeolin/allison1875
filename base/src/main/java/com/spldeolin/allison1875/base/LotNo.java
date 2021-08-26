package com.spldeolin.allison1875.base;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;

/**
 * 生产批号
 *
 * <pre>
 *  第一段，2位，具体工具缩写，例如QT、HT、PG、DA
 *  第二段，5位，Allison 1875版本+release版或snapshot版标识，例如0802R、0803S
 *  第三段，1位，<code>-</code>
 *  第三段，9位，32位murmur3算法对指定信息的hash，例如B75FD0B4
 * </pre>
 *
 * @author Deolin 2021-08-25
 */
@AllArgsConstructor
public class LotNo {

    private final ModuleAbbr moduleAbbr;

    private final String information;

    @Override
    public String toString() {
        String hash = StringUtils.upperCase(
                Hashing.murmur3_32().hashString(information, StandardCharsets.UTF_8).toString());
        return String.format("%s%s-%s", moduleAbbr, Version.lotNoVersion, hash);
    }

    public String asJavadocDescription() {
        return "\n<p> Allison 1875 Lot No: " + this;
    }

    public String asXmlComment() {
        return "<!-- Allison 1875 Lot No: " + this + " -->";
    }

    public enum ModuleAbbr {
        DA, HT, PG, QT
    }

}