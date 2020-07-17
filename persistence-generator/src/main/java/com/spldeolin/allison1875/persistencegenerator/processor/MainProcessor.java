package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.atteo.evo.inflector.English;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
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

    private static final String singleIndent = "    ";

    private static final String doubleIndex = Strings.repeat(singleIndent, 2);

    private static final String trebleIndex = Strings.repeat(singleIndent, 3);

    private static final String newLine = "\r\n";

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
                coid.addAnnotation(parseAnnotation("@Data"));
                coid.addAnnotation(parseAnnotation("@Accessors(chain = true)"));
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

            boolean hasPK = persistence.getPkProperties().size() > 0;

            if (hasPK) {
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

            if (hasPK) {
                // 删除所有updateByIdEvenNull方法，再在前一个方法之后插入int updateByIdEvenNull(XxxEntity xxx);
                methods = mapper.getMethodsByName("updateByIdEvenNull");
                methods.forEach(Node::remove);
                MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
                updateByIdEvenNull.setJavadocComment(
                        new JavadocComment("根据ID更新数据，值为null的属性强制更新为null" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                updateByIdEvenNull.setType(PrimitiveType.intType());
                updateByIdEvenNull.setName("updateByIdEvenNull");
                updateByIdEvenNull.addParameter(persistence.getEntityName(), "entity");
                updateByIdEvenNull.setBody(null);
                members.add(0, updateByIdEvenNull);
            }

            if (hasPK) {
                // 删除所有queryById方法，再在前一个方法之后插入BizEntity queryById(PkType id);
                methods = mapper.getMethodsByName("queryById");
                methods.forEach(Node::remove);
                MethodDeclaration queryById = new MethodDeclaration();
                queryById.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
                queryById.setName("queryById");
                Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    String varName = StringUtils.lowerFirstLetter(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@Param(\"" + varName + "\")" + pk.getJavaType().getSimpleName() + " " + varName);
                    queryById.addParameter(parameter);
                }
                queryById.setBody(null);
                members.add(0, queryById);
            }

            if (persistence.getPkProperties().size() == 1) {
                // 删除所有queryByIds方法，再在前一个方法之后插入List<BizEntity> queryByIds(Collection<PkType> ids);
                methods = mapper.getMethodsByName("queryByIds");
                methods.forEach(Node::remove);
                MethodDeclaration queryByIds = new MethodDeclaration();
                queryByIds.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                Imports.ensureImported(mapper, "java.util.List");
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
                queryByIds.setType(parseType("List<" + persistence.getEntityName() + ">"));
                queryByIds.setName("queryByIds");
                String varsName = English.plural(StringUtils.lowerFirstLetter(onlyPk.getPropertyName()));
                Parameter parameter = parseParameter(
                        "@Param(\"" + varsName + "\") Collection<" + onlyPk.getJavaType().getSimpleName() + "> "
                                + varsName);
                queryByIds.addParameter(parameter);
                queryByIds.setBody(null);
                members.add(0, queryByIds);
            }

            if (persistence.getPkProperties().size() == 1) {
                // 删除所有queryByIdsAsMap方法，再插入@MapKey("id") Map<Long, BizEntity> queryByIdsEachId(Collection<Long> ids);
                methods = mapper.getMethodsByName("queryByIdsEachId");
                methods.forEach(Node::remove);
                MethodDeclaration queryByIdsEachId = new MethodDeclaration();
                queryByIdsEachId.setJavadocComment(
                        new JavadocComment("根据ID查询数据，并以ID为key映射到Map" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
                Imports.ensureImported(mapper, "java.util.Map");
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
                String varName = StringUtils.lowerFirstLetter(onlyPk.getPropertyName());
                String pkTypeName = onlyPk.getJavaType().getSimpleName();
                queryByIdsEachId.setType(parseType(
                        "@MapKey(\"" + varName + "\")" + "Map<" + pkTypeName + ", " + persistence.getEntityName()
                                + ">"));
                queryByIdsEachId.setName("queryByIdsEachId");
                String varsName = English.plural(varName);
                queryByIdsEachId.addParameter(
                        parseParameter("@Param(\"" + varsName + "\") Collection<" + pkTypeName + "> " + varsName));
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
            root.addText(newLine);
            Element resultMapTag = Dom4jUtils.findAndRebuildElement(root, "resultMap", "id", "all");
            resultMapTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            resultMapTag.addAttribute("type", getEntityNameInXml(entityCuCreator));
            for (PropertyDto pk : persistence.getPkProperties()) {
                Element resultTag = resultMapTag.addElement("id");
                resultTag.addAttribute("column", pk.getColumnName());
                resultTag.addAttribute("property", pk.getPropertyName());
            }
            for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                Element resultTag = resultMapTag.addElement("result");
                resultTag.addAttribute("column", nonPk.getColumnName());
                resultTag.addAttribute("property", nonPk.getPropertyName());
            }

            // 删除可能存在的sql(id=all)标签，并重新生成
            root.addText(newLine);
            Element sqlTag = Dom4jUtils.findAndRebuildElement(root, "sql", "id", "all");
            sqlTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            sqlTag.addText(newLine + doubleIndex + persistence.getProperties().stream().map(PropertyDto::getColumnName)
                    .collect(Collectors.joining(",")));

            // Mapper.xml#insert
            root.addText(newLine);
            Element insertTag = Dom4jUtils.findAndRebuildElement(root, "insert", "id", "insert");
            insertTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            insertTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
            if (hasPK) {
                insertTag.addAttribute("useGeneratedKeys", "true");
                String keyProperty = persistence.getPkProperties().stream().map(PropertyDto::getColumnName)
                        .collect(Collectors.joining(","));
                insertTag.addAttribute("keyProperty", keyProperty);
            }
            final StringBuilder sql = new StringBuilder(64);
            sql.append(newLine).append(doubleIndex);
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
            if (hasPK) {
                root.addText(newLine);
                Element updateByIdTag = Dom4jUtils.findAndRebuildElement(root, "update", "id", "updateById");
                updateByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                updateByIdTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
                updateByIdTag.addText(newLine + doubleIndex + "UPDATE " + persistence.getTableName());

                Element setTag = updateByIdTag.addElement("set");
                for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                    Element ifTag = setTag.addElement("if");
                    String ifTest = nonPk.getPropertyName() + "!=null";
                    if (String.class == nonPk.getJavaType()) {
                        ifTest += " and " + nonPk.getPropertyName() + "!=''";
                    }
                    ifTag.addAttribute("test", ifTest);
                    ifTag.addText(newLine + Strings.repeat(singleIndent, 4) + nonPk.getColumnName() + "=#{" + nonPk
                            .getPropertyName() + "},\r\n" + trebleIndex);
                }

                sql.append(" WHERE ");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    sql.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("} AND ");
                }
                String text = StringUtils.removeLast(sql.toString(), " AND ");
                sql.setLength(0);
                updateByIdTag.addText(text);
            }

            // Mapper.xml#updateByIdEvenNull
            if (hasPK) {
                root.addText(newLine);
                Element updateByIdEvenNullTag = Dom4jUtils
                        .findAndRebuildElement(root, "update", "id", "updateByIdEvenNull");
                updateByIdEvenNullTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                updateByIdEvenNullTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
                sql.append(newLine).append(doubleIndex).append("UPDATE ").append(persistence.getTableName());
                sql.append(newLine).append(doubleIndex).append("SET ");
                for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                    sql.append(nonPk.getColumnName()).append("=#{").append(nonPk.getPropertyName()).append("},");
                }
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(newLine).append(doubleIndex).append("WHERE ");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    sql.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("} AND ");
                }
                String text = StringUtils.removeLast(sql, " AND ");
                sql.setLength(0);
                updateByIdEvenNullTag.addText(text);
            }

            // Mapper.xml#queryById
            if (hasPK) {
                root.addText(newLine);
                Element queryByIdTag = Dom4jUtils.findAndRebuildElement(root, "select", "id", "queryById");
                queryByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                queryByIdTag.addAttribute("resultMap", "all");
                queryByIdTag.addText(newLine + doubleIndex + "SELECT");
                queryByIdTag.addElement("include").addAttribute("refid", "all");
                sql.append(newLine).append(doubleIndex).append("FROM ").append(persistence.getTableName());
                sql.append(newLine).append(doubleIndex).append("WHERE ");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    sql.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("},");
                }
                String text = StringUtils.removeLast(sql, ",");
                sql.setLength(0);
                queryByIdTag.addText(text);
            }

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
                cu = parse(mapperPath);
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
                            new ImportDeclaration("org.apache.ibatis.annotations", false, true)), () -> {
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