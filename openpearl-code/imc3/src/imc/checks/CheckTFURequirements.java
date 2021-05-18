package imc.checks;


import java.util.List;

import org.w3c.dom.Node;

import imc.types.Module;
import imc.types.ModuleEntrySystemPart;
import imc.types.Platform;
import imc.types.UserDationTfuInformation;
import imc.utilities.Log;
import imc.utilities.NodeUtils;

public class CheckTFURequirements {
	List<UserDationTfuInformation> userDationTfuInformations;
	
	public  CheckTFURequirements(List<Module> modules) {
		Log.info("start checking TFU requirements");
		
		for (Module m : modules) {
		   userDationTfuInformations = m.getUserDationTfuInformations(); 
		   for (int i=0; i<userDationTfuInformations.size(); i++) {
			   UserDationTfuInformation userDationTfuInfo = userDationTfuInformations.get(i);
			   // locate system node
			   String sysname = userDationTfuInfo.getSystemDationName();
			   if (sysname==null) continue; // nothing to do for configurations
			   Node systemEntry = getNodeOfSystemElement(modules, sysname);
			   Node tfuInSystemEntry = NodeUtils.getChildByName(systemEntry, "requireTFU");
			   String platformName = NodeUtils.getAttributeByName(systemEntry, "name");
			   if (tfuInSystemEntry!= null) {
				   // TFU requires is set
				   String sMaxTfu = NodeUtils.getAttributeByName(tfuInSystemEntry, "maxsize");
				   if (sMaxTfu!= null) {
					   int maxTfu = Integer.parseInt(sMaxTfu);
					   int udTfuSize = userDationTfuInformations.get(i).getTfuSize();
					   Log.info(sysname+ " has maxTfu of "+maxTfu+" -- UD has "+ udTfuSize);
					   Log.setLocation(m.getSourceFileName(),userDationTfuInfo.getLine(),userDationTfuInfo.getCol());
					   if (udTfuSize == -1) {
						   Log.error("userdation '"+userDationTfuInfo.getUserName()+
								   "' has no TFU -- system dation '"+platformName+"' requires TFU");
					   } else if (maxTfu<udTfuSize) {
						   Log.error("userdation '"+userDationTfuInfo.getUserName()+
								   "' requires a TFU record of "+udTfuSize+
								   "' -- system dation '"+platformName+"' provides max "+maxTfu);
					   }
					   
					   
				   }
			   }
			   


			   int x=0;
		   }
		}
	}
	
	private Node getNodeOfSystemElement(List<Module> modules, String sysname) {
			for ( Module m: modules) {
				Module module = m;
				for (ModuleEntrySystemPart se: module.getSystemElements()) {
					//Node seSystem = NodeUtils.getChildByName(se.getNode(), "sysname");
					String sysnameInSystemPart = se.getNameOfSystemelement();
					String unameInSystemPart = se.getUserName();
					if (unameInSystemPart==null) continue; // try next module
					if (unameInSystemPart.equals(sysname)) {
						Node platformNode = Platform.getInstance().getNodeOfSystemname(sysnameInSystemPart);
						return platformNode; //seSystem.getNode();
					}
				}
			}
			return null;
	}
}
