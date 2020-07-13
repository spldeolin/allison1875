package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Log4j2
public class MainProcessor {

    public static void main(String[] args) {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();
        Collection<CompilationUnit> cus = Lists.newArrayList();

        InformationSchemaQueryProcessor isqp = new InformationSchemaQueryProcessor().process();
        Collection<InformationSchemaDto> infoSchemas = isqp.getColumns();

        PersistenceInfoBuildProcessor pibp = new PersistenceInfoBuildProcessor(infoSchemas).process();
        Collection<PersistenceDto> persistences = pibp.getPersistences();

        for (PersistenceDto persistence : persistences) {
            Path entityPath = CodeGenerationUtils
                    .fileInPackageAbsolutePath(conf.getSourceRoot(), conf.getEntityPackage(),
                            persistence.getEntityName() + ".java");
            if (entityPath.toFile().exists()) {
                log.info("Entity file exist, overwrite. [{}]", entityPath);
            }

            // 覆盖生成Entity
            CuCreator entityCuCreator = new CuCreator(Paths.get(conf.getSourceRoot()), conf.getEntityPackage(),
                    Lists.newArrayList("java.math.BigDecimal", "java.util.Date", "lombok.Data",
                            "lombok.experimental.Accessors"), () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                Javadoc javadoc = new JavadocComment(
                        persistence.getDescrption() + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
                javadoc.addBlockTag(new JavadocBlockTag(Type.AUTHOR, conf.getAuthor() + " " + LocalDate.now()));
                coid.setJavadocComment(javadoc);
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Data"));
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Accessors(chain = true)"));
                coid.setPublic(true);
                coid.setName(persistence.getEntityName());
                for (PropertyDto property : persistence.getProperties()) {
                    FieldDeclaration field = coid.addField(property.getType(), property.getName(), Keyword.PRIVATE);
                    field.setJavadocComment(property.getDescription());
                }
                return coid;
            });
            cus.add(entityCuCreator.create(false));

            // 找到Mapper
            ClassOrInterfaceDeclaration mapper = findMapperOrElseCreate(conf, cus, persistence, entityPath,
                    entityCuCreator);
            if (mapper == null) {
                continue;
            }
            NodeList<BodyDeclaration<?>> members = mapper.getMembers();

            // 删除所有insert方法，再在头部插入int insert(BizEntity entity);
            List<MethodDeclaration> methods = mapper.getMethodsByName("insert");
            methods.forEach(Node::remove);
            MethodDeclaration insert = new MethodDeclaration();
            insert.setJavadocComment(new JavadocComment("插入数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            insert.setType(PrimitiveType.intType());
            insert.setName("insert");
            insert.addParameter(persistence.getEntityName(), "entity");
            insert.setBody(null);
            if (members.size() == 0) {
                members.add(insert);
            } else {
                members.set(0, insert);
            }

            //  删除所有updateById方法，再在前一个方法之后插入int updateById(BizEntity entity);
            methods = mapper.getMethodsByName("updateById");
            methods.forEach(Node::remove);
            MethodDeclaration updateById = new MethodDeclaration();
            updateById.setJavadocComment(
                    new JavadocComment("根据ID更新数据，值为null的属性不做更新" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            updateById.setType(PrimitiveType.intType());
            updateById.setName("updateById");
            updateById.addParameter(persistence.getEntityName(), "entity");
            updateById.setBody(null);
            members.addAfter(updateById, insert);

            // 删除所有queryById方法，再在前一个方法之后插入BizEntity queryById(Long id);
            methods = mapper.getMethodsByName("queryById");
            methods.forEach(Node::remove);
            MethodDeclaration queryById = new MethodDeclaration();
            queryById.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
            queryById.setName("queryById");
            queryById.addParameter(PrimitiveType.longType(), "id");
            queryById.setBody(null);
            members.addAfter(queryById, updateById);

            // 删除所有queryByIds方法，再在前一个方法之后插入Collection<BizEntity> queryById(Collection<Long> ids);
            methods = mapper.getMethodsByName("queryByIds");
            methods.forEach(Node::remove);
            MethodDeclaration queryByIds = new MethodDeclaration();
            queryByIds.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            queryByIds.setType(StaticJavaParser.parseType("Collection<" + persistence.getEntityName() + ">"));
            queryByIds.setName("queryByIds");
            queryByIds.addParameter(StaticJavaParser.parseType("Collection<Long>"), "ids");
            queryByIds.setBody(null);
            members.addAfter(queryByIds, queryById);

            // 删除所有queryByIdsAsMap方，再在前一个方法之后插入@MapKey("id") Map<Long, BizEntity> queryByIdsAsMap(Collection<Long> ids);
            methods = mapper.getMethodsByName("queryByIdsAsMap");
            methods.forEach(Node::remove);
            MethodDeclaration queryByIdsAsMap = new MethodDeclaration();
            queryByIds.setJavadocComment(
                    new JavadocComment("根据ID查询数据，并以ID为key映射到Map" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
            Imports.ensureImported(mapper, "java.util", false, true);
            queryByIdsAsMap.addAnnotation(StaticJavaParser.parseAnnotation("@MapKey(\"id\")"));
            queryByIdsAsMap.setType(StaticJavaParser.parseType("Map<Long, " + persistence.getEntityName() + ">"));
            queryByIdsAsMap.setName("queryByIdsAsMap");
            queryByIdsAsMap.addParameter(StaticJavaParser.parseType("Collection<Long>"), "ids");
            queryByIdsAsMap.setBody(null);
            members.addAfter(queryByIdsAsMap, queryByIds);

            cus.add(mapper.findCompilationUnit().orElseThrow(CuAbsentException::new));
        }

        cus.forEach(Saves::prettySave);
    }

    private static ClassOrInterfaceDeclaration findMapperOrElseCreate(PersistenceGeneratorConfig conf,
            Collection<CompilationUnit> cus, PersistenceDto persistence, Path entityPath, CuCreator entityCuCreator) {
        Path mapperPath = CodeGenerationUtils.fileInPackageAbsolutePath(conf.getSourceRoot(), conf.getMapperPackage(),
                persistence.getMapperName() + ".java");
        if (mapperPath.toFile().exists()) {
            CompilationUnit cu;
            try {
                cu = StaticJavaParser.parse(mapperPath);
            } catch (IOException e) {
                log.error("Ast parse failed, mapperPath={}", mapperPath, e);
                return null;
            }
            Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
            if (!primaryType.isPresent()) {
                log.error("primaryType absent, mapperPath={}", mapperPath);
                return null;
            }
            cus.add(cu);
            return primaryType.get().asClassOrInterfaceDeclaration();
        } else {
            log.info("Mapper file absent, create [{}]", entityPath);
            CuCreator mapperCuCreator = new CuCreator(Paths.get(conf.getSourceRoot()), conf.getMapperPackage(),
                    Lists.newArrayList(new ImportDeclaration("java.util", false, true),
                            new ImportDeclaration(entityCuCreator.getPrimaryTypeQualifier(), false, false),
                            new ImportDeclaration("org.apache.ibatis.annotations.MapKey", false, false)), () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                Javadoc javadoc = new JavadocComment(persistence.getDescrption()).parse();
                javadoc.addBlockTag(new JavadocBlockTag(Type.SEE, entityCuCreator.getPrimaryTypeName()));
                javadoc.addBlockTag(new JavadocBlockTag(Type.AUTHOR, conf.getAuthor() + " " + LocalDate.now()));
                coid.setJavadocComment(javadoc);
                coid.setPublic(true);
                coid.setInterface(true);
                coid.setName(persistence.getMapperName());
                return coid;
            });
            cus.add(mapperCuCreator.create(false));

            ClassOrInterfaceDeclaration result = mapperCuCreator.getPt().asClassOrInterfaceDeclaration();
            if (!result.isInterface()) {
                log.error("mapper must be a interface, mapperPath={}", mapperPath);
            }
            return result;

        }
    }

}