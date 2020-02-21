package com.spldeolin.allison1875.da.view.rap;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.Jsons;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.domain.BodyFieldDomain;

/**
 * @author Deolin 2019-12-09
 */
public class RapConverter {

    public String convert(Collection<ApiDomain> apis) {
        String json = Jsons.toJson(convertApis(apis));

        StringBuilder sb = new StringBuilder(json.length());
        int id = -1;
        String[] parts = json.split("-2333");
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1) {
                sb.append(id--);
            }
        }
        return sb.toString();
    }

    private Collection<ActionListVo> convertApis(Collection<ApiDomain> apis) {
        Collection<ActionListVo> actions = Lists.newLinkedList();
        apis.forEach(api -> {
            ActionListVo action = ActionListVo.build(api);
            action.setRequestParameterList(convertFields(api.requestBodyFields()));
            action.setResponseParameterList(convertFields(api.responseBodyFields()));
            actions.add(action);
        });

        return actions;
    }

    private List<ParameterListVo> convertFields(Collection<BodyFieldDomain> fields) {
        List<ParameterListVo> firstFloor = Lists.newArrayList();
        if (fields == null) {
            return Lists.newArrayList();
        }
        for (BodyFieldDomain field : fields) {
            ParameterListVo child = ParameterListVo.build(field);
            if (field.childFields() != null && field.childFields().size() > 0) {
                this.convertFields(field.childFields(), child);
            }
            firstFloor.add(child);
        }
        return firstFloor;
    }

    private void convertFields(Collection<BodyFieldDomain> fields, ParameterListVo parent) {
        List<ParameterListVo> childrent = Lists.newArrayList();
        for (BodyFieldDomain field : fields) {
            ParameterListVo child = ParameterListVo.build(field);
            if (field.childFields() != null && field.childFields().size() > 0) {
                this.convertFields(field.childFields(), child);
            }
            childrent.add(child);
        }
        parent.setParameterList(childrent);
    }

}
