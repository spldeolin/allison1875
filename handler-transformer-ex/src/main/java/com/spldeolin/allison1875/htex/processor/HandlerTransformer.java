package com.spldeolin.allison1875.htex.processor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.builder.FieldDeclarationBuilder;
import com.spldeolin.allison1875.base.builder.JavabeanCuBuilder;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.htex.HandlerTransformerConfig;
import com.spldeolin.allison1875.htex.handle.CreateHandlerHandle;
import com.spldeolin.allison1875.htex.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.htex.javabean.FirstLineDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-22
 */
@Singleton
@Log4j2
public class HandlerTransformer implements Allison1875MainProcessor {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private CreateServiceMethodHandle createServiceMethodHandle;

    @Inject
    private CreateHandlerHandle createHandlerHandle;

    @Inject
    private ControllerCollectProc controllerCollectProc;

    @Inject
    private InitBodyCollectProc initBodyCollectProc;

    @Inject
    private EnsureNoRepeationProc ensureNoRepeationProc;

    @Inject
    private ReqRespProc reqRespProc;

    @Inject
    private DtoProc dtoProc;

    @Override
    public void process(AstForest astForest) {
        Set<CompilationUnit> toCreate = Sets.newHashSet();

        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : controllerCollectProc.collect(cu)) {
                ClassOrInterfaceDeclaration controllerClone = controller.clone();

                boolean transformed = false;
                for (BlockStmt initBody : initBodyCollectProc.collect(controller)) {
                    String firstLine = tryGetFirstLine(initBody.getStatements());
                    FirstLineDto firstLineDto = parseFirstLine(firstLine);
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info(firstLineDto);

                    // 当指定的handlerName在controller中已经存在同名handler时，handlerName后拼接Ex（递归，确保不会重名）
                    ensureNoRepeationProc.ensureNoRepeation(controller, firstLineDto);

                    // 校验init下的Req和Resp类
                    reqRespProc.checkInitBody(initBody, firstLineDto);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtos = dtoProc.collectDtosFromBottomToTop(initBody);

                    String reqDtoQualifier = null;
                    String respDtoQualifier = null;
                    String paramType = null;
                    String resultType = null;
                    Collection<JavabeanCuBuilder> builders = Lists.newArrayList();
                    Collection<String> dtoQualifiers = Lists.newArrayList();
                    // 生成ReqDto、RespDto、NestDto
                    for (ClassOrInterfaceDeclaration dto : dtos) {
                        JavabeanCuBuilder builder = new JavabeanCuBuilder();
                        builder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
                        boolean isReq = dto.getNameAsString().equals("Req");
                        boolean isResp = dto.getNameAsString().equals("Resp");
                        boolean isInReq = dto.findAncestor(ClassOrInterfaceDeclaration.class,
                                ancestor -> ancestor.getNameAsString().equals("Req")).isPresent();
                        boolean isInResp = dto.findAncestor(ClassOrInterfaceDeclaration.class,
                                ancestor -> ancestor.getNameAsString().equals("Resp")).isPresent();

                        if (isReq || isResp) {
                            dto.setName(MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + dto
                                    .getNameAsString());
                        }

                        // 计算每一个dto的package
                        String pkg;
                        if (isReq) {
                            pkg = handlerTransformerConfig.getReqDtoPackage();
                        } else if (isResp) {
                            pkg = handlerTransformerConfig.getRespDtoPackage();
                        } else if (isInReq) {
                            pkg = handlerTransformerConfig.getReqDtoPackage() + ".dto";
                        } else if (isInResp) {
                            pkg = handlerTransformerConfig.getRespDtoPackage() + ".dto";
                        } else {
                            throw new RuntimeException("impossible unless bug.");
                        }
                        builder.packageDeclaration(pkg);
                        builder.importDeclarations(cu.getImports());
                        builder.importDeclarationsString(
                                Lists.newArrayList("javax.validation.Valid", "java.util.Collection",
                                        handlerTransformerConfig.getPageTypeQualifier()));
                        ClassOrInterfaceDeclaration clone = dto.clone();
                        clone.setPublic(true).getFields().forEach(field -> field.setPrivate(true));
                        clone.getAnnotations().removeIf(annotationExpr -> StringUtils
                                .equalsAnyIgnoreCase(annotationExpr.getNameAsString(), "l", "p"));
                        builder.coid(clone.setPublic(true));
                        if (isReq) {
                            paramType = calcType(dto);
                            reqDtoQualifier = pkg + "." + clone.getNameAsString();
                        }
                        if (isResp) {
                            resultType = calcType(dto);
                            respDtoQualifier = pkg + "." + clone.getNameAsString();
                        }
                        builders.add(builder);
                        dtoQualifiers.add(pkg + "." + clone.getNameAsString());

                        // 遍历到NestDto时，将父节点中的自身替换为Field
                        if (dto.getParentNode().filter(parent -> parent instanceof ClassOrInterfaceDeclaration)
                                .isPresent()) {
                            ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto
                                    .getParentNode().get();
                            FieldDeclarationBuilder fieldBuilder = new FieldDeclarationBuilder();
                            dto.getJavadoc().ifPresent(fieldBuilder::javadoc);
                            fieldBuilder.annotationExpr("@Valid");
                            fieldBuilder.type(calcType(dto));
                            fieldBuilder.fieldName(MoreStringUtils.upperCamelToLowerCamel(dto.getNameAsString()));
                            parentCoid.replace(dto, fieldBuilder.build());
                        }
                    }
                    for (JavabeanCuBuilder builder : builders) {
                        builder.importDeclarationsString(dtoQualifiers);
                        toCreate.add(builder.build());
                    }

                    // 生成Service
                    SingleMethodServiceCuBuilder serviceBuilder = new SingleMethodServiceCuBuilder();
                    serviceBuilder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
                    serviceBuilder.servicePackageDeclaration(handlerTransformerConfig.getServicePackage());
                    serviceBuilder.implPackageDeclaration(handlerTransformerConfig.getServiceImplPackage());
                    serviceBuilder.importDeclarations(cu.getImports());
                    List<String> imports = Lists
                            .newArrayList("java.util.Collection", handlerTransformerConfig.getPageTypeQualifier());
                    if (reqDtoQualifier != null) {
                        imports.add(reqDtoQualifier);
                    }
                    if (respDtoQualifier != null) {
                        imports.add(respDtoQualifier);
                    }
                    serviceBuilder.importDeclarationsString(imports);
                    serviceBuilder.serviceName(
                            MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "Service");
                    serviceBuilder.method(createServiceMethodHandle
                            .createMethodImpl(firstLineDto, paramType, resultType));
                    toCreate.add(serviceBuilder.buildService());
                    toCreate.add(serviceBuilder.buildServiceImpl());

                    // Controller中的InitBlock转化为handler方法
                    MethodDeclaration handler = createHandlerHandle
                            .createHandler(firstLineDto, paramType, resultType, serviceBuilder);

                    // 确保存在 @Autowired private Service service;
                    if (!controller.getFieldByName(serviceBuilder.getServiceVarName()).isPresent()) {
                        FieldDeclarationBuilder serviceField = new FieldDeclarationBuilder();
                        serviceField.annotationExpr("@Autowired");
                        serviceField.type(serviceBuilder.getService().getNameAsString());
                        serviceField.fieldName(serviceBuilder.getServiceVarName());
                        controllerClone.addMember(serviceField.build());
                    }
                    controllerClone.addMember(handler);
                    transformed = true;
                    if (reqDtoQualifier != null) {
                        Imports.ensureImported(cu, reqDtoQualifier);
                    }
                    if (respDtoQualifier != null) {
                        Imports.ensureImported(cu, respDtoQualifier);
                    }
                    Imports.ensureImported(cu, serviceBuilder.getJavabeanQualifier());
                }
                if (transformed) {
                    Imports.ensureImported(cu, handlerTransformerConfig.getPageTypeQualifier());
                    Imports.ensureImported(cu, AnnotationConstant.REQUEST_BODY_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.VALID_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.POST_MAPPING_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.AUTOWIRED_QUALIFIER);
                    controller.replace(controllerClone);
                    toCreate.add(cu);
                }
            }
        }
        toCreate.forEach(Saves::save);
    }

    private String calcType(ClassOrInterfaceDeclaration dto) {
        if (dto.getAnnotationByName("L").isPresent()) {
            return "Collection<" + dto.getNameAsString() + ">";
        }
        if (dto.getAnnotationByName("P").isPresent()) {
            String[] split = handlerTransformerConfig.getPageTypeQualifier().split("\\.");
            return split[split.length - 1] + "<" + dto.getNameAsString() + ">";
        }
        return dto.getNameAsString();
    }

    private String tryGetFirstLine(NodeList<Statement> statements) {
        if (CollectionUtils.isEmpty(statements)) {
            return null;
        }
        Statement first = statements.get(0);
        if (!first.getComment().filter(Comment::isLineComment).isPresent()) {
            return null;
        }
        String firstLineContent = first.getComment().get().asLineComment().getContent();
        return firstLineContent;
    }

    private FirstLineDto parseFirstLine(String firstLineContent) {
        if (StringUtils.isBlank(firstLineContent)) {
            return null;
        }
        firstLineContent = firstLineContent.trim();
        // extract to processor
        String[] parts = firstLineContent.split(" ");
        if (parts.length != 2) {
            return null;
        }

        FirstLineDto firstLineDto = new FirstLineDto();
        firstLineDto.setHandlerUrl(parts[0]);
        firstLineDto.setHandlerName(MoreStringUtils.slashToLowerCamel(parts[0]));
        firstLineDto.setHandlerDescription(parts[1]);
        firstLineDto.setMore(null); // privode handle
        return firstLineDto;
    }

}