package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Suresh Hewapathirana
 */

public abstract class MzIdentMLObject {
    @XmlTransient
    private Long hid;

    public MzIdentMLObject() {
    }

    public Long getHid() {
        return this.hid;
    }
}
