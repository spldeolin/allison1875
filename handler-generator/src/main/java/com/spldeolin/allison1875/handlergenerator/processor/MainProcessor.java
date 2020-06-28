package com.spldeolin.allison1875.handlergenerator.processor;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.PackageAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocTags;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlergenerator.meta.DtoMetaInfo;
import com.spldeolin.allison1875.handlergenerator.meta.HandlerMetaInfo;
import com.spldeolin.allison1875.handlergenerator.strategy.DefaultStrategyAggregation;
import com.spldeolin.allison1875.handlergenerator.strategy.HandlerStrategy;
import com.spldeolin.allison1875.handlergenerator.strategy.PackageStrategy;
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
        Collection<CompilationUnit> cus = Lists.newArrayList();

        Collection<String> serviceNames = Lists.newArrayList();
        Collection<HandlerMetaInfo> metaInfos = Lists.newArrayList();
        ControllerInitDecIterateProcessor iterateProcessor = new ControllerInitDecIterateProcessor(forest);
        iterateProcessor.iterate((cu, controller, init) -> {
            String controllerPackage = cu.getPackageDeclaration().orElseThrow(PackageAbsentException::new)
                    .getNameAsString();

            InitializerDeclarationAnalyzeProcessor analyzeProcessor = new InitializerDeclarationAnalyzeProcessor(
                    controllerPackage, packageStrategy);
            HandlerMetaInfo metaInfo = analyzeProcessor.analyze(init);
            metaInfo.controller(controller);
            serviceNames.add(metaInfo.serviceName());
            metaInfos.add(metaInfo);

            cus.addAll(generateDtos(cu, metaInfo.dtos()));
        });

        ServiceFindProcessor serviceFindProcessor = new ServiceFindProcessor(forest.reset(), serviceNames);
        serviceFindProcessor.findAll();

        for (HandlerMetaInfo metaInfo : metaInfos) {
            String serviceName = metaInfo.serviceName();
            ClassOrInterfaceDeclaration service = serviceFindProcessor.getService(serviceName);
            Collection<TypeDeclaration<?>> serviceImpls = serviceFindProcessor.getServiceImpl(serviceName);
            if (service == null) {
                // TODO 新建interface service 与 class serviceImpl
                // service = new Coid();
                // serviceImpls = Lists.newArrayList(new Coid());
            }
            // TODO 为service和serviceImpls分别追加方法

            // TODO handlerMetaInfo.imports中追加service的qualifier

            // TODO handlerMetaInfo.autowiredServiceFields追加service

            // TODO handlerMetaInfo.callServiceExpr

            // TODO HandlerStrategy的实现中兼容callServiceExpr

            cus.add(generateHandler(metaInfo.controller(), metaInfo));
        }

        Saves.prettySave(cus);
    }

    private CompilationUnit generateHandler(ClassOrInterfaceDeclaration controller, HandlerMetaInfo metaInfo) {
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        cu.addImport(metaInfo.reqBodyDto().typeQualifier());
        cu.addImport(metaInfo.respBodyDto().typeQualifier());
        MethodDeclaration handler = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(metaInfo.handlerDescription()).parse();
        if (StringUtils.isNotBlank(metaInfo.author())) {
            boolean noneMatch = JavadocTags.getEveryLineByTag(controller, JavadocBlockTag.Type.AUTHOR).stream()
                    .noneMatch(line -> line.contains(metaInfo.author()));
            if (noneMatch) {
                javadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, metaInfo.author()));
            }
        }
        handler.setJavadocComment(javadoc);
        handler.addAnnotation(
                StaticJavaParser.parseAnnotation("@PostMapping(value=\"/" + metaInfo.handlerName() + "\")"));
        handler.setPublic(true);
        handler.setType(handlerStrategy.resolveReturnType(metaInfo));
        handler.setName(metaInfo.handlerName());
        Parameter requestBody = StaticJavaParser.parseParameter(metaInfo.reqBodyDto().asVariableDeclarator());
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
