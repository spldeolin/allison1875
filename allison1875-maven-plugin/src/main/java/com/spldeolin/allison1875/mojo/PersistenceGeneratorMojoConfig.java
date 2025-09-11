package com.spldeolin.allison1875.mojo;

import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-07-29
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class PersistenceGeneratorMojoConfig extends PersistenceGeneratorConfig {

    String module = PersistenceGeneratorModule.class.getName();

}
