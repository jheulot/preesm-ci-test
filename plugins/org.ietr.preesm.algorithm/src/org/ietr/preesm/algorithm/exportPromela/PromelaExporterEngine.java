/*******************************************************************************
 * Copyright or © or Copr. 2015 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2015)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ietr.preesm.algorithm.exportPromela;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.preesm.core.scenario.PreesmScenario;

// TODO: Auto-generated Javadoc
/**
 * The Class PromelaExporterEngine.
 *
 * @author kdesnos
 */
public class PromelaExporterEngine {

  /**
   * Prints the SDF graph to promela file.
   *
   * @param sdf
   *          the sdf
   * @param scenario
   *          the scenario
   * @param path
   *          the path
   * @param fifoShared
   *          the fifo shared
   * @param synchronousActor
   *          the synchronous actor
   */
  public void printSDFGraphToPromelaFile(final SDFGraph sdf, final PreesmScenario scenario,
      IPath path, final boolean fifoShared, final boolean synchronousActor) {
    /// Create the exporter
    final PromelaPrinter exporter = new PromelaPrinter(sdf, scenario);
    exporter.setFifoSharedAlloc(fifoShared);
    exporter.setSynchronousActor(synchronousActor);

    try {
      if ((path.getFileExtension() == null) || !path.getFileExtension().equals("pml")) {
        path = path.addFileExtension("pml");
      }
      final IWorkspace workspace = ResourcesPlugin.getWorkspace();
      final IFile iFile = workspace.getRoot().getFile(path);
      if (!iFile.exists()) {
        iFile.create(null, false, new NullProgressMonitor());
      }
      final File file = new File(iFile.getRawLocation().toOSString());

      // Write the result into the text file
      exporter.write(file);

    } catch (final CoreException e) {
      e.printStackTrace();
    }
  }

}
