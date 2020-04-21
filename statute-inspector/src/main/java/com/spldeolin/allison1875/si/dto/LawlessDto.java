package com.spldeolin.allison1875.si.dto;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.Locations;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-02-22
 */
@Data
@Accessors(chain = true)
public class LawlessDto {

    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    private String qualifier;

    private String statuteNo;

    private String message;

    private String author;

    private String fixer;

    private LocalDateTime fixedAt;

    public LawlessDto(Node node, String qualifier) {
        sourceCode = Locations.getRelativePathWithLineNo(node);
        this.qualifier = qualifier;
        author = Authors.getAuthor(node);
    }

    public LawlessDto(Node node) {
        sourceCode = Locations.getRelativePathWithLineNo(node);
        author = Authors.getAuthor(node);
    }

    public static void main(String[] args) throws Exception {
        Path path1 = Paths.get("/Users/deolin/Documents/project-repo/allison1875/snippet-transformer/target/classes");
        Path path2 = Paths.get("/Users/deolin/Documents/project-repo/allison1875/doc-analyzer/target/classes");
        ClassLoader classLoader = new URLClassLoader(new URL[] {path1.toUri().toURL(), path2.toUri().toURL()});

        Class<?> aClass = classLoader.loadClass("com.spldeolin.demo.classloader.Aa");

        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath("/Users/deolin/Documents/project-repo/allison1875/snippet-transformer/target/classes");
        CtClass cc = pool.get("com.spldeolin.demo.classloader.Aa");
        CtField a3 = cc.getField("a3");

        ClassFile cfile = cc.getClassFile();
        ConstPool cpool = cfile.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("com.fasterxml.jackson.annotation.JsonPropertyDescription", cpool);
        annot.addMemberValue("value", new StringMemberValue("askdfjasdf", cpool));
        attr.addAnnotation(annot);
        a3.getFieldInfo()
            .addAttribute(attr);

        // save
        Class<?> c = cc.toClass();

        JsonSchema jsonSchema = JsonSchemaUtils.generateSchema(c);


        Arrays.stream(aClass.getDeclaredFields())
            .forEach(System.out::println);
        System.out.println("!");
        Arrays.stream(aClass.getFields())
            .forEach(System.out::println);

        // ProjectRoot collect = new SymbolSolverCollectionStrategy().collect(
        //     Paths.get("/Users/deolin/Documents/project-repo/allison1875"));
        // collect.getSourceRoots().forEach(one -> {
        //     try {
        //         one.tryToParse().forEach(r -> {
        //             if (r.isSuccessful()) {
        //                 r.getResult().ifPresent(cu -> {
        //                     cu.getPrimaryType().ifPresent(pt->{
        //                         if (pt.getNameAsString().equals("LawlessDto")) {
        //                             ResolvedFieldDeclaration resolve = pt.getFields()
        //                                 .get(0)
        //                                 .resolve();
        //
        //                             System.out.println(1);
        //                         }
        //                     });
        //                 });
        //             }
        //         });
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // });

    }

}
