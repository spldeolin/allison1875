package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-27
 */
public class MybatisWrapperSelectStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class,
                coid -> coid.getNameAsString().endsWith("ServiceImpl")).forEach(serviceImpl -> {
            serviceImpl.findAll(MethodCallExpr.class).forEach(mce -> mce.getScope().ifPresent(scope -> {

                // calculateResolvedType的性能开销比较大

//                ResolvedType rt;
//                try {
//                    rt = scope.calculateResolvedType();
//                } catch (Exception ignored) {
//                    return;
//                }
//
//                if (!rt.isReferenceType()) {
//                    return;
//                }
//                if (rt.asReferenceType().getAllAncestors().stream()
//                        .anyMatch(ancestor -> QualifierConstants.MYBATIS_PLUS_BASE_MAPPER.equals(ancestor.getId()))) {
                if (StringUtils
                        .equalsAny(mce.getNameAsString(), "delete", "update", "selectCount", "selectList", "selectMaps",
                                "selectObjs", "selectPage", "selectMapsPage")) {
                    result.add(new LawlessDto(mce).setMessage("ServiceImpl中禁止调用BaseMapper中需要Wrapper对象的方法" + mce));
                }
//                }
            }));
        }));

        return result;
    }

}
