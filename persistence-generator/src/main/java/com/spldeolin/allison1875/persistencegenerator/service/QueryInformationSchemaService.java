package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.QueryInformationSchemaServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(QueryInformationSchemaServiceImpl.class)
public interface QueryInformationSchemaService {

    Collection<InformationSchemaDto> query();

}