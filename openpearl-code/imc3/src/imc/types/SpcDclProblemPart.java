package imc.types;

import org.w3c.dom.Node;


/**
 * parent class for SpcProblemPart and DclProblemPart
 * 
 * @author mueller
 *
 */
public class SpcDclProblemPart {
    private String userName;
    private String type;

    private Node locationInDomTree;
    private int line;
    private int col;

    private String attributes;  // DATION attributes , or MAIN for TASKs
    private String dationType;
 
    private String procParameters;
    private String procReturns;
    /**
     * name of the GLOBAL attribute
     * 
     * the name is defaulted in SPC and DCL by the current module name
     */
    private Module module;
    private String global;

    SpcDclProblemPart(String sn, int line, int col, String t, Node location, Module module,
            String global) {
        this.line = line;
        this.col = col;
        userName = sn;
        type = t;
        locationInDomTree = location;
        this.global = global;
        this.module = module;
    }

    public String getUserName() {
        return userName;
    }

    public String getType() {
        return type;
    }

    public Node getLocationInDomTree() {
        return locationInDomTree;
    }

    public Module getModule() {
        return module;
    }

    public String getGlobal() {
        return global;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getDationType() {
        return dationType;
    }

    public void setDationType(String dationType) {
        this.dationType = dationType;
    }


    public String getProcParameters() {
        return procParameters;
    }

    public void setProcParameters(String procParameters) {
        this.procParameters = procParameters;
    }

    public String getProcReturns() {
        return procReturns;
    }

    public void setProcReturns(String procReturns) {
        this.procReturns = procReturns;
    }
}
