/**
 * This file is used to do annotations on the package level.
 * In this case, namespace mapping for mzIdentML(XML) file has been added.
 *
 * @author Suresh Hewapathirana
 */

@XmlSchema(
        namespace = Constants.MZIDENTML_NAMESPACE,
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED
)
package uk.ac.ebi.pride.utilities.data.lightModel;

import uk.ac.ebi.pride.utilities.data.utils.Constants;
import javax.xml.bind.annotation.*;