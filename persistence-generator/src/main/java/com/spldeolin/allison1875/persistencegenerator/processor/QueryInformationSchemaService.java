package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.processor.impl.QueryInformationSchemaServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(QueryInformationSchemaServiceImpl.class)
public interface QueryInformationSchemaService {

    Collection<InformationSchemaDto> process();

}