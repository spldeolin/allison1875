package com.spldeolin.allison1875.satisficing.docanalyzer;

import java.util.Optional;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import com.spldeolin.allison1875.docanalyzer.service.impl.ResponseBodyServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-25
 */
@Slf4j
public class ResponseBodyServiceImpl2 extends ResponseBodyServiceImpl {

    @Override
    protected ResolvedType getConcernedResponseBodyType(MethodDeclaration mvcHandlerMd) {
        Type type = mvcHandlerMd.getType();
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType coit = type.asClassOrInterfaceType();
            if (coit.getNameAsString().equals("RequestResult")) {
                Optional<NodeList<Type>> typeArguments = coit.getTypeArguments();
                if (typeArguments.isPresent()) {
                    NodeList<Type> types = typeArguments.get();
                    if (types.size() == 1) {
                        Type typeArgument = types.get(0);
                        if (typeArgument.isClassOrInterfaceType()) {
                            if (typeArgument.toString().equals("Void")) {
                                return null;
                            }
                            if (!typeArgument.toString().equals("Object")) {
                                try {
                                    return typeArgument.resolve();
                                } catch (Exception e) {
                                    log.warn("fail to resolve, type={} returnType={}", typeArgument, type, e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.getConcernedResponseBodyType(mvcHandlerMd);
    }

}