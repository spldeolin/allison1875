package com.spldeolin.allison1875.docanalyzer.service.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.service.EndpointToStringService;
import com.spldeolin.allison1875.docanalyzer.service.MoreHandlerAnalysisService;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class EndpointToStringServiceImpl implements EndpointToStringService {

    @Inject
    private DocAnalyzerConfig config;

    @Inject
    private MoreHandlerAnalysisService moreHandlerAnalysisService;

    @Override
    public String toString(EndpointDto dto) {
        String deprecatedNode = null;
        if (dto.getIsDeprecated()) {
            deprecatedNode = "> 该接口已被开发者标记为**已废弃**，不建议调用";
        }

        String comment = null;
        if (CollectionUtils.isNotEmpty(dto.getDescriptionLines())) {
            StringBuilder sb = new StringBuilder();
            for (String line : dto.getDescriptionLines()) {
                if (StringUtils.isNotBlank(line)) {
                    sb.append(line).append("\n");
                } else {
                    sb.append("\n");
                }
            }
            // sb并不是只有换号符时
            if (StringUtils.isNotBlank(sb)) {
                sb.insert(0, "##### 注释\n");
                comment = StringEscapeUtils.escapeHtml4(sb.deleteCharAt(sb.length() - 1).toString());
            }
        }

        String developer = "##### 开发者\n";
        if (StringUtils.isNotBlank(dto.getAuthor())) {
            developer += dto.getAuthor();
        } else {
            developer += "未知的开发者";
        }

        String code = "##### 源码\n";
        code += dto.getSourceCode();

        String moreText = moreHandlerAnalysisService.moreToString(dto.getMore());

        String allison1875Announce = "";
        if (config.getEnableNoModifyAnnounce() || config.getEnableLotNoAnnounce()) {
            allison1875Announce += BaseConstant.NEW_LINE + "---";
            if (config.getEnableNoModifyAnnounce()) {
                allison1875Announce += BaseConstant.NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
            }
            if (config.getEnableLotNoAnnounce()) {
                if (config.getEnableNoModifyAnnounce()) {
                    allison1875Announce += " ";
                } else {
                    allison1875Announce += BaseConstant.NEW_LINE;
                }
                String hash = StringUtils.upperCase(HashingUtils.hashString(JsonUtils.toJson(dto)));
                allison1875Announce +=
                        BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + String.format("DA%s-%s", Allison1875.SHORT_VERSION,
                                hash);
            }
        }

        return Joiner.on('\n').skipNulls()
                .join(deprecatedNode, comment, developer, code, moreText, allison1875Announce);
    }

}