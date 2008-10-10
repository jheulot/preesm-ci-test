/**
 * 
 */
package org.ietr.preesm.core.scenario.editor;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * @author mpelcat
 *
 */
public class SDFLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		String name = "";
		if(element instanceof SDFAbstractVertex){
			name = ((SDFAbstractVertex)element).getName();
		}
		else if(element instanceof SDFGraph){
			name = "graph";
		}
		
		return name;
	}
	
}
