package com.spldeolin.allison1875.gadget.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.gadget.LineCounterConfig;
import lombok.extern.log4j.Log4j2;

/**
 * 源码行数计数器
 *
 * @author Deolin 2020-10-28
 */
@Singleton
@Log4j2
public class LineCounter implements Allison1875MainProcessor {

    @Inject
    private LineCounterConfig lineCounterConfig;

    @Inject
    private BaseConfig baseConfig;

    @Override
    public void process(AstForest astForest) {
        // with src/test/java
        if (lineCounterConfig.getWithTest()) {
            astForest = new AstForest(astForest.getAnyClassFromHost(), Lists.newArrayList(
                    astForest.getHost().resolve(baseConfig.getTestJavaDirectoryLayout()).toString()));
        }
        AstForestContext.setCurrent(astForest);

        Map<String, Integer> allJavas = Maps.newHashMap();
        Map<String, Integer> allTypes = Maps.newHashMap();
        Map<String, Integer> allMethods = Maps.newHashMap();
        for (CompilationUnit cu : astForest) {
            allJavas.put(Locations.getRelativePath(cu).toString(), getLineCount(cu));
            for (TypeDeclaration<?> type : cu.findAll(TypeDeclaration.class)) {
                type.getFullyQualifiedName()
                        .ifPresent(typeQualifier -> allTypes.put(typeQualifier, getLineCount(type)));
            }
            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
                if (method.getRange().isPresent()) {
                    allMethods.put(MethodQualifiers.getTypeQualifierWithMethodName(method), getLineCount(method));
                }
            }
        }

        // 所有java代码
        String rankListTitlePart = lineCounterConfig.getRankListSize() > 0 ? "，排行：" : "";
        log.info("");
        log.info("所有java代码总行数：{}{}", valuesSum(allJavas), rankListTitlePart);
        reportRankList(allJavas);

        // xxx结尾的类型
        for (String postfix : lineCounterConfig.getTypePostfix()) {
            Map<String, Integer> postfixTypes = Maps.newHashMap();
            allTypes.forEach((typeQualifier, count) -> {
                if (typeQualifier.endsWith(postfix)) {
                    postfixTypes.put(typeQualifier, count);
                }
            });
            log.info("以「{}」结尾的类型总行数：{}{}", postfix, valuesSum(postfixTypes), rankListTitlePart);
            reportRankList(postfixTypes);
        }

        // 方法
        log.info("方法总行数：{}{}", valuesSum(allMethods), rankListTitlePart);
        reportRankList(allMethods);

        // 所有xml代码
        Map<String, Integer> allXmls = Maps.newHashMap();
        for (File xml : detectXmls(astForest, lineCounterConfig.getWithTest())) {
            try {
                String xmlPath = AstForestContext.getCurrent().getCommonPathPart().relativize(xml.toPath()).normalize()
                        .toString();
                allXmls.put(xmlPath, (int) Files.lines(xml.toPath()).count());
            } catch (IOException e) {
                log.error("xml={}", xml, e);
            }
        }
        log.info("所有xml代码总行数：{}{}", valuesSum(allXmls), rankListTitlePart);
        reportRankList(allXmls);

    }

    private Collection<File> detectXmls(AstForest astForest, boolean withTest) {
        Collection<File> result = Lists.newArrayList();
        FileUtils.iterateFiles(astForest.getHost().resolve(baseConfig.getResourcesDirectoryLayout()).toFile(),
                new String[]{"xml"}, true).forEachRemaining(result::add);
        if (withTest) {
            File directory = astForest.getHost().resolve(baseConfig.getTestResourcesDirectoryLayout()).toFile();
            if (directory.exists()) {
                FileUtils.iterateFiles(directory, new String[]{"xml"}, true).forEachRemaining(result::add);
            }
        }
        return result;
    }

    private int valuesSum(Map<String, Integer> allXmls) {
        return allXmls.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void reportRankList(Map<String, Integer> lineCounts) {
        // 显示阈值
        lineCounts.values().removeIf(count -> count < lineCounterConfig.getDisplayThreshold());

        // 用户不需要打印排行榜
        int rankListSize = lineCounterConfig.getRankListSize();
        if (rankListSize == 0) {
            log.info("");
            return;
        }

        // 没有行数数据
        if (lineCounts.size() == 0) {
            log.info(BaseConstant.SINGLE_INDENT + "没有此类代码 或是 均小于显示阈值");
            log.info("");
            return;
        }

        List<Entry<String, Integer>> list = Lists.newArrayList(lineCounts.entrySet());
        // asc sort
        list.sort(Entry.comparingByValue());
        // subList
        if (lineCounts.size() > rankListSize) {
            list = list.subList(lineCounts.size() - rankListSize, lineCounts.size());
        }
        // reverse as desc
        Collections.reverse(list);
        // report
        for (Entry<String, Integer> entry : list) {
            boolean danger = entry.getValue() >= lineCounterConfig.getDangerThreshold();
            log.info(BaseConstant.SINGLE_INDENT + (danger ? "[危] " : "") + entry.getKey() + "：" + entry.getValue());
        }
        // more...
        if (lineCounts.size() > rankListSize) {
            log.info("...");
        }
        log.info("");
    }

    private int getLineCount(Node node) {
        return node.getRange().map(Range::getLineCount).orElse(0);
    }

}