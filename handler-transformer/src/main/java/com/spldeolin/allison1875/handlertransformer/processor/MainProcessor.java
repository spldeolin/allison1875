package com.spldeolin.allison1875.handlertransformer.processor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocTags;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.meta.DtoMetaInfo;
import com.spldeolin.allison1875.handlertransformer.meta.HandlerMetaInfo;
import com.spldeolin.allison1875.handlertransformer.strategy.DefaultStrategyAggregation;
import com.spldeolin.allison1875.handlertransformer.strategy.HandlerStrategy;
import com.spldeolin.allison1875.handlertransformer.strategy.PackageStrategy;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-22
 */
@Accessors(chain = true)
public class MainProcessor {

    @Setter
    private HandlerStrategy handlerStrategy = new DefaultStrategyAggregation();

    @Setter
    private PackageStrategy packageStrategy = new DefaultStrategyAggregation();

    {
        String handler = "getUser", service = "UserService", author = "Deolin 2020";
        String desc = "获取用户信息";
        {
            Long userId;
        }
        {
            String userNames[], nickName;
            Integer age, no;
            {
                String dtos = "address";
                String province, city;
                Integer areaCode;
            }
            {
                String dto = "id";
                Long id;
                {
                    String dto$ = "name";
                    String name;
                }
            }
        }
    }

    public void process() {
        AstForest forest = AstForest.getInstance();
        Collection<CompilationUnit> cus = Sets.newHashSet();

        Collection<String> serviceNames = Lists.newArrayList();
        Collection<HandlerMetaInfo> metaInfos = Lists.newArrayList();

        for (CompilationUnit cu : forest) {
            for (Pair<ClassOrInterfaceDeclaration, InitializerDeclaration> controllerAndInit :
                    new ControllerInitDecCollectProcessor(
                    cu).collect().getResult()) {
                InitializerDeclarationAnalyzeProcessor analyzeProcessor = new InitializerDeclarationAnalyzeProcessor(
                        controllerAndInit.getLeft(), packageStrategy);
                HandlerMetaInfo metaInfo = analyzeProcessor.analyze(controllerAndInit.getRight());
                serviceNames.add(metaInfo.getServiceName());
                metaInfos.add(metaInfo);
                cus.addAll(generateDtos(cu, metaInfo.getDtos()));
                cus.add(generateHandler(metaInfo));
            }
        }

        ServiceFindProcessor serviceFindProcessor = new ServiceFindProcessor(forest.reset(), serviceNames);
        serviceFindProcessor.findAll();

        for (HandlerMetaInfo metaInfo : metaInfos) {
            String serviceName = metaInfo.getServiceName();
            if (StringUtils.isBlank(serviceName)) {
                break;
            }
            ClassOrInterfaceDeclaration service = serviceFindProcessor.getService(serviceName);
            Collection<TypeDeclaration<?>> serviceImpls = serviceFindProcessor.getServiceImpl(serviceName);

            // 找不到serivce时，创建service与serviceImpl
            if (service == null) {
                Collection<ImportDeclaration> imports = metaInfo.getImports().stream()
                        .map(one -> new ImportDeclaration(one, false, false)).collect(Collectors.toList());
                CuCreator cuCreator = new CuCreator(metaInfo.getSourceRoot(),
                        packageStrategy.calcServicePackage(metaInfo.getControllerPackage()), imports, () -> {
                    ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                    coid.setInterface(true);
                    coid.setName(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "Service");
                    return coid;
                });
                cus.add(cuCreator.getCu());
                service = cuCreator.getPt().asClassOrInterfaceDeclaration();

                ArrayList<ImportDeclaration> imports4Impl = Lists.newArrayList(imports);
                imports4Impl.add(new ImportDeclaration(cuCreator.getPrimaryTypeQualifier(), false, false));
                final String serviceName4Impl = service.getNameAsString();
                cuCreator = new CuCreator(metaInfo.getSourceRoot(),
                        packageStrategy.calcServiceImplPackage(metaInfo.getControllerPackage()), imports4Impl, () -> {
                    ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                    coid.setInterface(false);
                    coid.setName(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "ServiceImpl");
                    coid.addImplementedType(serviceName4Impl);
                    return coid;
                });
                cus.add(cuCreator.getCu());
                serviceImpls.add(cuCreator.getPt().asClassOrInterfaceDeclaration());
            } else {
                cus.add(service.findCompilationUnit().orElseThrow(CuAbsentException::new));
                for (TypeDeclaration<?> serviceImpl : serviceImpls) {
                    cus.add(serviceImpl.findCompilationUnit().orElseThrow(CuAbsentException::new));
                }
            }
            // 为service和serviceImpls分别追加方法
            MethodDeclaration method = new MethodDeclaration();
            Javadoc javadoc = new JavadocComment(metaInfo.getHandlerDescription()).parse();
            String author = metaInfo.getAuthor();
            if (StringUtils.isNotBlank(author)) {
                boolean noneMatch = JavadocTags.getEveryLineByTag(metaInfo.getController(), JavadocBlockTag.Type.AUTHOR)
                        .stream().noneMatch(line -> line.contains(author));
                if (noneMatch) {
                    javadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, author));
                }
            }
            method.setJavadocComment(javadoc);
            method.setType(handlerStrategy.resolveReturnType(metaInfo));
            method.setName(metaInfo.getHandlerName());
            service.addMember(method);

