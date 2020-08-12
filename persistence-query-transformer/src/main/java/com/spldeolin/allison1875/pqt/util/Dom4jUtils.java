package com.spldeolin.allison1875.pqt.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-16
 */
@Log4j2
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
            revmoeContinuousBlankLines(mapperXmlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Collection<String> toSourceCodeLines(Element element) {
        try {
            StringWriter out = new StringWriter();
            OutputFormat prettyPrint = OutputFormat.createPrettyPrint();
            prettyPrint.setIndent("    ");
            prettyPrint.setTrimText(false);
            XMLWriter writer = new XMLWriter(out, prettyPrint);
            writer.write(element);
            writer.flush();
            return StringUtils.splitLineByLine(out.toString());
        } catch (Exception e) {
            log.error(e);
            return Lists.newArrayList();
        }
    }

    private static void revmoeContinuousBlankLines(File mapperXmlFile) throws IOException {
        Iterator<String> lines = FileUtils.readLines(mapperXmlFile, StandardCharsets.UTF_8).iterator();
        Collection<String> newLines = Lists.newArrayList();
        boolean isLastrowNotBlank = false;
        while (lines.hasNext()) {
            String line = lines.next();
            boolean isCurrentNotBlank = StringUtils.isNotBlank(line);
            if (isLastrowNotBlank || isCurrentNotBlank) {
                newLines.add(line);
            }
            isLastrowNotBlank = isCurrentNotBlank;
        }
        FileUtils.writeLines(mapperXmlFile, newLines);
    }

    public static Element findAndRebuildElement(Element ele, String tagName, String attributeName,
            String attributeValue) {
        Element tag = (Element) ele
                .selectSingleNode("./" + tagName + "[@" + attributeName + "='" + attributeValue + "']");
        if (tag != null) {
            tag.getParent().remove(tag);
        }
        tag = new DefaultElement(tagName);
        tag.addAttribute(attributeName, attributeValue);
        List<Element> elements = ele.elements();
        elements.add(elements.size(), tag);
        return tag;
    }

}