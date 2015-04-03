package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Description of the acquisition settings or the instrument prior to the start of
 * the run.
 * <p/>
 * @author Rui Wang
 * Date: 05-Feb-2010
 * Time: 11:00:53
 */
public class ScanSetting extends ParamGroup {

    /**
     * identifier of the scan setting
     */
    private String id;

    /**
     * source file
     */
    private final List<SourceFile> sourceFile;

    /**
     * target list
     */
    private final List<ParamGroup> targets;

    /**
     * Constructor
     *
     * @param id         required.
     * @param sourceFile optional.
     * @param targets    optional.
     * @param params     optional.
     */
    public ScanSetting(String id, List<SourceFile> sourceFile, List<ParamGroup> targets, ParamGroup params) {
        super(params);
        this.id = id;
        this.sourceFile = CollectionUtils.createListFromList(sourceFile);
        this.targets = CollectionUtils.createListFromList(targets);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SourceFile> getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(List<SourceFile> sourceFile) {
        CollectionUtils.replaceValuesInCollection(sourceFile, this.sourceFile);
    }

    public List<ParamGroup> getTargets() {
        return targets;
    }

    public void setTargets(List<ParamGroup> targets) {
        CollectionUtils.replaceValuesInCollection(targets, this.targets);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanSetting)) return false;
        if (!super.equals(o)) return false;

        ScanSetting that = (ScanSetting) o;

        return !(id != null ? !id.equals(that.id) : that.id != null) && sourceFile.equals(that.sourceFile) && targets.equals(that.targets);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + sourceFile.hashCode();
        result = 31 * result + targets.hashCode();
        return result;
    }
}



