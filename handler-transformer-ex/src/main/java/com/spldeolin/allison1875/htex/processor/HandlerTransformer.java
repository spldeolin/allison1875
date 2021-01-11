package com.spldeolin.allison1875.htex.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
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
import com.spldeolin.allison1875.base.constant.QualifierConstants;
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

    @Override
    public void process(AstForest astForest) {
        Set<CompilationUnit> toCreate = Sets.newHashSet();
        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : cu
                    .findAll(ClassOrInterfaceDeclaration.class, this::isController)) {
                ClassOrInterfaceDeclaration controllerClone = controller.clone();
                boolean transformed = false;
                for (BodyDeclaration<?> member : controller.getMembers()) {
                    if (member.isInitializerDeclaration()) {
                        InitializerDeclaration init = member.asInitializerDeclaration();
                        if (!isFirstLineCommentPresent(init)) {
                            continue;
                        }
                        BlockStmt initBody = init.getBody();
                        String firstLine = tryGetFirstLine(initBody.getStatements());
                        FirstLineDto firstLineDto = parseFirstLine(firstLine);
                        if (firstLineDto == null) {
                            continue;
                        }
                        log.info(firstLineDto);

                        ensureNoRepeation(controller, firstLineDto);

                        // 校验init下的Req和Resp类
                        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
                            throw new IllegalArgumentException(
                                    "构造代码块下最多只能有2个类声明，分别用于代表Req和Resp。[" + firstLineDto.getHandlerUrl() + "] 当前："
                                            + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                                            .map(one -> one.getClassDeclaration().getNameAsString())
                                            .collect(Collectors.joining("、")));
                        }
                        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 0) {
                            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                                if (!StringUtils
                                        .equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req",
                                                "Resp")) {
                                    throw new IllegalArgumentException(
                                            "构造代码块下类的命名只能是Req或者Resp。[" + firstLineDto.getHandlerUrl() + "] 当前："
                                                    + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                                    .collect(Collectors.joining("、")));
                                }
                            }
                        }
                        if (initBody.findAll(ClassOrInterfaceDeclaration.class,
                                coid -> coid.getNameAsString().equals("Req")).size() > 1) {
                            throw new IllegalArgumentException(
                                    "构造代码块下不能重复声明Req类。[" + firstLineDto.getHandlerUrl() + "]");
                        }
                        if (initBody.findAll(ClassOrInterfaceDeclaration.class,
                                coid -> coid.getNameAsString().equals("Resp")).size() > 1) {
                            throw new IllegalArgumentException(
                                    "构造代码块下不能重复声明Resp类。[" + firstLineDto.getHandlerUrl() + "]");
                        }

                        // 广度优先遍历收集 + 反转
                        List<ClassOrInterfaceDeclaration> dtos = Lists.newArrayList();
                        init.walk(TreeTraversal.BREADTHFIRST, node -> {
                            if (node instanceof ClassOrInterfaceDeclaration) {
                                ClassOrInterfaceDeclaration coid = (ClassOrInterfaceDeclaration) node;
                                if (!coid.isInterface()) {
                                    dtos.add(coid);
                                }
                            }
                        });
                        Collections.reverse(dtos);

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
                }
                if (transformed) {
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

    private void ensureNoRepeation(ClassOrInterfaceDeclaration controller, FirstLineDto firstLineDto) {
        String handlerName = firstLineDto.getHandlerName();
        if (controller.getMethodsByName(handlerName).size() > 0) {
            String newHandlerName = handlerName + "Ex";
            firstLineDto.setHandlerName(newHandlerName);
            firstLineDto.setHandlerUrl(firstLineDto.getHandlerUrl() + "Ex");
            log.warn(String.format("方法[%s] 在Controller[%s] 中已存在，重名名为[%s]", handlerName,
                    controller.getNameAsString(), newHandlerName));
            ensureNoRepeation(controller,firstLineDto);
        }
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

    private boolean isFirstLineCommentPresent(InitializerDeclaration init) {
        NodeList<Statement> statements = init.getBody().getStatements();
        if (CollectionUtils.isEmpty(statements)) {
            return false;
        }
        return statements.get(0).getComment().filter(Comment::isLineComment).isPresent();
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

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(QualifierConstants.CONTROLLER) || QualifierConstants.CONTROLLER
                        .equals(resolve.getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.error("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

}