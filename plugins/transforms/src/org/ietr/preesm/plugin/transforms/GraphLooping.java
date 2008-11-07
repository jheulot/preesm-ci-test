/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/
package org.ietr.preesm.plugin.transforms;

import java.util.List;

import org.ietr.preesm.core.task.IGraphTransformation;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;
import org.sdf4j.optimisations.clustering.Clusterize;
import org.sdf4j.optimisations.loops.detection.LoopDetector;

public class GraphLooping implements IGraphTransformation {

	@Override
	public TaskResult transform(SDFGraph algorithm, TextParameters params) {
		SDFGraph inGraph = algorithm.clone();
		LoopDetector detector = new LoopDetector(inGraph);
		String nbClust = params.getVariable("loopLength");
		List<List<SDFAbstractVertex>> loops = detector.getLoops(Integer
				.decode(nbClust));
		int i = 0;
		for (List<SDFAbstractVertex> loop : loops) {
			Clusterize.culsterizeBlocks(inGraph, loop, "cluster_" + i);
			i++;
		}
		TaskResult result = new TaskResult();
		result.setSDF(inGraph);
		return result;
	}

}
