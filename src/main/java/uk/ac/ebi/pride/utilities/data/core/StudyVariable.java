package uk.ac.ebi.pride.utilities.data.core;

import java.util.List;

/**
 * Study variable is use for Quantitation on mzTab files. It would be great to add have for mzTab
 * files they are own structure for Quantitation and this is represented using StudyVariable classes.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class StudyVariable extends IdentifiableParamGroup{

    //Assays related with this study variable.
    private List<Assay> assays;

    private List<Sample> samples;

    private String description;

    public StudyVariable(Comparable id, String name) {
        super(id, name);
    }

    public StudyVariable(ParamGroup params, Comparable id, String name) {
        super(params, id, name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Assay> getAssays() {
        return assays;
    }

    public void setAssays(List<Assay> assays) {
        this.assays = assays;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StudyVariable that = (StudyVariable) o;

        if (!assays.equals(that.assays)) return false;
        return description.equals(that.description) && samples.equals(that.samples);

    }
}
