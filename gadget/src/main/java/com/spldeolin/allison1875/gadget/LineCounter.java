package com.spldeolin.allison1875.gadget;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.MavenPathResolver;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.util.FileTraverseUtils;
import com.spldeolin.allison1875.common.util.ast.Locations;
import com.spldeolin.allison1875.common.util.ast.MethodQualifiers;
import lombok.extern.log4j.Log4j2;

/**
 * 源码行数计数器
 *
 * @author Deolin 2020-10-28
 */
@Singleton
@Log4j2
public class LineCounter implements Allison1875MainService {

    @Inject
    private LineCounterConfig config;

    @Override
    public void process(AstForest astForest) {
        Map<String, Integer> allJavas = Maps.newHashMap();
        Map<String, Integer> allTypes = Maps.newHashMap();
        Map<String, Integer> allMethods = Maps.newHashMap();
        for (CompilationUnit cu : astForest) {
            allJavas.put(Locations.getAbsolutePath(cu).toString(), getLineCount(cu));
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
        String rankListTitlePart = config.getRankListSize() > 0 ? "，排行：" : "";
        log.info("");
        log.info("所有java代码总行数：{}{}", valuesSum(allJavas), rankListTitlePart);
        reportRankList(allJavas);

        // xxx结尾的类型
        for (String postfix : config.getTypePostfix()) {
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

        Path commonPath = calcCommonPath(astForest.getJavasInForest());

        // 所有xml代码
        Map<String, Integer> allXmls = Maps.newHashMap();
        for (File xml : detectXmls(astForest)) {
            try {
                String xmlPath = commonPath.relativize(xml.toPath()).normalize().toString();
                allXmls.put(xmlPath, Files.readLines(xml, StandardCharsets.UTF_8).size());
            } catch (IOException e) {
                log.error("xml={}", xml, e);
            }
        }
        log.info("所有xml代码总行数：{}{}", valuesSum(allXmls), rankListTitlePart);
        reportRankList(allXmls);

    }

    private Set<File> detectXmls(AstForest astForest) {
        Set<File> result = FileTraverseUtils.listFilesRecursively(
                MavenPathResolver.findMavenModule(astForest.getPrimaryClass()).resolve("src/main/resources"), "xml");
        File directory = MavenPathResolver.findMavenModule(astForest.getPrimaryClass()).resolve("src/main/resources")
                .toFile();
        if (directory.exists()) {
            result = FileTraverseUtils.listFilesRecursively(directory.toPath(), "xml");
        }
        return result;
    }

    private int valuesSum(Map<String, Integer> allXmls) {
        return allXmls.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void reportRankList(Map<String, Integer> lineCounts) {
        // 显示阈值
        lineCounts.values().removeIf(count -> count < config.getDisplayThreshold());

        // 用户不需要打印排行榜
        int rankListSize = config.getRankListSize();
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
            boolean danger = entry.getValue() >= config.getDangerThreshold();
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

    private Path calcCommonPath(Set<File> sourceRootPaths) {
        List<File> paths = Lists.newArrayList(sourceRootPaths);
        String common = paths.get(0).toString();
        for (File path : paths) {
            common = Strings.commonPrefix(common, path.getPath());
        }
        return Paths.get(common);
    }

}