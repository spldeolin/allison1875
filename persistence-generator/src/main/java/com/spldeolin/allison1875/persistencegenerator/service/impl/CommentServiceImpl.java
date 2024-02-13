package com.spldeolin.allison1875.persistencegenerator.service.impl;

import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.service.CommentService;

/**
 * @author Deolin 2021-05-24
 */
@Singleton
public class CommentServiceImpl implements CommentService {

    @Override
    public String analyzeColumnComment(InformationSchemaDto infoSchema) {
        return infoSchema.getColumnComment();
    }

    @Override
    public String analyzeTableComment(InformationSchemaDto infoSchema) {
        return infoSchema.getTableComment();
    }

}