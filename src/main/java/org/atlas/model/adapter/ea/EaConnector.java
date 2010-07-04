package org.atlas.model.adapter.ea;

/**
 *
 * @author andrews
 */
public class EaConnector {

    private String sourceElement;
    private String sourceAggregation;
    private String sourceMultiplicity;
    private String sourceRole;
    private String targetElement;
    private String targetAggregation;
    private String targetMultiplicity;
    private String targetRole;
    private String type;

    public String getSourceAggregation() {
        return sourceAggregation;
    }

    public void setSourceAggregation(String sourceAggregation) {
        this.sourceAggregation = sourceAggregation;
    }

    public String getSourceElement() {
        return sourceElement;
    }

    public void setSourceElement(String sourceEntity) {
        this.sourceElement = sourceEntity;
    }

    public String getSourceMultiplicity() {
        return sourceMultiplicity;
    }

    public void setSourceMultiplicity(String sourceMultiplicity) {
        this.sourceMultiplicity = sourceMultiplicity;
    }

    public String getSourceRole() {
        return sourceRole;
    }

    public void setSourceRole(String sourceRole) {
        this.sourceRole = sourceRole;
    }

    public String getTargetAggregation() {
        return targetAggregation;
    }

    public void setTargetAggregation(String targetAggregation) {
        this.targetAggregation = targetAggregation;
    }

    public String getTargetElement() {
        return targetElement;
    }

    public void setTargetElement(String targetEntity) {
        this.targetElement = targetEntity;
    }

    public String getTargetMultiplicity() {
        return targetMultiplicity;
    }

    public void setTargetMultiplicity(String targetMultiplicity) {
        this.targetMultiplicity = targetMultiplicity;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    

}
