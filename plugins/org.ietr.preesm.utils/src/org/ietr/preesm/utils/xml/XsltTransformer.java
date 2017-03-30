/*******************************************************************************
 * Copyright or © or Copr. 2015 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2015)
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

package org.ietr.preesm.utils.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.workflow.tools.CLIWorkflowLogger;

/**
 * This class provides methods to transform an XML file via XSLT
 * 
 * @author Matthieu Wipliez
 * @author mpelcat
 * 
 */
public class XsltTransformer {

	private Transformer transformer;

	/**
	 * Creates a new {@link XsltTransform}
	 */
	public XsltTransformer() {
		super();
	}

	/**
	 * Sets an XSLT stylesheet contained in the file whose name is
	 * <code>fileName</code>.
	 * 
	 * @param fileName
	 *            The XSLT stylesheet file name.
	 * @throws TransformerConfigurationException
	 *             Thrown if there are errors when parsing the Source or it is
	 *             not possible to create a {@link Transformer} instance.
	 */
	public boolean setXSLFile(String fileName)
			throws TransformerConfigurationException {

		TransformerFactory factory = TransformerFactory.newInstance();

		Path xslFilePath = new Path(fileName);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile xslFile = root.getFile(xslFilePath);
		IPath path = xslFile.getLocation();
		if (path != null) {
			String xslFileLoc = xslFile.getLocation().toOSString();
			StreamSource source = new StreamSource(xslFileLoc);

			try {
				transformer = factory.newTransformer(source);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (transformer == null) {
			CLIWorkflowLogger.log(Level.SEVERE,
					"XSL sheet not found or not valid: " + fileName);
			return false;
		}

		return true;
	}

	/**
	 * Transforms the given input file and generates the output file
	 */
	public void transformFileToFile(String sourceFilePath, String destFilePath) {

		if (transformer != null) {
			Path osSourceFilePath = new Path(sourceFilePath);
			Path osDestFilePath = new Path(destFilePath);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile sourceFile = root.getFile(osSourceFilePath);
			IFile destFile = root.getFile(osDestFilePath);
			String sourceFileLoc = sourceFile.getLocation().toOSString();
			String destFileLoc = destFile.getLocation().toOSString();

			try {
				FileOutputStream outStream = new FileOutputStream(destFileLoc);
				StreamResult outResult = new StreamResult(outStream);
				transformer.transform(new StreamSource(sourceFileLoc),
						outResult);
				outStream.flush();
				outStream.close();

			} catch (FileNotFoundException e1) {
				CLIWorkflowLogger.log(
						Level.SEVERE,
						"Problem finding files for XSL transfo ("
								+ osSourceFilePath + "," + osDestFilePath + ")");
			} catch (TransformerException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
