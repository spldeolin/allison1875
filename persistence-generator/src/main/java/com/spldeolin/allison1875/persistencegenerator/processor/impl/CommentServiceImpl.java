package com.spldeolin.allison1875.persistencegenerator.processor.impl;

import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.processor.CommentService;

/**
 * @author Deolin 2021-05-24
 */
@Singleton
public class CommentServiceImpl implements CommentService {

    @Override
    public String resolveColumnComment(InformationSchemaDto infoSchema) {
        return infoSchema.getColumnComment();
    }

    @Override
    public String resolveTableComment(InformationSchemaDto infoSchema) {
        return infoSchema.getTableComment();
    }

}