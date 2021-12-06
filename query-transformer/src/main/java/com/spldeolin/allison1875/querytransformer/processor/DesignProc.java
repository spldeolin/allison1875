package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.utils.StringEscapeUtils;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.exception.IllegalDesignException;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.util.HashingUtils;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;

/**
 * @author Deolin 2021-07-01
 */
public class DesignProc {

    public ClassOrInterfaceDeclaration findDesign(AstForest astForest, MethodCallExpr chain) {
        String designQualifier = chain.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
        CompilationUnit designCu = astForest.findCu(designQualifier);

        List<Comment> orphanComments = designCu.getOrphanComments();
        if (orphanComments.size() < 2 || !orphanComments.get(1).isLineComment()) {
            throw new IllegalDesignException("cannot found Design Hashcode");
        }
        String hashcode = orphanComments.get(1).asLineComment().getContent().trim();

        if (!designCu.getPrimaryType().isPresent()) {
            throw new IllegalDesignException(
                    "cannot found Design Type in file [" + Locations.getStorage(designCu).getFileName()
                            + "], this Design file need to regenerate");
        }
        TypeDeclaration<?> primaryType = designCu.getPrimaryType().get();
        String hashing = HashingUtils.hashTypeDeclaration(primaryType);

        if (!hashing.equals(hashcode)) {
            throw new IllegalDesignException(
                    "modifications exist in Type [" + designQualifier + "], this Design file need to regenerate");
        }

        return designCu.getType(0).asClassOrInterfaceDeclaration();
    }

    public DesignMeta parseDesignMeta(ClassOrInterfaceDeclaration design) {
        FieldDeclaration queryMetaField = design.getFieldByName(TokenWordConstant.META_FIELD_NAME)
                .orElseThrow(IllegalChainException::new);
        Expression initializer = queryMetaField.getVariable(0).getInitializer()
                .orElseThrow(IllegalDesignException::new);
        String metaJson = StringEscapeUtils.unescapeJava(initializer.asStringLiteralExpr().getValue());
        return JsonUtils.toObject(metaJson, DesignMeta.class);
    }

    public StringBuilder parseOffset(ClassOrInterfaceDeclaration design) {
        FieldDeclaration queryMetaField = design.getFieldByName(TokenWordConstant.OFFSET_FIELD_NAME)
                .orElseThrow(IllegalChainException::new);
        Expression initializer = queryMetaField.getVariable(0).getInitializer()
                .orElseThrow(IllegalDesignException::new);
        String offsetText = initializer.asStringLiteralExpr().getValue();
        return new StringBuilder(offsetText);
    }

}