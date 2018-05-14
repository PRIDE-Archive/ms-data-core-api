package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Suresh Hewapathirana
 */
@Getter
@NoArgsConstructor
public abstract class MzIdentMLObject {
    @XmlTransient
    private Long hid;
}
