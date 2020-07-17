package com.spldeolin.allison1875.persistencegenerator.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author Deolin 2020-07-16
 */
public class Dom4jUtils {

    public static void write(File mapperXmlFile, Node node) {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(StandardCharsets.UTF_8.name());
            format.setIndentSize(4);
            format.setTrimText(false);
            XMLWriter outPut = new XMLWriter(new FileWriter(mapperXmlFile), format);
            outPut.write(node.getDocument());
            outPut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}