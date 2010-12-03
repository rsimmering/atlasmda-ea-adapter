package org.atlas.model.adapter.ea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.atlas.mda.Stereotype;
import org.atlas.mda.adapter.Adapter;
import org.atlas.mda.adapter.AdapterException;
import org.atlas.metamodel.Association;
import org.atlas.metamodel.Boundary;
import org.atlas.metamodel.Control;
import org.atlas.metamodel.Entity;
import org.atlas.metamodel.Enumeration;
import org.atlas.metamodel.Operation;
import org.atlas.metamodel.Parameter;
import org.atlas.metamodel.Model;
import org.atlas.metamodel.Property;
import org.xml.sax.SAXException;

/**
 *
 * @author andrews
 */
public class ModelAdapter implements Adapter {

    private static final String ROOT = "xmi:XMI/xmi:Extension";
    private static final String ELEMENT = ROOT + "/elements/element";
    private static final String ATTRIBUTE = ELEMENT + "/attributes/attribute";
    private static final String OPERATION = ELEMENT + "/operations/operation";
    private static final String PARAMETER = OPERATION + "/parameters/parameter";
    private static final String OWNED_PARAMETER = "xmi:XMI/uml:Model/packagedElement/packagedElement/ownedOperation/ownedParameter";
    private static final String CONNECTOR = ROOT + "/connectors/connector";
    private static final String UML_CLASS = "uml:Class";
    private static final String NONE = "none";
    private static final String COMPOSITE = "composite";
    private static final String ASSOCIATION = "Association";
    private static final String GENERALIZATION = "Generalization";
    private static final String AGGREGATION = "Aggregation";
    private static final String MANY = "*";
    private static final String ONEMANY = "1..*";
    private static final String ZEROMANY = "0..*";
    private Map<String, EaElement> ELEMENTS;
    private Map<String, EaParameter> OWNED_PARAMETERS;
    private List<EaConnector> CONNECTORS;
    private Model model;

    public ModelAdapter() {
        ELEMENTS = new HashMap<String, EaElement>();
        OWNED_PARAMETERS = new HashMap<String, EaParameter>();
        CONNECTORS = new ArrayList<EaConnector>();
    }

    public Model adapt(File file, Model inputModel) throws AdapterException {
        model = inputModel;

        parse(file);
        normalize();

        return model;
    }

