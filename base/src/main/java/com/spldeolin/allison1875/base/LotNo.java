package com.spldeolin.allison1875.base;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import jodd.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

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
@AllArgsConstructor
@Data
public class LotNo {

    public static final String TAG_PREFIXION = "Allison 1875 Lot No: ";

    public static final String NO_MANUAL_MODIFICATION = " (don't modify manually)";

    private final ModuleAbbr moduleAbbr;

    private final String versionText;

    private final String hash;

    private final Boolean unmodifiable;

    private final String text;

    private LotNo(ModuleAbbr moduleAbbr, String versionText, String hash, Boolean unmodifiable) {
        this.moduleAbbr = moduleAbbr;
        this.versionText = versionText;
        this.hash = hash;
        this.unmodifiable = unmodifiable;
        this.text = String.format("%s%s-%s%s", moduleAbbr, Version.lotNoVersion, hash,
                unmodifiable ? NO_MANUAL_MODIFICATION : "");
    }

    public static LotNo build(ModuleAbbr moduleAbbr, String information, Boolean unmodifiable) {
        String hash = StringUtils.upperCase(
                Hashing.murmur3_32().hashString(information, StandardCharsets.UTF_8).toString());
        return new LotNo(moduleAbbr, Version.lotNoVersion, hash, unmodifiable);
    }

    public static LotNo parse(String text) {
        Preconditions.checkNotNull(text);
        String[] snippets = text.split("\\W");
        if (snippets.length != 2) {
            throw new IllegalArgumentException("cannot parse Lot No '" + text + "'");
        }
        ModuleAbbr moduleAbbr = ModuleAbbr.valueOf(snippets[0].substring(0, 2));
        String versionText = StringUtil.cutSuffix(snippets[0], moduleAbbr.toString());
        String hash = snippets[1];
        Boolean unmodifiable = text.contains(NO_MANUAL_MODIFICATION);
        return new LotNo(moduleAbbr, versionText, hash, unmodifiable);
    }

    public String asJavadocDescription() {
        return "\n\n<p> " + TAG_PREFIXION + this;
    }

    public String asXmlComment() {
        return "<!-- " + TAG_PREFIXION + this + " -->";
    }

    @Override
    public String toString() {
        return text;
    }

    public enum ModuleAbbr {
        DA, HT, PG, QT
    }

}