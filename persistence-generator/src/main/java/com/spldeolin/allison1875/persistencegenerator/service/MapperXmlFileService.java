package com.spldeolin.allison1875.persistencegenerator.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperXmlFileServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperXmlFileServiceImpl.class)
public interface MapperXmlFileService {

    FileFlush process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper, Path mapperXmlDirectory,
            Collection<Collection<String>> sourceCodes) throws IOException;

}