    public void parse(File file) {
        FileInputStream s = null;
        try {
            s = new FileInputStream(file);
            Digester d = new Digester();
            d.push(this);
            d.addObjectCreate(ELEMENT, EaElement.class);
            d.addSetProperties(ELEMENT, new String[]{"xmi:idref", "xmi:type", "name"}, new String[]{"id", "type", "name"});
            d.addSetProperties(ELEMENT + "/properties", new String[]{"stereotype", "isAbstract"}, new String[]{"stereotype", "isAbstract"});
            d.addSetNext(ELEMENT, "addElement");

            d.addObjectCreate(ATTRIBUTE, EaAttribute.class);
            d.addSetProperties(ATTRIBUTE, new String[]{"name"}, new String[]{"name"});
            d.addSetProperties(ATTRIBUTE + "/properties", new String[]{"type"}, new String[]{"type"});
            d.addSetNext(ATTRIBUTE, "addAttribute");

            d.addObjectCreate(OPERATION, EaOperation.class);
            d.addSetProperties(OPERATION, new String[]{"name"}, new String[]{"name"});
            d.addSetProperties(OPERATION + "/type", new String[]{"type", "returnarray"}, new String[]{"returnType", "returnMany"});
            d.addSetProperties(OPERATION + "/documentation", new String[]{"value"}, new String[]{"documentation"});
            d.addSetNext(OPERATION, "addOperation");

            d.addObjectCreate(PARAMETER, EaParameter.class);
            d.addSetProperties(PARAMETER, new String[]{"xmi:idref"}, new String[]{"id"});
            d.addSetProperties(PARAMETER + "/properties", new String[]{"type"}, new String[]{"type"});
            d.addSetNext(PARAMETER, "addParameter");

            d.addObjectCreate(OWNED_PARAMETER, EaParameter.class);
            d.addSetProperties(OWNED_PARAMETER, new String[]{"xmi:id", "name", "direction"}, new String[]{"id", "name", "direction"});
            d.addSetNext(OWNED_PARAMETER, "addOwnedParameter");

            d.addObjectCreate(CONNECTOR, EaConnector.class);
            d.addSetProperties(CONNECTOR + "/source/model", new String[]{"name"}, new String[]{"sourceElement"});
            d.addSetProperties(CONNECTOR + "/source/role", new String[]{"name"}, new String[]{"sourceRole"});
            d.addSetProperties(CONNECTOR + "/source/type", new String[]{"multiplicity", "aggregation"}, new String[]{"sourceMultiplicity", "sourceAggregation"});
            d.addSetProperties(CONNECTOR + "/target/model", new String[]{"name"}, new String[]{"targetElement"});
            d.addSetProperties(CONNECTOR + "/target/role", new String[]{"name"}, new String[]{"targetRole"});
            d.addSetProperties(CONNECTOR + "/target/type", new String[]{"multiplicity", "aggregation"}, new String[]{"targetMultiplicity", "targetAggregation"});
            d.addSetProperties(CONNECTOR + "/properties", new String[]{"ea_type"}, new String[]{"type"});
            d.addSetNext(CONNECTOR, "addConnector");

            d.parse(s);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ModelAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModelAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ModelAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                s.close();
            } catch (IOException ex) {
                Logger.getLogger(ModelAdapter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void normalize() throws AdapterException {

        for (EaElement e : ELEMENTS.values()) {
            if (e.getType().equals(UML_CLASS) && e.getStereotype() != null) {
                switch (Stereotype.valueOf(e.getStereotype())) {
                    case entity:
                        addEntity(e);
                        break;
                    case enumeration:
                        addEnumeration(e);
                        break;
                    case control:
                        addControl(e);
                        break;
                    case boundary:
                        addBoundary(e);
                        break;
                    default:
                        break;
                }
            }
        }

        for (EaConnector c : CONNECTORS) {
            normalize(c);
        }

    }

    private void normalize(EaConnector c) throws AdapterException {
        Entity sourceEntity = model.getEntity(c.getSourceElement());
        Entity targetEntity = model.getEntity(c.getTargetElement());

        if (ASSOCIATION.equals(c.getType())) {
            if (sourceEntity != null && targetEntity != null) {
                normalizeAssociation(c);
                return;
            }

            Boundary sourceBoundary = model.getBoundary(c.getSourceElement());
            Boundary targetBoundary = model.getBoundary(c.getTargetElement());

            if (sourceBoundary != null && targetBoundary != null) {
                normalizeNavigation(c);
            }
        } else if (AGGREGATION.equals(c.getType())) {
            normalizeAggregation(c);
        } else if (GENERALIZATION.equals(c.getType())) {
            normalizeGeneralization(c);
        }
    }

    private void normalizeGeneralization(EaConnector c) {
        Entity source = model.getEntity(c.getSourceElement());
        source.setGeneralization(model.getEntity(c.getTargetElement()));
    }

    private void normalizeAssociation(EaConnector c) throws AdapterException {
        Entity source = model.getEntity(c.getSourceElement());
        Entity target = model.getEntity(c.getTargetElement());

        String m = c.getSourceMultiplicity();
        boolean sourceIsMany = (m.equals(MANY) || m.equals(ZEROMANY) || m.equals(ONEMANY)) ? true : false;
        m = c.getTargetMultiplicity();
        boolean targetIsMany = (m.equals(MANY) || m.equals(ZEROMANY) || m.equals(ONEMANY)) ? true : false;

        Association sourceAssn = new Association();
        sourceAssn.setEntity(model.getEntity(c.getTargetElement()));

        Association targetAssn = new Association();
        targetAssn.setEntity(model.getEntity(c.getSourceElement()));

        if (c.getSourceAggregation().equals(NONE) && c.getTargetAggregation().equals(NONE)) {
            // Either M:1 or M:M
            if (sourceIsMany) {
                if (targetIsMany) {
                    sourceAssn.setMultiplicity(Association.Multiplicity.ManyToMany);
                    sourceAssn.setOwner(true);
                    targetAssn.setMultiplicity(Association.Multiplicity.ManyToMany);
                    source.addAssociation(sourceAssn);
                    target.addAssociation(targetAssn);
                } else {
                    sourceAssn.setMultiplicity(Association.Multiplicity.ManyToOne);
                    targetAssn.setMultiplicity(Association.Multiplicity.ManyToOne);
                    if (c.getTargetRole() != null) {
                        sourceAssn.setName(c.getTargetRole());
                    }
                    source.addAssociation(sourceAssn);
                }
            } else if (targetIsMany) {
                sourceAssn.setMultiplicity(Association.Multiplicity.ManyToOne);
                targetAssn.setMultiplicity(Association.Multiplicity.ManyToOne);
                if (c.getSourceRole() != null) {
                    targetAssn.setName(c.getSourceRole());
                }
                target.addAssociation(targetAssn);
            }
        } else {
            throw new AdapterException("Invalid aggregation on assocation between [" + c.getSourceElement() + "] and [" + c.getTargetElement() + "]");
        }
    }

    private void normalizeAggregation(EaConnector c) throws AdapterException {
        Entity source = model.getEntity(c.getSourceElement());
        Entity target = model.getEntity(c.getTargetElement());

        String m = c.getSourceMultiplicity();
        boolean sourceIsMany = (m.equals(MANY) || m.equals(ZEROMANY) || m.equals(ONEMANY)) ? true : false;
        m = c.getTargetMultiplicity();
        boolean targetIsMany = (m.equals(MANY) || m.equals(ZEROMANY) || m.equals(ONEMANY)) ? true : false;

        Association sourceAssn = new Association();
        sourceAssn.setEntity(model.getEntity(c.getTargetElement()));

        Association targetAssn = new Association();
        targetAssn.setEntity(model.getEntity(c.getSourceElement()));

        if (c.getSourceAggregation().equals(COMPOSITE) && c.getTargetAggregation().equals(NONE)) {
            if (sourceIsMany) {
                throw new AdapterException("Only multiplicity of 1 can be on the composite side of association between [" + c.getSourceElement() + "] and [" + c.getTargetElement() + "]");
            }

            sourceAssn.setOwner(true);
            targetAssn.setOwner(false);
            sourceAssn.setName(c.getSourceRole());
            targetAssn.setName(c.getTargetRole());

            if (targetIsMany) {
                sourceAssn.setMultiplicity(Association.Multiplicity.OneToMany);
                targetAssn.setMultiplicity(Association.Multiplicity.OneToMany);
            } else {
                sourceAssn.setMultiplicity(Association.Multiplicity.OneToOne);
                targetAssn.setMultiplicity(Association.Multiplicity.OneToOne);
            }

            source.addAssociation(sourceAssn);
            target.addAssociation(targetAssn);
        } else if (c.getSourceAggregation().equals(NONE) && c.getTargetAggregation().equals(COMPOSITE)) {
            if (targetIsMany) {
                throw new AdapterException("Only multiplicity of 1 can be on the composite side of association between [" + c.getSourceElement() + "] and [" + c.getTargetElement() + "]");
            }

            sourceAssn.setOwner(false);
            targetAssn.setOwner(true);
            sourceAssn.setName(c.getTargetRole());
            targetAssn.setName(c.getSourceRole());

            if (sourceIsMany) {
                sourceAssn.setMultiplicity(Association.Multiplicity.OneToMany);
                targetAssn.setMultiplicity(Association.Multiplicity.OneToMany);
            } else {
                sourceAssn.setMultiplicity(Association.Multiplicity.OneToOne);
                targetAssn.setMultiplicity(Association.Multiplicity.OneToOne);
            }
            source.addAssociation(sourceAssn);
            target.addAssociation(targetAssn);
        } else {
            throw new AdapterException("Invalid aggregation on assocation between [" + c.getSourceElement() + "] and [" + c.getTargetElement() + "]");
        }

    }

    private void addEntity(EaElement e) {
        Entity entity = new Entity();
        entity.setName(e.getName());
        entity.setAbstractEntity(("true".equals(e.getIsAbstract())));
        for (EaAttribute a : e.getAttributes()) {
            entity.addProperty(normalizeProperty(a));
        }
        for (EaOperation eo : e.getOperations()) {
            entity.addOperation(normalizeOperation(eo));
        }

        model.addEntity(entity);
    }

    private void addEnumeration(EaElement e) {
        Enumeration en = new Enumeration();
        en.setName(e.getName());
        for (EaAttribute a : e.getAttributes()) {
            en.addLiteral(a.getName());
        }

        model.addEnumeration(en);
    }

    private void addControl(EaElement e) {
        Control c = new Control();
        c.setName(e.getName());
        for (EaOperation eo : e.getOperations()) {
            c.addOperation(normalizeOperation(eo));
        }

        model.addControl(c);
    }

    private void addBoundary(EaElement e) {
        Boundary b = new Boundary();
        b.setName(e.getName());
        for (EaOperation eo : e.getOperations()) {
            b.addOperation(normalizeOperation(eo));
        }
        for (EaAttribute a : e.getAttributes()) {
            b.addProperty(normalizeProperty(a));
        }

        model.addBoundary(b);
    }

    private Property normalizeProperty(EaAttribute a) {
        Property p = new Property();
        p.setName(a.getName());
        p.setType(a.getType());

        return p;
    }

    private Operation normalizeOperation(EaOperation eo) {
        Operation o = new Operation();
        o.setName(eo.getName());
        o.setReturnType(eo.getReturnType());
        o.setReturnMany((eo.getReturnMany().equals("0") ? false : true));
        o.setDocumentation(StringUtils.remove(eo.getDocumentation(), '\n'));
        for (EaParameter ep : eo.getParams()) {
            EaParameter ownedParameter = OWNED_PARAMETERS.get(ep.getId());
            if (ownedParameter != null && !ownedParameter.getDirection().equals("return")) {
                Parameter p = new Parameter();
                p.setName(ownedParameter.getName());
                p.setType(ep.getType());
                p.setMany(ownedParameter.isMany());
                o.addParameter(p);
            }
        }

        return o;
    }

    public void addElement(EaElement e) {
        try {
            if (e.getType() != null && e.getType().equals(UML_CLASS)) {
                if (e.getStereotype() != null && Stereotype.valueOf(e.getStereotype()) != null) {
                    ELEMENTS.put(e.getId(), e);
                }
            }
        } catch (IllegalArgumentException ex) {
        }
    }

    public void addConnector(EaConnector c) {
        CONNECTORS.add(c);
    }

    public void addOwnedParameter(EaParameter p) {
        if (p.getType().endsWith("[]")) {
            p.setType(p.getType().substring(0, p.getType().length() - 2));
            p.setMany(true);
        }
        OWNED_PARAMETERS.put(p.getId(), p);
    }

    private void normalizeNavigation(EaConnector c) {
    }
}
