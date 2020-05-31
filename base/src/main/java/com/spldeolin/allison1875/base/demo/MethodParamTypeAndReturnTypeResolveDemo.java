package com.spldeolin.allison1875.base.demo;

import java.util.Collection;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 对方法的参数和返回值进行resolve，然后然后进行类加载的示例
 * （这是doc-analyzer中最重要也最容易出现没有考虑到异常的一部）
 * <pre>
 * 几个会导致type.resolve()抛出RuntimeException的情况
 * 1. 声明Type所在的java文件未被编译时，会提示Unsolved symbol :，靠源码工具无法解决这个种问题，需要目标项目充分编译
 * 2. javax的类型也会提示Unsolved symbol，应该需要加具体的实现依赖
 * 3. 碰到泛型类型时会提示Unable to get the resolved type of class ResolvedReferenceType from T，这个源码工具在catch内可以分辨这种情况
 * </pre>
 *
 * @author Deolin 2020-05-30
 */
@Log4j2
public class MethodParamTypeAndReturnTypeResolveDemo {

    private static final AstForest astFotest = AstForest.getInstance();

    public static void main(String[] args) {
        for (CompilationUnit cu : astFotest) {
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                for (Parameter parameter : method.getParameters()) {
                    Type type = parameter.getType();
                    try {
                        ResolvedType resolve = type.resolve();
                        if (resolve.isReferenceType()) {
                            String id = resolve.asReferenceType().getId();
                            String describe = resolve.describe();
                            String qualifiedName = resolve.asReferenceType().getQualifiedName();
//                            if (!id.equals(describe) || !describe.equals(qualifiedName)) {
//                                log.info("id={}", id); // 会考虑方法中等声明的局部类，不带泛型
//                                log.info("describe={}", describe); // 会考虑匿名类，附带泛型
//                                log.info("qualifiedName={}", qualifiedName);
//                            }

                            for (String classQualifier : listAllTypeName(resolve.asReferenceType())) {
                                try {
                                    LoadClassUtils.loadClass(classQualifier, astFotest.getCurrentClassLoader());
                                } catch (Exception ignored) {
                                }
                            }
                        }

                    } catch (Exception e) {
                        boolean isTypeParameter = isTypeParameter(method, type);

                        if (!isTypeParameter) {
                            log.error(e.getMessage() + "    " + method.getNameAsString());
                        }
                    }
                }

                Type type = method.getType();
                try {
                    ResolvedType resolve = type.resolve();
                    if (resolve.isReferenceType()) {

                        for (String classQualifier : listAllTypeName(resolve.asReferenceType())) {
                            try {
                                LoadClassUtils.loadClass(classQualifier, astFotest.getCurrentClassLoader());
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } catch (Exception e) {
                    boolean isTypeParameter = isTypeParameter(method, type);

                    if (!isTypeParameter) {
                        log.error(e.getMessage() + "    " + method.getNameAsString());
                    }
                }
            });
        }
    }

    private static boolean isTypeParameter(MethodDeclaration method, Type type) {
        boolean typeIsTypeParameter = false;
        Optional<ClassOrInterfaceDeclaration> ancestor = method.findAncestor(ClassOrInterfaceDeclaration.class);
        if (ancestor.isPresent()) {
            NodeList<TypeParameter> typeParameters = ancestor.get().getTypeParameters();
            for (TypeParameter typeParameter : typeParameters) {
                if (typeParameter.getNameAsString().equals(type.toString())) {
                    typeIsTypeParameter = true;
                }
            }
        }
        for (TypeParameter typeParameter : method.getTypeParameters()) {
            if (typeParameter.getNameAsString().equals(type.toString())) {
                typeIsTypeParameter = true;
            }
        }
        return typeIsTypeParameter;
    }

    private static Collection<String> listAllTypeName(ResolvedReferenceType rrt) {
        Collection<String> result = Lists.newArrayList();
        result.add(rrt.getQualifiedName());
        for (Pair<ResolvedTypeParameterDeclaration, ResolvedType> pair : rrt.getTypeParametersMap()) {
            result.add(pair.b.describe());
        }
        return result;
    }

}
