package imc.checks;

import java.util.List;

import imc.types.DclProblemPart;
import imc.types.Module;

public class CheckMainTasks {
	private int nbrOfMainTasks;
	
	public CheckMainTasks(List<Module> modules) {
		nbrOfMainTasks = 0;
		for (Module m: modules) {
			for (DclProblemPart decl: m.getDclProblemPart()) {
				if (decl.getType().equals("TASK")) {
					if (decl.getAttributes() != null && decl.getAttributes().equals("MAIN")) {
						nbrOfMainTasks ++;
					}
				}
			}
		}
	}

	public int getCount() {
		return nbrOfMainTasks;
		
	}

	
}
