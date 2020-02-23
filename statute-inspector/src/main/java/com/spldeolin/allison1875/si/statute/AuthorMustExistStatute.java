package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.si.vo.LawlessVo;

/**
 * @author Deolin 2020-02-23
 */
public class AuthorMustExistStatute implements Statute {

    @Override
    public Collection<LawlessVo> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessVo> result = Lists.newArrayList();
        cus.forEach(cu -> {
            TypeDeclaration<?> type;
            Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
            if (primaryType.isPresent()) {
                type = primaryType.get();
            } else {
                NodeList<TypeDeclaration<?>> types = cu.getTypes();
                if (types.size() > 0) {
                    type = types.get(0);
                } else {
                    result.add(new LawlessVo(cu).setMessage("这个Java文件中没有任何类型声明，它的作用是什么？"));
                    return;
                }
            }

            Optional<Javadoc> javadocOpt = type.getJavadoc();
            if (!javadocOpt.isPresent()) {
                result.add(new LawlessVo(type, type.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new))
                        .setMessage("文件中第一个类型声明缺少Javadoc"));
                return;
            }

            Javadoc javadoc = javadocOpt.get();
            Optional<JavadocBlockTag> authorTag = javadoc.getBlockTags().stream()
                    .filter(tag -> Type.AUTHOR == tag.getType()).findFirst();
            if (!authorTag.isPresent()) {
                result.add(new LawlessVo(type, type.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new))
                        .setMessage("文件中第一个Javadoc中缺少@author"));
                return;
            }

            if (authorTag.get().getContent().getElements().size() == 0) {
                result.add(new LawlessVo(type, type.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new))
                        .setMessage("文件中第一个Javadoc的@author缺少内容"));
            }
        });

        return result;
    }

}
