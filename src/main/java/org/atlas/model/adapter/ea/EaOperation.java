package org.atlas.model.adapter.ea;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrews
 */
public class EaOperation {

    private String name;
    private String returnType;
    private String returnMany;
    private String documentation;
    private List<EaParameter> params = new ArrayList<EaParameter>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EaParameter> getParams() {
        return params;
    }

    public void setParams(List<EaParameter> params) {
        this.params = params;
    }

    public void addParameter(EaParameter p) {
        if (p.getType().endsWith("[]")) {
            p.setType(p.getType().substring(0, p.getType().length() - 2));
            p.setMany(true);
        }
        params.add(p);
    }

    public String getReturnMany() {
        return returnMany;
    }

    public void setReturnMany(String returnMany) {
        this.returnMany = returnMany;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }



}
