package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.utils.StringEscapeUtils;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.exception.IllegalDesignException;
import com.spldeolin.allison1875.querytransformer.exception.SameNameTerminationMethodException;
import com.spldeolin.allison1875.querytransformer.service.DesignService;

/**
 * @author Deolin 2021-07-01
 */
public class DesignServiceImpl implements DesignService {

    @Override
    public ClassOrInterfaceDeclaration findDesign(AstForest astForest, MethodCallExpr chain) {
        String designQualifier = chain.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
        Optional<CompilationUnit> opt = astForest.findCu(designQualifier);
        if (!opt.isPresent()) {
            throw new SameNameTerminationMethodException();
        }
        CompilationUnit designCu = opt.get();

        List<Comment> orphanComments = designCu.getOrphanComments();
        if (orphanComments.size() < 2 || !orphanComments.get(1).isLineComment()) {
            throw new IllegalDesignException("cannot found Design Hashcode");
        }
        String hashcode = orphanComments.get(1).asLineComment().getContent().trim();

        if (!designCu.getPrimaryType().isPresent()) {
            throw new IllegalDesignException(
                    "cannot found Design Type in file [" + CompilationUnitUtils.getCuAbsolutePath(designCu)
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

    @Override
    public DesignMeta parseDesignMeta(ClassOrInterfaceDeclaration design) {
        FieldDeclaration queryMetaField = design.getFieldByName(TokenWordConstant.META_FIELD_NAME)
                .orElseThrow(IllegalChainException::new);
        Expression initializer = queryMetaField.getVariable(0).getInitializer()
                .orElseThrow(IllegalDesignException::new);
        String metaJson = StringEscapeUtils.unescapeJava(initializer.asStringLiteralExpr().getValue());
        return JsonUtils.toObject(metaJson, DesignMeta.class);
    }

}