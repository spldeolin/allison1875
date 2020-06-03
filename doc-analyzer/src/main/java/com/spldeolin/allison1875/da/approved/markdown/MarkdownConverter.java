//package com.spldeolin.allison1875.da.approved.markdown;
//
//import java.util.Collection;
//import org.apache.commons.lang3.StringUtils;
//import com.google.common.base.Joiner;
//import com.google.common.collect.Lists;
//import com.spldeolin.allison1875.base.exception.FreeMarkerPrintExcpetion;
//import com.spldeolin.allison1875.da.approved.dto.EndpointDto;
//import lombok.extern.log4j.Log4j2;
//
///**
// * @author Deolin 2020-02-18
// */
//@Log4j2
//public class MarkdownConverter {
//
//    public void convert(Collection<EndpointDto> endpoints) {
//        for (EndpointDto endpoint : endpoints) {
//            SimpleMdOutputVo vo = new SimpleMdOutputVo();
//            vo.setUri(Joiner.on("\n").join(endpoint.getUrls()));
//            vo.setDescription(endpoint.getDescription());
//            vo.setRequestBodySituation(endpoint.getRequestBodySituation().getValue());
//            vo.setResponseBodySituation(endpoint.getResponseBodySituation().getValue());
//            vo.setAuthor(endpoint.getAuthor());
//            vo.setSourceCode(endpoint.getSourceCode());
//
//            if (!vo.getIsRequestBodyChaos() && !vo.getIsRequestBodyNone()) {
//                Collection<RequestBodyFieldVo> fieldVos = Lists.newArrayList();
//                for (BodyFieldDefinition field : endpoint.listRequestBodyFieldsFlatly()) {
//                    RequestBodyFieldVo fieldVo = new RequestBodyFieldVo();
//                    fieldVo.setLinkName(emptyToHorizontalLine(field.getLinkName()));
//                    fieldVo.setDescription(nullToEmpty(field.getDescription()));
//                    fieldVo.setJsonTypeAndFormats(converterTypeAndFormat(field));
//                    fieldVo.setValidators(convertValidators(field.getNullable(), field.getValidators()));
//                    fieldVos.add(fieldVo);
//                }
//                vo.setRequestBodyFields(fieldVos);
//                vo.setAnyValidatorsExist(fieldVos.stream().anyMatch(fieldVo -> !"".equals(fieldVo.getValidators())));
//            }
//            if (!vo.getIsResponseBodyChaos() && !vo.getIsResponseBodyNone()) {
//                Collection<ResponseBodyFieldVo> fieldVos = Lists.newArrayList();
//                for (BodyFieldDefinition field : endpoint.listResponseBodyFieldsFlatly()) {
//                    ResponseBodyFieldVo fieldVo = new ResponseBodyFieldVo();
//                    fieldVo.setLinkName(emptyToHorizontalLine(field.getLinkName()));
//                    fieldVo.setDescription(nullToEmpty(field.getDescription()));
//                    fieldVo.setJsonTypeAndFormats(converterTypeAndFormat(field));
//                    fieldVos.add(fieldVo);
//                }
//                vo.setResponseBodyFields(fieldVos);
//            }
//
//            try {
//                FreeMarkerPrinter.print(vo, vo.getDescription());
//            } catch (FreeMarkerPrintExcpetion e) {
//                log.warn("FreeMarkerPrinter print failed.", e);
//            }
//        }
//
//    }
//
//    private String emptyToHorizontalLine(String linkName) {
//        if (StringUtils.isEmpty(linkName)) {
//            return "`-`";
//        }
//        return linkName;
//    }
//
//    private Collection<String> converterTypeAndFormat(BodyFieldDefinition field) {
//        Collection<String> result = Lists.newArrayList();
//        result.add(field.getJsonType().getValue());
//        String stringFormat = field.getStringFormat();
//        if (stringFormat != null && !StringFormatTypeEnum.normal.getValue().equals(stringFormat)) {
//            result.add(stringFormat);
//        }
//        NumberFormatTypeEnum numberFormat = field.getNumberFormat();
//        if (numberFormat != null) {
//            result.add(numberFormat.getValue());
//        }
//        return result;
//    }
//
//    private String convertValidators(Boolean nullable, Collection<ValidatorDefinition> validators) {
//        StringBuilder result = new StringBuilder(64);
//
//        if (Boolean.FALSE.equals(nullable)) {
//            result.append("必填");
//            result.append("　");
//        }
//
//        if (validators != null && validators.size() > 0) {
//            Collection<String> parts = Lists.newLinkedList();
//            validators.forEach(validator -> {
//                parts.add(validator.getValidatorType().getDescription());
//                parts.add(validator.getNote());
//            });
//            Joiner.on("　").skipNulls().appendTo(result, parts);
//        }
//
//        if (result.length() == 0) {
//            return "";
//        } else {
//            return result.toString();
//        }
//    }
//
//    private String nullToEmpty(String s) {
//        if (s == null) {
//            s = "";
//        }
//        return s;
//    }
//
//    private String emptyToDefault(String s, String defaultString) {
//        if (StringUtils.isEmpty(s)) {
//            return defaultString;
//        }
//        return s;
//    }
//
//}
