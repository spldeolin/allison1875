package com.spldeolin.allison1875.handlertransformer.processor;

import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.meta.DtoMetaInfo;
import com.spldeolin.allison1875.handlertransformer.meta.MetaInfo;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-22
 */
@Accessors(chain = true)
public class MainProc {

    public void process() {
        Collection<CompilationUnit> cus = Sets.newHashSet();

        for (CompilationUnit cu : AstForest.getInstance()) {
            for (Pair<ClassOrInterfaceDeclaration, InitializerDeclaration> pair : new BlueprintCollectProc(cu).process()
                    .getControllerAndBlueprints()) {

                ClassOrInterfaceDeclaration controller = pair.getLeft();
                InitializerDeclaration blueprint = pair.getRight();

                BlueprintAnalyzeProc blueprintAnalyzeProc = new BlueprintAnalyzeProc(controller, blueprint).process();
                MetaInfo metaInfo = blueprintAnalyzeProc.getMetaInfo();

                cus.addAll(generateDtos(cu, metaInfo.getDtos(), Imports.listImports(controller)));
                GenerateServicesProc generateServicesProc = new GenerateServicesProc(metaInfo).process();
                cus.add(generateServicesProc.getServiceCu());
                cus.add(generateServicesProc.getServiceImplCu());
                cus.add(generateHandler(metaInfo, generateServicesProc.getServiceQualifier()));
            }
        }

        Saves.prettySave(cus);
    }

    private CompilationUnit generateHandler(MetaInfo metaInfo, String serviceQualifier) {
        ClassOrInterfaceDeclaration controller = metaInfo.getController();
        Imports.ensureImported(controller, metaInfo.getReqBody().typeQualifier());
        Imports.ensureImported(controller, metaInfo.getRespBody().typeQualifier());
        Imports.ensureImported(controller, serviceQualifier);
        for (String controllerImport : HandlerTransformerConfig.getInstance().getControllerImports()) {
            Imports.ensureImported(controller, controllerImport);
        }
        FieldDeclaration field = controller
                .addField(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "Service",
                        metaInfo.getHandlerName() + "Service");
        field.addAnnotation(StaticJavaParser.parseAnnotation("@Autowired"));
        field.setPrivate(true);
        MethodDeclaration handler = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(metaInfo.getHandlerDescription()).parse();
        handler.setJavadocComment(javadoc);
        String handlerName = metaInfo.getHandlerName();
        handler.addAnnotation(StaticJavaParser.parseAnnotation("@PostMapping(\"/" + handlerName + "\")"));
        for (String handlerAnnotation : HandlerTransformerConfig.getInstance().getHandlerAnnotations()) {
            handler.addAnnotation(StaticJavaParser.parseAnnotation("@" + handlerAnnotation));
        }
        handler.setPublic(true);
        handler.setType(
                String.format(HandlerTransformerConfig.getInstance().getResult(), metaInfo.getRespBody().typeName()));
        handler.setName(handlerName);
        Parameter requestBody = StaticJavaParser.parseParameter(metaInfo.getReqBody().typeName() + " req");
        requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@RequestBody"));
        requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@Valid"));
        handler.addParameter(requestBody);
        BlockStmt body = new BlockStmt();
        String serviceCallExpr = metaInfo.getHandlerName() + "Service." + metaInfo.getHandlerName() + "(req)";
        String returnStatement = String
                .format(HandlerTransformerConfig.getInstance().getReturnWrappedResult(), serviceCallExpr);
        body.addStatement(StaticJavaParser.parseStatement(returnStatement));
        handler.setBody(body);
        controller.addMember(handler);
        return controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
    }

    private Collection<CompilationUnit> generateDtos(CompilationUnit cu, Collection<DtoMetaInfo> dtos,
            Collection<ImportDeclaration> importsFromController) {
        Collection<CompilationUnit> result = Lists.newArrayList();
        Path sourceRoot = Locations.getStorage(cu).getSourceRoot();
        for (DtoMetaInfo dto : dtos) {

            Collection<ImportDeclaration> imports = Lists.newArrayList(importsFromController);
            imports.add(new ImportDeclaration("lombok.Data", false, false));
            imports.add(new ImportDeclaration("lombok.experimental.Accessors", false, false));
            for (String anImport : dto.imports()) {
                imports.add(new ImportDeclaration(anImport, false, false));
            }
            CuCreator cuCreator = new CuCreator(sourceRoot, dto.packageName(), imports, () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Data"))
                        .addAnnotation(StaticJavaParser.parseAnnotation("@Accessors(chain = true)"));
                coid.setPublic(true).setName(dto.typeName());
                for (Pair<String, String> pair : dto.variableDeclarators()) {
                    FieldDeclaration field = StaticJavaParser.parseBodyDeclaration(pair.getRight() + ";")
                            .asFieldDeclaration();
                    field.setPrivate(true);
                    if (pair.getLeft() != null) {
                        field.setJavadocComment(pair.getLeft());
                    }
                    coid.addMember(field);
                }
                return coid;
            });
            result.add(cuCreator.create(false));
        }
        return result;
    }

}
