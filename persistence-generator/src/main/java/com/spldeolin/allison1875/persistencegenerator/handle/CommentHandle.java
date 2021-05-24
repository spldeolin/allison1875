package com.spldeolin.allison1875.persistencegenerator.handle;

import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;

/**
 * @author Deolin 2021-05-24
 */
@Singleton
public class CommentHandle {

    public String resolveColumnComment(InformationSchemaDto infoSchema) {
        return infoSchema.getColumnComment();
    }

    public String resolveTableComment(InformationSchemaDto infoSchema) {
        return infoSchema.getTableComment();
    }

}