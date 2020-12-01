package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.docanalyzer.processor.DocAnalyzer;

/**
 * @author Deolin 2020-04-27
 */
@JsonInclude(Include.NON_NULL)
public class JsonPropertyDescriptionValueDto {

    private Collection<String> descriptionLines;

    /**
     * 解析自声明在Field上的校验注解
     *
     * e.g: @NotEmpty private Collection<String> userNames;
     */
    private Collection<ValidatorDto> valids = Lists.newArrayList();

    private String jsonFormatPattern;

    private Boolean isFieldCrossingValids = false;

    private Boolean docIgnore = false;

    private String referencePath;

    private Collection<EnumCodeAndTitleDto> ecats;

    /**
     * 类型名
     *
     * e.g.: String
     * UserStatusEnum
     */
    private String extraInfo;

    public JsonPropertyDescriptionValueDto() {
    }

    public String toStringPrettily() {
        if (isFieldCrossingValids) {
            StringBuilder sb = new StringBuilder(64);
            for (ValidatorDto valid : valids) {
                sb.append(valid.getValidatorType()).append(valid.getNote()).append("\n");
            }
            return sb.toString();
        } else {

            String ref = null;
            if (referencePath != null) {
                ref = "复用类型\n" + "\t数据结构同：" + referencePath;
            }
            String comment = null;
            if (CollectionUtils.isNotEmpty(descriptionLines)) {
                StringBuilder sb = new StringBuilder();
                for (String line : descriptionLines) {
                    if (StringUtils.isNotBlank(line)) {
                        sb.append("\t").append(line).append("\n");
                    } else {
                        sb.append("\n");
                    }
                }
                // sb并不是只有换号符时
                if (StringUtils.isNotBlank(sb)) {
                    sb.insert(0, "注释\n");
                    comment = sb.deleteCharAt(sb.length() - 1).toString();
                }
            }
            String validInfo = null;
            if (CollectionUtils.isNotEmpty(valids)) {
                StringBuilder sb = new StringBuilder("校验项\n");
                for (ValidatorDto valid : valids) {
                    sb.append("\t").append(valid.getValidatorType()).append(valid.getNote()).append("\n");
                }
                validInfo = sb.deleteCharAt(sb.length() - 1).toString();
            }
            String format = null;
            if (jsonFormatPattern != null) {
                format = "格式\n";
                format += "\t" + jsonFormatPattern;
            }
            String enumInfo = null;
            if (CollectionUtils.isNotEmpty(ecats)) {
                StringBuilder sb = new StringBuilder("枚举项\n");
                Map<String, String> catsMap = Maps.newHashMap();
                for (EnumCodeAndTitleDto ecat : ecats) {
                    catsMap.put(ecat.getCode(), ecat.getTitle());
                }
                for (String line : StringUtils.splitLineByLine(JsonUtils.toJsonPrettily(catsMap))) {
                    sb.append("\t").append(line).append("\n");
                }
                enumInfo = sb.deleteCharAt(sb.length() - 1).toString();
            }
            String extra = null;
            if (extraInfo != null) {

                // TODO 抽取到handle
                StringBuilder sb = new StringBuilder("枚举名\n");
                String appName = DocAnalyzer.CONFIG.get().getGlobalUrlPrefix().replace("/", "");
                sb.append("\t").append(appName).append(":").append(extraInfo);
                extra = sb.toString();
            }

            return Joiner.on("\n\n").skipNulls().join(ref, comment, validInfo, format, enumInfo, extra);
        }
    }

    public Collection<String> getDescriptionLines() {
        return this.descriptionLines;
    }

    public Collection<ValidatorDto> getValids() {
        return this.valids;
    }

    public String getJsonFormatPattern() {
        return this.jsonFormatPattern;
    }

    public Boolean getIsFieldCrossingValids() {
        return this.isFieldCrossingValids;
    }

    public Boolean getDocIgnore() {
        return this.docIgnore;
    }

    public String getReferencePath() {
        return this.referencePath;
    }

    public Collection<EnumCodeAndTitleDto> getEcats() {
        return this.ecats;
    }

    public JsonPropertyDescriptionValueDto setDescriptionLines(Collection<String> descriptionLines) {
        this.descriptionLines = descriptionLines;
        return this;
    }

    public JsonPropertyDescriptionValueDto setValids(Collection<ValidatorDto> valids) {
        this.valids = valids;
        return this;
    }

