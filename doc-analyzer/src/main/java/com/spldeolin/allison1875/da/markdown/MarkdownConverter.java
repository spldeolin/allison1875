package com.spldeolin.allison1875.da.markdown;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.FreeMarkerPrintExcpetion;
import com.spldeolin.allison1875.da.dto.EndpointDto;
import com.spldeolin.allison1875.da.dto.PropertyDto;
import com.spldeolin.allison1875.da.dto.PropertyValidatorDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-18
 */
@Log4j2
public class MarkdownConverter {

    private static final String horizontalLine = "-";

    public void convert(Collection<EndpointDto> endpoints) {
        for (EndpointDto endpoint : endpoints) {
            EndpointVo vo = new EndpointVo();
            // 概要
            vo.setUri(Joiner.on("\n").join(endpoint.getUrls()));
            vo.setHttpMethod(Joiner.on("\n").join(endpoint.getHttpMethods()));
            vo.setDescription(endpoint.getDescription());

            // 参数结构
            vo.setRequestBodySituation(endpoint.getRequestBodySituation().getValue());
            vo.setRequestBodyJsonSchema(endpoint.getRequestBodyJsonSchema());
            if (endpoint.getRequestBodyProperties() != null) {
                Collection<RequestBodyPropertyVo> propVos = Lists.newArrayList();
                for (PropertyDto dto : endpoint.getRequestBodyProperties()) {
                    RequestBodyPropertyVo propVo = new RequestBodyPropertyVo();
                    propVo.setPath(dto.getPath());
                    propVo.setName(dto.getName());
                    propVo.setDescription(emptyToHorizontalLine(dto.getDescription()));
                    propVo.setJsonTypeAndFormats(dto.getJsonType().getValue() + " " + dto.getJsonFormat());
                    propVo.setValidators(convertValidators(dto.getRequired(), dto.getValidators()));
                    propVos.add(propVo);
                }
                vo.setRequestBodyProperties(propVos);
                vo.setAnyValidatorsExist(propVos.stream().anyMatch(one -> !horizontalLine.equals(one.getValidators())));
            }

            // 返回值结构
            vo.setResponseBodySituation(endpoint.getResponseBodySituation().getValue());
            vo.setResponseBodyJsonSchema(endpoint.getResponseBodyJsonSchema());
            if (endpoint.getResponseBodyProperties() != null) {
                Collection<ResponseBodyPropertyVo> propVos = Lists.newArrayList();
                for (PropertyDto dto : endpoint.getResponseBodyProperties()) {
                    ResponseBodyPropertyVo propVo = new ResponseBodyPropertyVo();
                    propVo.setPath(dto.getPath());
                    propVo.setName(dto.getName());
                    propVo.setDescription(emptyToHorizontalLine(dto.getDescription()));
                    propVo.setJsonTypeAndFormats(dto.getJsonType().getValue() + " " + dto.getJsonFormat());
                    propVos.add(propVo);
                }
                vo.setResponseBodyProperties(propVos);
            }

            vo.setAuthor(endpoint.getAuthor());
            vo.setSourceCode(endpoint.getSourceCode());

            try {
                FreeMarkerPrinter.print(vo, vo.getDescription());
            } catch (FreeMarkerPrintExcpetion e) {
                log.warn("FreeMarkerPrinter print failed.", e);
            }
        }

    }

    private String emptyToHorizontalLine(String linkName) {
        if (StringUtils.isEmpty(linkName)) {
            return horizontalLine;
        }
        return linkName;
    }

    private String convertValidators(Boolean required, Collection<PropertyValidatorDto> validators) {
        StringBuilder result = new StringBuilder(64);

        if (required) {
            result.append("必填");
        }

        if (validators != null && validators.size() > 0) {
            for (PropertyValidatorDto validator : validators) {
                result.append("　");
                result.append(validator.getValidatorType());
                result.append(validator.getNote());
            }
        }

        if (result.length() == 0) {
            return horizontalLine;
        } else {
            return result.toString();
        }
    }

}
