package uk.ac.ebi.pride.utilities.data.core;

/**
 * InstrumentComponent is key to keep the order of different instrument
 * component.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class InstrumentComponent extends ParamGroup {

    /**
     * order of the component among all instruments
     */
    private int order;

    /**
     * constructor
     *
     * @param order  required
     * @param params optional
     */
    public InstrumentComponent(int order, ParamGroup params) {
        super(params);
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        InstrumentComponent that = (InstrumentComponent) o;

        return order == that.order;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + order;
        return result;
    }
}



