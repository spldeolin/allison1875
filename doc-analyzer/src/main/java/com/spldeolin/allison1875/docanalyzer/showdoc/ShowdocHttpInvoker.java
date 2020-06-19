package com.spldeolin.allison1875.docanalyzer.showdoc;

import java.util.concurrent.TimeUnit;
import com.spldeolin.allison1875.base.util.HttpUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-04
 */
@Log4j2
public class ShowdocHttpInvoker {

    public static void invoke(String groupNames, String description, String markdownContent, int sequence) {
        ShowdocRequest req = new ShowdocRequest();
        req.setApiKey(DocAnalyzerConfig.getInstance().getShowdocApiKey());
        req.setApiToken(DocAnalyzerConfig.getInstance().getShowdocApiToken());
        req.setCatName(groupNames.replace('.', '/'));
        req.setPageTitle(description);
        req.setPageContent(markdownContent);
        req.setSNumber(sequence);
        String rawResp = HttpUtils
                .postJson("https://www.showdoc.cc/server/api/item/updateByApi", JsonUtils.toJson(req));
        log.info(rawResp);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

}
