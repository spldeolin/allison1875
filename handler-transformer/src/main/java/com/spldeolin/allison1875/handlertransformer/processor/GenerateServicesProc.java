package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;
import lombok.Getter;

/**
 * @author Deolin 2020-08-26
 */
class GenerateServicesProc {

    private final MetaInfo metaInfo;

    @Getter
    private CompilationUnit serviceCu;

    @Getter
    private CompilationUnit serviceImplCu;

    @Getter
    private String serviceQualifier;

    GenerateServicesProc(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    GenerateServicesProc process() {
        String serviceName = StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "Service";
        MethodDeclaration absMethod = new MethodDeclaration();
        if (CollectionUtils.isEmpty(metaInfo.getRespBody().getVariableDeclarators())) {
            absMethod.setType("void");
        } else {
            absMethod.setType(metaInfo.getRespBody().getTypeName());
        }
        absMethod.setName(metaInfo.getHandlerName());
        if (CollectionUtils.isNotEmpty(metaInfo.getReqBody().getVariableDeclarators())) {
            absMethod.addParameter(StaticJavaParser.parseType(metaInfo.getReqBody().getTypeName()), "req");
        }
        MethodDeclaration method = absMethod.clone();

        List<String> imports = Lists.newArrayList();
        imports.add(metaInfo.getReqBody().getTypeQualifier());
        imports.add(metaInfo.getRespBody().getTypeQualifier());
        CuCreator serviceCreator = new CuCreator(metaInfo.getSourceRoot(),
                HandlerTransformerConfig.getInstance().getServicePackage(), imports, () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            coid.setPublic(true).setInterface(true).setName(serviceName);
            MethodDeclaration decl = absMethod.setBody(null);
            coid.addMember(decl);
            return coid;
        });
        serviceCu = serviceCreator.create(false);
        serviceQualifier = serviceCreator.getPrimaryTypeQualifier();

        List<String> imports4Impl = Lists.newArrayList(imports);
        imports4Impl.add(serviceCreator.getPrimaryTypeQualifier());
        imports4Impl.add("org.springframework.stereotype.Service");
        CuCreator serviceImplCreator = new CuCreator(metaInfo.getSourceRoot(),
                HandlerTransformerConfig.getInstance().getServiceImplPackage(), imports4Impl, () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            coid.addAnnotation(StaticJavaParser.parseAnnotation("@Service"));
            coid.setPublic(true).setInterface(false).setName(serviceName + "Impl");
            coid.addImplementedType(serviceName);

            method.addAnnotation(StaticJavaParser.parseAnnotation("@Override"));
            method.setPublic(true);

            NodeList<Statement> stmts = new NodeList<>();
            if (CollectionUtils.isNotEmpty(metaInfo.getRespBody().getVariableDeclarators())) {
                String newWhat = method.getTypeAsString();
                if (method.getType().isClassOrInterfaceType() && method.getType().asClassOrInterfaceType()
                        .getTypeArguments().isPresent()) {
                    newWhat += "<>";
                }
                stmts.add(StaticJavaParser.parseStatement("return new " + newWhat + "();"));
            }
            method.setBody(new BlockStmt(stmts));
            coid.addMember(method);
            return coid;
        });
        serviceImplCu = serviceImplCreator.create(false);

        return this;
    }

}