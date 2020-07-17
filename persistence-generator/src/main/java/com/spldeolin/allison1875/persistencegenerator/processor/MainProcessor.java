package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Log4j2
public class MainProcessor {

    private static final String oneIndent = "    ";

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
                    FieldDeclaration field = coid
                            .addField(property.getJavaType(), property.getPropertyName(), Keyword.PRIVATE);
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
                members.add(0, insert);
            }

            if (!persistence.getIsNonePK()) {
                // 删除所有updateById方法，再在前一个方法之后插入int updateById(BizEntity entity);
                methods = mapper.getMethodsByName("updateById");
                methods.forEach(Node::remove);
                MethodDeclaration updateById = new MethodDeclaration();
                updateById.setJavadocComment(
                        new JavadocComment("根据ID更新数据，忽略值为null的属性" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                updateById.setType(PrimitiveType.intType());
                updateById.setName("updateById");
                updateById.addParameter(persistence.getEntityName(), "entity");
                updateById.setBody(null);
                members.add(0, updateById);
            }

            if (!persistence.getIsNonePK()) {
                // 删除所有updateByIdForce方法，再在前一个方法之后插入int updateByIdForce(XxxEntity xxx);
                methods = mapper.getMethodsByName("updateByIdForce");
                methods.forEach(Node::remove);
                MethodDeclaration updateByIdForce = new MethodDeclaration();
                updateByIdForce.setJavadocComment(
                        new JavadocComment("根据ID更新数据，值为null的属性强制更新为null" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                updateByIdForce.setType(PrimitiveType.intType());
                updateByIdForce.setName("updateByIdForce");
                updateByIdForce.addParameter(persistence.getEntityName(), "entity");
                updateByIdForce.setBody(null);
                members.add(0, updateByIdForce);
            }

            if (!persistence.getIsNonePK()) {
                // 删除所有queryById方法，再在前一个方法之后插入BizEntity queryById(Long id);
                methods = mapper.getMethodsByName("queryById");
                methods.forEach(Node::remove);
                MethodDeclaration queryById = new MethodDeclaration();
                queryById.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
                queryById.setName("queryById");
                queryById.addParameter(PrimitiveType.longType(), "id");
                queryById.setBody(null);
                members.add(0, queryById);
            }

            if (!persistence.getIsNonePK()) {
                // 删除所有queryByIds方法，再在前一个方法之后插入Collection<BizEntity> queryByIds(Collection<Long> ids);
                methods = mapper.getMethodsByName("queryByIds");
                methods.forEach(Node::remove);
                MethodDeclaration queryByIds = new MethodDeclaration();
                queryByIds.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                queryByIds.setType(StaticJavaParser.parseType("Collection<" + persistence.getEntityName() + ">"));
                queryByIds.setName("queryByIds");
                queryByIds.addParameter(StaticJavaParser.parseType("Collection<Long>"), "ids");
                queryByIds.setBody(null);
                members.add(0, queryByIds);
            }

            if (!persistence.getIsNonePK()) {
                // 删除所有queryByIdsAsMap方，再在前一个方法之后插入@MapKey("id") Map<Long, BizEntity> queryByIdsEachId(Collection<Long>
                // ids);
                methods = mapper.getMethodsByName("queryByIdsEachId");
                methods.forEach(Node::remove);
                MethodDeclaration queryByIdsEachId = new MethodDeclaration();
                queryByIdsEachId.setJavadocComment(
                        new JavadocComment("根据ID查询数据，并以ID为key映射到Map" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
                Imports.ensureImported(mapper, "java.util", false, true);
                queryByIdsEachId.addAnnotation(StaticJavaParser.parseAnnotation("@MapKey(\"id\")"));
                queryByIdsEachId.setType(StaticJavaParser.parseType("Map<Long, " + persistence.getEntityName() + ">"));
                queryByIdsEachId.setName("queryByIdsEachId");
                queryByIdsEachId.addParameter(StaticJavaParser.parseType("Collection<Long>"), "ids");
                queryByIdsEachId.setBody(null);
                members.add(0, queryByIdsEachId);
            }

            cus.add(mapper.findCompilationUnit().orElseThrow(CuAbsentException::new));

            // Mapper.xml
            File mapperXmlFile = Paths.get(conf.getMapperXmlPath(), persistence.getMapperName() + ".xml").toFile();
            Element root = findDom4jRootOrElseCreate(mapperXmlFile);
            if (root == null) {
                continue;
            }

            overwriteNamespace(mapper, root);

            // 删除可能存在的resultMap(id=all)标签，并重新生成
            Element resultMapTag = Dom4jUtils.findAndRebuildElement(root, "resultMap", "id", "all");
            resultMapTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            resultMapTag.addAttribute("id", "all");
            resultMapTag.addAttribute("type", getEntityNameInXml(entityCuCreator));
            Element idTag = resultMapTag.addElement("id");
            idTag.addAttribute("column", "id");
            idTag.addAttribute("property", "id");
            for (PropertyDto property : persistence.getProperties()) {
                Element resultTag = resultMapTag.addElement("result");
                resultTag.addAttribute("column", property.getColumnName());
                resultTag.addAttribute("property", property.getPropertyName());
            }

            // 删除可能存在的sql(id=all)标签，并重新生成
            Element sqlTag = Dom4jUtils.findAndRebuildElement(root, "sql", "id", "all");
            sqlTag.addAttribute("id", "all");
            sqlTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            sqlTag.addText("\r\n" + Strings.repeat(oneIndent, 2) + persistence.getProperties().stream()
                    .map(PropertyDto::getColumnName).collect(Collectors.joining(",")));

            // Mapper.xml#insert
            Element insertTag = Dom4jUtils.findAndRebuildElement(root, "insert", "id", "insert");
            insertTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            insertTag.addAttribute("id", "insert");
            insertTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
            if (!persistence.getIsNonePK()) {
                insertTag.addAttribute("useGeneratedKeys", "true");
                String keyProperty = persistence.getProperties().stream().filter(PropertyDto::getIsPK)
                        .map(PropertyDto::getColumnName).collect(Collectors.joining(","));
                insertTag.addAttribute("keyProperty", keyProperty);
            }
            final StringBuilder sql = new StringBuilder(64);
            sql.append("\r\n").append(Strings.repeat(oneIndent, 2));
            sql.append("INSERT INTO ").append(persistence.getTableName()).append(" (");
            insertTag.addText(sql.toString());
            insertTag.addElement("include").addAttribute("refid", "all");
            sql.setLength(0);
            sql.append(") VALUES (");
            for (PropertyDto property : persistence.getProperties()) {
                sql.append("#{").append(property.getPropertyName()).append("},");
            }
            sql.deleteCharAt(sql.lastIndexOf(",")).append(")");
            insertTag.addText(sql.toString());
            sql.setLength(0);

            // Mapper.xml#updateById
            if (!persistence.getIsNonePK()) {
                Element updateByIdTag = Dom4jUtils.findAndRebuildElement(root, "update", "id", "updateById");
                updateByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                updateByIdTag.addAttribute("id", "updateById");
                updateByIdTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
                sql.append("\r\n").append(Strings.repeat(oneIndent, 2));
                sql.append("UPDATE ").append(persistence.getTableName());
                Element setTag = updateByIdTag.addElement("set");

                List<PropertyDto> nonPKs = persistence.getProperties().stream().filter(one -> !one.getIsPK())
                        .collect(Collectors.toList());
                for (PropertyDto nonPK : nonPKs) {
                    Element ifTag = setTag.addElement("if");
                    String ifTest = nonPK.getPropertyName() + "!=null";
                    if (String.class == nonPK.getJavaType()) {
                        ifTest += " and " + nonPK.getPropertyName() + "!=''";
                    }
                    ifTag.addAttribute("test", ifTest);
                    ifTag.addText("\r\n" + Strings.repeat(oneIndent, 4) + nonPK.getColumnName() + "=#{" + nonPK
                            .getPropertyName() + "},\r\n" + Strings.repeat(oneIndent, 3));
                }
                sql.append(" WHERE ");
                persistence.getProperties().stream().filter(PropertyDto::getIsPK).forEach(
                        property -> sql.append(property.getColumnName()).append("=#{")
                                .append(property.getPropertyName()).append("} AND "));
                String text = StringUtils.removeLast(sql.toString(), " AND ");
                updateByIdTag.addText(text);
            }

            // Mapper.xml#

            Dom4jUtils.write(mapperXmlFile, root);

        }

        cus.forEach(Saves::prettySave);
    }

    private static String getEntityNameInXml(CuCreator entityCuCreator) {
        if (PersistenceGeneratorConfig.getInstace().getIsEntityUsingAlias()) {
            return entityCuCreator.getPrimaryTypeName();
        } else {
            return entityCuCreator.getPrimaryTypeQualifier();
        }
    }

    private static void overwriteNamespace(ClassOrInterfaceDeclaration mapper, Element root) {
        root.addAttribute("namespace", mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
    }

    private static Element findDom4jRootOrElseCreate(File mapperXmlFile) {
        Document document;
        if (mapperXmlFile.exists()) {
            try {
                document = new SAXReader().read(mapperXmlFile);
                Element rootElement = document.getRootElement();
                if (rootElement == null) {
                    rootElement = document.addElement("mapper");
                }
                return rootElement;
            } catch (DocumentException e) {
                log.error("xml parse failed, mapperXmlFile={}", mapperXmlFile, e);
                return null;
            }
        } else {
            document = DocumentHelper.createDocument();
            document.addDocType("mapper",
                    "-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd", null);
            return document.addElement("mapper");
        }
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