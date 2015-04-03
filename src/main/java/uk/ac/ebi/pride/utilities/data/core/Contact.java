package uk.ac.ebi.pride.utilities.data.core;

/**
 * This class is just to organize the code, the ConstantRole class have to kind of instance,
 * one of then is Organization and the Other one if the person both objects extend the IdentifiableParamGroup
 * in order to organize the code we create a new abstract class named AbstractContact to separate the Contacts types
 * from the IdentifiableParamGroup.
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public abstract class Contact extends IdentifiableParamGroup {

    public Contact(Comparable id, String name) {
        super(id, name);
    }

    protected Contact(ParamGroup params, Comparable id, String name) {
        super(params, id, name);
    }
}



