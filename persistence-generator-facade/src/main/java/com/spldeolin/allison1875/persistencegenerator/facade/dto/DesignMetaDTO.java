package com.spldeolin.allison1875.persistencegenerator.facade.dto;

import java.util.LinkedHashMap;
import java.util.List;
import com.spldeolin.allison1875.persistencegenerator.facade.enums.PageParamStyleEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-10-06
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DesignMetaDTO {

    String designQualifier;

    String designName;

    String entityQualifier;

    String entityName;

    String mapperQualifier;

    String mapperName;

    List<String> mapperPaths;

    LinkedHashMap<String, PropertyDTO> properties;

    String tableName;

    /**
     * 如果有逻辑删除，怎么样算作“数据未被删”
     * 如果properties中有逻辑删除标识，则值来自PersistenceGeneratorConfig#notDeletedSql
     */
    String notDeletedSql;

    /**
     * 指定Design类中的分页接口使用「pageNo + pageSize」还是「offset + limit」
     */
    PageParamStyleEnum pageParamStyle = PageParamStyleEnum.PAGE_NO_PAGE_SIZE;

}