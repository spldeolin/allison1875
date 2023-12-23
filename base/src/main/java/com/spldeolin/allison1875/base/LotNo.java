package com.spldeolin.allison1875.base;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 生产批号
 *
 * <pre>
 *  第一段，2位，具体工具缩写，例如QT、HT、PG、DA
 *  第二段，5位，Allison 1875版本+release版或snapshot版标识，例如0802R、0803S
 *  第三段，1位，<code>-</code>
 *  第四段，9位，32位murmur3算法对指定信息的hash，例如9F65D43A
 * </pre>
 *
 * @author Deolin 2021-08-25
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LotNo {

    public static final String TAG_PREFIXION = "Allison 1875 Lot No: ";

    public static final String NO_MANUAL_MODIFICATION = " (don't modify manually)";

    final ModuleAbbr moduleAbbr;

    final String versionText;

    final String hash;

    final Boolean unmodifiable;

    final String text;

    private LotNo(ModuleAbbr moduleAbbr, String versionText, String hash, Boolean unmodifiable) {
        this.moduleAbbr = moduleAbbr;
        this.versionText = versionText;
        this.hash = hash;
        this.unmodifiable = unmodifiable;
        this.text = String.format("%s%s-%s", moduleAbbr, Version.lotNoVersion, hash);
    }

    public static LotNo build(ModuleAbbr moduleAbbr, String information, Boolean unmodifiable) {
        String hash = StringUtils.upperCase(
                Hashing.murmur3_32().hashString(information, StandardCharsets.UTF_8).toString());
        return new LotNo(moduleAbbr, Version.lotNoVersion, hash, unmodifiable);
    }

    public String asJavadocDescription() {
        return "\n\n<p> " + TAG_PREFIXION + this + (unmodifiable ? NO_MANUAL_MODIFICATION : "");
    }

    public String asXmlComment() {
        return "<!-- " + TAG_PREFIXION + this + (unmodifiable ? NO_MANUAL_MODIFICATION : "") + " -->";
    }

    @Override
    public String toString() {
        return text;
    }

    public enum ModuleAbbr {
        DA, HT, PG, QT
    }

}