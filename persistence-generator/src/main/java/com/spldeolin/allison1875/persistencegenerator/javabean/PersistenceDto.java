package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.util.Collection;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-07-12
 */
@Data
@Accessors(chain = true)
public class PersistenceDto {

    private Boolean isNonePK;

    private String tableName;

    private String entityName;

    private String mapperName;

    private String descrption;

    private Collection<PropertyDto> properties;

}