            MethodDeclaration methodImpl = method.clone();
            methodImpl.addAnnotation(StaticJavaParser.parseAnnotation("@Override"));
            methodImpl.setBody(new BlockStmt().addStatement(StaticJavaParser.parseStatement("return null;")));
            serviceImpls.forEach(one -> one.addMember(methodImpl));

            // handlerMetaInfo.imports中追加service的qualifier
            metaInfo.getImports().add(service.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
            for (TypeDeclaration<?> serviceImpl : serviceImpls) {
                metaInfo.getImports()
                        .add(serviceImpl.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
            }

            // metaInfo.autowiredServiceFields追加service
            String serviceVariableName = StringUtils.lowerFirstLetter(serviceName);
            metaInfo.setAutowiredServiceField(serviceName + " " + serviceVariableName);

            // metaInfo.callServiceExpr
            metaInfo.setCallServiceExpr(String.format("%s.%s(%s);", serviceVariableName, metaInfo.getHandlerName(),
                    metaInfo.getReqBodyDto().dtoName()));

            // TODO HandlerStrategy的实现中兼容callServiceExpr


        }

        Saves.prettySave(cus);
    }

    private CompilationUnit generateHandler(HandlerMetaInfo metaInfo) {
        ClassOrInterfaceDeclaration controller = metaInfo.getController();
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        cu.addImport(metaInfo.getReqBodyDto().typeQualifier());
        cu.addImport(metaInfo.getRespBodyDto().typeQualifier());
        MethodDeclaration handler = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(metaInfo.getHandlerDescription()).parse();
        String author = metaInfo.getAuthor();
        if (StringUtils.isNotBlank(author)) {
            boolean noneMatch = JavadocTags.getEveryLineByTag(controller, JavadocBlockTag.Type.AUTHOR).stream()
                    .noneMatch(line -> line.contains(author));
            if (noneMatch) {
                javadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, author));
            }
        }
        handler.setJavadocComment(javadoc);
        String handlerName = metaInfo.getHandlerName();
        handler.addAnnotation(StaticJavaParser.parseAnnotation("@PostMapping(\"/" + handlerName + "\")"));
        handler.setPublic(true);
        handler.setType(handlerStrategy.resolveReturnType(metaInfo));
        handler.setName(handlerName);
        Parameter requestBody = StaticJavaParser.parseParameter(metaInfo.getReqBodyDto().asVariableDeclarator());
        requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@RequestBody"));
        handler.addParameter(requestBody);
        handler.setBody(handlerStrategy.resolveBody(metaInfo));
        controller.addMember(handler);
        return cu;
    }

    private Collection<CompilationUnit> generateDtos(CompilationUnit cu, Collection<DtoMetaInfo> dtos) {
        Collection<CompilationUnit> result = Lists.newArrayList();
        Path sourceRoot = Locations.getStorage(cu).getSourceRoot();
        for (DtoMetaInfo dto : dtos) {
            Collection<ImportDeclaration> imports = Lists.newArrayList(new ImportDeclaration("java.util", false, true),
                    new ImportDeclaration("lombok.Data", false, false),
                    new ImportDeclaration("lombok.experimental.Accessors", false, false));
            for (String anImport : dto.imports()) {
                imports.add(new ImportDeclaration(anImport, false, false));
            }
            CuCreator cuCreator = new CuCreator(sourceRoot, dto.packageName(), imports, () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Data"))
                        .addAnnotation(StaticJavaParser.parseAnnotation("@Accessors(chain = true)"));
                coid.setPublic(true).setName(dto.typeName());
                for (String vd : dto.variableDeclarators()) {
                    coid.addMember(StaticJavaParser.parseBodyDeclaration("private " + vd + ";"));
                }
                return coid;
            });
            result.add(cuCreator.create(false));
        }
        return result;
    }

}
