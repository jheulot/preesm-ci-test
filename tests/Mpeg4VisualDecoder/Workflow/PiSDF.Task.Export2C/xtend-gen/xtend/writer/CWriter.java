/*******************************************************************************
 * Copyright or © or Copr. 2013 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Yaset Oliva <yaset.oliva@insa-rennes.fr> (2013)
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
package xtend.writer;

import com.google.common.base.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface;
import org.ietr.preesm.experiment.model.pimm.DataInputInterface;
import org.ietr.preesm.experiment.model.pimm.DataOutputInterface;

@SuppressWarnings("all")
public class CWriter {
  public CharSequence writeHierarchyLevel(final String levelName, final EList<AbstractActor> vertices) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("void ");
    _builder.append(levelName, "");
    _builder.append(" (PiSDFGraph* graph, BaseVertex* parentVertex){");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("// Creating vertices.");
    _builder.newLine();
    {
      for(final AbstractActor vertex : vertices) {
        {
          Class<? extends AbstractActor> _class = vertex.getClass();
          boolean _equals = Objects.equal(_class, DataInputInterface.class);
          if (_equals) {
            _builder.append("\t  \t");
            _builder.append("PiSDFIfVertex* ");
            String _name = vertex.getName();
            _builder.append(_name, "	  	");
            _builder.append(" = (PiSDFIfVertex*)graph->addVertex(");
            String _name_1 = vertex.getName();
            _builder.append(_name_1, "	  	");
            _builder.append(", input_vertex);");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          Class<? extends AbstractActor> _class_1 = vertex.getClass();
          boolean _equals_1 = Objects.equal(_class_1, DataOutputInterface.class);
          if (_equals_1) {
            _builder.append("\t  \t");
            _builder.append("PiSDFIfVertex* ");
            String _name_2 = vertex.getName();
            _builder.append(_name_2, "	  	");
            _builder.append(" = (PiSDFIfVertex*)graph->addVertex(");
            String _name_3 = vertex.getName();
            _builder.append(_name_3, "	  	");
            _builder.append(", output_vertex);");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          Class<? extends AbstractActor> _class_2 = vertex.getClass();
          boolean _equals_2 = Objects.equal(_class_2, ConfigOutputInterface.class);
          if (_equals_2) {
            _builder.append("\t  \t");
            _builder.append("PiSDFConfigVertex* ");
            String _name_4 = vertex.getName();
            _builder.append(_name_4, "	  	");
            _builder.append(" = (PiSDFConfigVertex*)graph->addVertex(");
            String _name_5 = vertex.getName();
            _builder.append(_name_5, "	  	");
            _builder.append(", config_vertex);");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          Class<? extends AbstractActor> _class_3 = vertex.getClass();
          boolean _equals_3 = Objects.equal(_class_3, Actor.class);
          if (_equals_3) {
            _builder.append("\t  \t");
            _builder.append("PiSDFVertex* ");
            String _name_6 = vertex.getName();
            _builder.append(_name_6, "	  	");
            _builder.append(" = (PiSDFVertex*)graph->addVertex(");
            String _name_7 = vertex.getName();
            _builder.append(_name_7, "	  	");
            _builder.append(", pisdf_vertex);");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    _builder.append("}");
    return _builder;
  }
}