    public JsonPropertyDescriptionValueDto setJsonFormatPattern(String jsonFormatPattern) {
        this.jsonFormatPattern = jsonFormatPattern;
        return this;
    }

    public JsonPropertyDescriptionValueDto setIsFieldCrossingValids(Boolean isFieldCrossingValids) {
        this.isFieldCrossingValids = isFieldCrossingValids;
        return this;
    }

    public JsonPropertyDescriptionValueDto setDocIgnore(Boolean docIgnore) {
        this.docIgnore = docIgnore;
        return this;
    }

    public JsonPropertyDescriptionValueDto setReferencePath(String referencePath) {
        this.referencePath = referencePath;
        return this;
    }

    public JsonPropertyDescriptionValueDto setEcats(Collection<EnumCodeAndTitleDto> ecats) {
        this.ecats = ecats;
        return this;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JsonPropertyDescriptionValueDto)) {
            return false;
        }
        final JsonPropertyDescriptionValueDto other = (JsonPropertyDescriptionValueDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$descriptionLines = this.getDescriptionLines();
        final Object other$descriptionLines = other.getDescriptionLines();
        if (this$descriptionLines == null ? other$descriptionLines != null
                : !this$descriptionLines.equals(other$descriptionLines)) {
            return false;
        }
        final Object this$valids = this.getValids();
        final Object other$valids = other.getValids();
        if (this$valids == null ? other$valids != null : !this$valids.equals(other$valids)) {
            return false;
        }
        final Object this$jsonFormatPattern = this.getJsonFormatPattern();
        final Object other$jsonFormatPattern = other.getJsonFormatPattern();
        if (this$jsonFormatPattern == null ? other$jsonFormatPattern != null
                : !this$jsonFormatPattern.equals(other$jsonFormatPattern)) {
            return false;
        }
        final Object this$isFieldCrossingValids = this.getIsFieldCrossingValids();
        final Object other$isFieldCrossingValids = other.getIsFieldCrossingValids();
        if (this$isFieldCrossingValids == null ? other$isFieldCrossingValids != null
                : !this$isFieldCrossingValids.equals(other$isFieldCrossingValids)) {
            return false;
        }
        final Object this$docIgnore = this.getDocIgnore();
        final Object other$docIgnore = other.getDocIgnore();
        if (this$docIgnore == null ? other$docIgnore != null : !this$docIgnore.equals(other$docIgnore)) {
            return false;
        }
        final Object this$referencePath = this.getReferencePath();
        final Object other$referencePath = other.getReferencePath();
        if (this$referencePath == null ? other$referencePath != null
                : !this$referencePath.equals(other$referencePath)) {
            return false;
        }
        final Object this$ecats = this.getEcats();
        final Object other$ecats = other.getEcats();
        return this$ecats == null ? other$ecats == null : this$ecats.equals(other$ecats);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof JsonPropertyDescriptionValueDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $descriptionLines = this.getDescriptionLines();
        result = result * PRIME + ($descriptionLines == null ? 43 : $descriptionLines.hashCode());
        final Object $valids = this.getValids();
        result = result * PRIME + ($valids == null ? 43 : $valids.hashCode());
        final Object $jsonFormatPattern = this.getJsonFormatPattern();
        result = result * PRIME + ($jsonFormatPattern == null ? 43 : $jsonFormatPattern.hashCode());
        final Object $isFieldCrossingValids = this.getIsFieldCrossingValids();
        result = result * PRIME + ($isFieldCrossingValids == null ? 43 : $isFieldCrossingValids.hashCode());
        final Object $docIgnore = this.getDocIgnore();
        result = result * PRIME + ($docIgnore == null ? 43 : $docIgnore.hashCode());
        final Object $referencePath = this.getReferencePath();
        result = result * PRIME + ($referencePath == null ? 43 : $referencePath.hashCode());
        final Object $ecats = this.getEcats();
        result = result * PRIME + ($ecats == null ? 43 : $ecats.hashCode());
        return result;
    }

    public String toString() {
        return "JsonPropertyDescriptionValueDto(descriptionLines=" + this.getDescriptionLines() + ", valids=" + this
                .getValids() + ", jsonFormatPattern=" + this.getJsonFormatPattern() + ", isFieldCrossingValids=" + this
                .getIsFieldCrossingValids() + ", docIgnore=" + this.getDocIgnore() + ", referencePath=" + this
                .getReferencePath() + ", ecats=" + this.getEcats() + ")";
    }

}