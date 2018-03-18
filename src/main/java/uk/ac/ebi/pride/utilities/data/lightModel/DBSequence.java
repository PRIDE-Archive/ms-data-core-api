package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * @author Suresh Hewapathirana
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DBSequenceType")
public class DBSequence
        extends Identifiable
        implements Serializable
{
    private final static long serialVersionUID = 100L;

}
