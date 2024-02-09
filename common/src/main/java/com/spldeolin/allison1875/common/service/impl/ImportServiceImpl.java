package com.spldeolin.allison1875.common.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.ImportService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-09
 */
@Singleton
@Slf4j
public class ImportServiceImpl implements ImportService {

    /**
     * 将参数cu中的所有全限定类型抽取成import声明
     *
     * <pre>
     * ClassOrInterfaceType.scope的含义是模棱两可的，
     * JavaParser不知道scope是什么——可能是Map.Entry的Map部分，也可能是java.util.List的java.util部分，
     * 遇到前者的情况时，调用这个方法时，应当排除这部分
     * </pre>
     *
     * @param cu 本方法不会改变这个
     * @return 抽取import完毕后的CU对象
     * @see ClassOrInterfaceType
     */
    @Override
    public CompilationUnit extractQualifiedTypeToImport(CompilationUnit cu) {
        for (ClassOrInterfaceType type : cu.findAll(ClassOrInterfaceType.class, type -> {
            // 防止scope的scope被加入到import，所以只取用name首字母大写的type
            return MoreStringUtils.isFirstLetterUpperCase(type.getNameAsString());
        })) {
            type.getScope().ifPresent(scope -> {
                // 采用判断scope是否为全数字的方式，忽略Map.Entry这样的scope情况
                if (!StringUtils.isAllLowerCase(scope.toString().replace(".", ""))) {
                    return;
                }
                log.info("Qualified Type '{}' in '{}' extract to Import", type,
                        CompilationUnitUtils.getCuAbsolutePath(cu));
                cu.addImport(type.toString());
                type.replace(StaticJavaParser.parseType(type.getNameAsString()));

            });
        }
        return cu;
    }

    /**
     * 将参数cu中的所有全限定类型抽取成import声明
     */
    public List<CompilationUnit> extractQualifiedTypeToImport(List<CompilationUnit> cus) {
        return cus.stream().map(this::extractQualifiedTypeToImport).collect(Collectors.toList());
    }

}