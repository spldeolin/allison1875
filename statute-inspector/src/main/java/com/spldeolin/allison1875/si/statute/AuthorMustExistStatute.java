package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-23
 */
public class AuthorMustExistStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newArrayList();
        cus.forEach(cu -> {
            NodeList<TypeDeclaration<?>> types = cu.getTypes();
            if (types.size() == 0) {
                result.add(new LawlessDto(cu).setMessage("这个Java文件中没有任何类型声明，它是多余的吗？"));
                return;
            }
            types.forEach(type -> {
                if (Authors.isAuthorAbsent(type)) {
                    result.add(new LawlessDto(cu).setMessage("没有作者信息"));
                }
            });
        });

        return result;
    }

}
