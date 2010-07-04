package org.atlas.model.adapter.ea;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrews
 */
public class EaElement {

    private String id;
    private String type;
    private String name;
    private String stereotype;
    private String isAbstract;
    private List<EaAttribute> attributes = new ArrayList<EaAttribute>();
    private List<EaOperation> operations = new ArrayList<EaOperation>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStereotype() {
        return stereotype;
    }

    public void setStereotype(String stereotype) {
        this.stereotype = stereotype;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<EaAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<EaAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(EaAttribute a) {
        attributes.add(a);
    }

    public List<EaOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<EaOperation> operations) {
        this.operations = operations;
    }

    public void addOperation(EaOperation o) {
        operations.add(o);
    }

    public String getIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(String isAbstract) {
        this.isAbstract = isAbstract;
    }

    

}
