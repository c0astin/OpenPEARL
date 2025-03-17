package imc.types;

import org.w3c.dom.Node;

import imc.utilities.NodeUtils;

public class UserDationTfuInformation extends SpcDclProblemPart {
    String systemDationName;
    int tfusize;

    public UserDationTfuInformation(String username, int line, int col, Node location, Module module,
            String global, int tfusize) {
        super(username, line, col, "UserDation", location, module, global);
        Node sys = NodeUtils.getChildByName(location, "systemdation");
        this.systemDationName = NodeUtils.getAttributeByName(sys, "name");
        this.tfusize = tfusize;
    }

    /**
     * 
     * @return user name of system element
     */
    public String getSystemDationName() {
        return systemDationName;
    }

    public int getTfuSize() {
        return tfusize;
    }
}
