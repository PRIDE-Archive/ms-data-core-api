package uk.ac.ebi.pride.utilities.data.core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QuantitativeSample which contains quantitative related information
 * This is mainly for PRIDE XMl
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 * Date: 09/08/2011
 * Time: 13:51
 */
public class QuantitativeSample {

    public static final int MAX_SUB_SAMPLE_SIZE = 8;
    private static final Pattern SUB_SAMPLE_PATTERN = Pattern.compile("[^\\d]+([\\d]+)[^\\d]*");

    /**
     * Array of sub samples
     */
    private final SubSample[] samples;

    public QuantitativeSample() {
        this.samples = new SubSample[MAX_SUB_SAMPLE_SIZE];
    }

    /**
     * Get the number of valid sub samples
     *
     * @return int number of sub samples
     */
    public int getNumberOfSubSamples() {
        int cnt = 0;

        for (SubSample sample : samples) {
            if (sample != null) {
                cnt++;
            }
        }

        return cnt;
    }

    /**
     * Check whether a subsample exist with a given index
     *
     * @param index index is one based
     * @return boolean true means a sub sample exist
     */
    public boolean hasSubSample(int index) {
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                return true;
            }
        }
        return false;
    }

    public void addsubSample(int index){
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample subSample = new SubSample(index);
            this.samples[index] = subSample;
        }
    }

    /**
     * Get whether species information exists
     *
     * @return boolean true means exist
     */
    public boolean hasSpecies() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getSpecies() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the species information of sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam species cv param
     */
    public CvParam getSpecies(int index) {
        CvParam species = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                species = sample.getSpecies();
            }
        }

        return species;
    }

    /**
     * Add species information to a subsample
     *
     * @param cvParam species cv param
     */
    public void setSpecies(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getValue());
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setSpecies(cvParam);
        }
    }

    /**
     * Add species information to a subsample
     *
     * @param cvParam species cv param
     */
    public void setSpecies(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setSpecies(cvParam);
        }
    }



    /**
     * Get whether cell line information exists
     *
     * @return boolean true means exist
     */
    public boolean hasCellLine() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getCellLine() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get cell line cv param of a sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam cell line cv param
     */
    public CvParam getCellLine(int index) {
        CvParam cellLine = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                cellLine = sample.getCellLine();
            }
        }

        return cellLine;
    }

    /**
     * Add cell line information to a subsample
     *
     * @param cvParam cell line cv param
     */
    public void setCellLine(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getValue());
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setCellLine(cvParam);
        }
    }

    /**
     * Add cell line information to a subsample
     *
     * @param cvParam cell line cv param
     */
    public void setCellLine(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setCellLine(cvParam);
        }
    }
    /**
     * Get whether tissue information exists
     *
     * @return boolean true means exist
     */
    public boolean hasTissue() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getTissue() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get tissue cv param of a sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam tissue cv param
     */
    public CvParam getTissue(int index) {
        CvParam tissue = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                tissue = sample.getTissue();
            }
        }

        return tissue;
    }

    /**
     * Add tissue information to a subsample
     *
     * @param cvParam tissue cv param
     */
    public void setTissue(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getValue());
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setTissue(cvParam);
        }
    }

    /**
     * Add tissue information to a subsample
     *
     * @param cvParam tissue cv param
     */
    public void setTissue(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setTissue(cvParam);
        }
    }

    /**
     * Get whether reagent information exists
     *
     * @return boolean true means exist
     */
    public boolean hasReagent() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getReagent() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get reagent cv param of a sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam reagent cv param
     */
    public CvParam getReagent(int index) {
        CvParam reagent = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                reagent = sample.getReagent();
            }
        }

        return reagent;
    }

    /**
     * Add reagent information to a subsample
     *
     * @param cvParam reagent cv param
     */
    public void setReagent(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getValue());
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setReagent(cvParam);
        }
    }

    /**
     * Add reagent information to a subsample
     *
     * @param cvParam reagent cv param
     */
    public void setReagent(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setReagent(cvParam);
        }
    }

    /**
     * Get whether disease information exists
     *
     * @return boolean true means exist
     */
    public boolean hasDisease() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getDisease() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get disease cv param of a sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam disease cv param
     */
    public CvParam getDisease(int index) {
        CvParam disease = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                disease = sample.getDisease();
            }
        }

        return disease;
    }

    /**
     * Add disease information to a subsample
     *
     * @param cvParam disease cv param
     */
    public void setDisease(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getValue());
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setDisease(cvParam);
        }
    }

    /**
     * Add disease information to a subsample
     *
     * @param cvParam disease cv param
     */
    public void setDisease(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setDisease(cvParam);
        }
    }

    /**
     * Get whether GO information exists
     *
     * @return boolean true means exist
     */
    public boolean hasGOTerm() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getGoTerm() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get GO cv param of a sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam GO cv param
     */
    public CvParam getGOTerm(int index) {
        CvParam goTerm = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                goTerm = sample.getGoTerm();
            }
        }

        return goTerm;
    }

    /**
     * Add GO information to a subsample
     *
     * @param cvParam GO cv param
     */
    public void setGOTerm(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getValue());
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setGoTerm(cvParam);
        }
    }

    /**
     * Add GO information to a subsample
     *
     * @param cvParam GO cv param
     */
    public void setGOTerm(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setGoTerm(cvParam);
        }
    }

    /**
     * Get whether description exists
     *
     * @return boolean true means exist
     */
    public boolean hasDescription() {
        for (SubSample sample : samples) {
            if (sample != null && sample.getDescription() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get description cv param of a sub sample with given index
     *
     * @param index sub sample index
     * @return CvParam description cv param
     */
    public CvParam getDescription(int index) {
        CvParam description = null;

        if (index >= 0 && index < MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample != null) {
                description = sample.getDescription();
            }
        }

        return description;
    }

    /**
     * Add description to a subsample
     *
     * @param cvParam description cv param
     */
    public void setDescription(CvParam cvParam) {
        int index = getSubSampleIndex(cvParam.getName());
        if (index > 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index - 1];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index - 1] = sample;
            }
            sample.setDescription(cvParam);
        }
    }

    /**
     * Add description to a subsample
     *
     * @param cvParam description cv param
     */
    public void setDescription(int index, CvParam cvParam) {
        if (index >= 0 && index <= MAX_SUB_SAMPLE_SIZE) {
            SubSample sample = samples[index];
            if (sample == null) {
                sample = new SubSample(index);
                samples[index] = sample;
            }
            sample.setDescription(cvParam);
        }
    }

    /**
     * Get the index of a sub sample string
     *
     * @param value sub sample string
     * @return int sub sample index
     */
    private int getSubSampleIndex(String value) {

        int index = -1;

        if (value != null) {
            Matcher m = SUB_SAMPLE_PATTERN.matcher(value);
            if (m.matches()) {
                index = Integer.parseInt(m.group(1));
            }
        }

        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuantitativeSample that = (QuantitativeSample) o;

        return Arrays.equals(samples, that.samples);

    }

    @Override
    public int hashCode() {
        return samples != null ? Arrays.hashCode(samples) : 0;
    }


    private static class SubSample {
        /**
         * Index of the sub sample
         */
        int index;
        /**
         * cv param contains species information
         */
        CvParam species;
        /**
         * cv param contains cell line information
         */
        CvParam cellLine;
        /**
         * cv param contains tissue information
         */
        CvParam tissue;
        /**
         * cv param contains disease
         */
        CvParam disease;
        /**
         * cv param contains GO term
         */
        CvParam goTerm;
        /**
         * cv param contains reagent information
         */
        CvParam reagent;
        /**
         * cv param contains sample description
         */
        CvParam description;

        public SubSample(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public CvParam getSpecies() {
            return species;
        }

        public void setSpecies(CvParam species) {
            this.species = species;
        }

        public CvParam getCellLine() {
            return cellLine;
        }

        public void setCellLine(CvParam cellLine) {
            this.cellLine = cellLine;
        }

        public CvParam getTissue() {
            return tissue;
        }

        public void setTissue(CvParam tissue) {
            this.tissue = tissue;
        }

        public CvParam getDisease() {
            return disease;
        }

        public void setDisease(CvParam disease) {
            this.disease = disease;
        }

        public CvParam getDescription() {
            return description;
        }

        public void setDescription(CvParam description) {
            this.description = description;
        }

        public CvParam getReagent() {
            return reagent;
        }

        public void setReagent(CvParam reagent) {
            this.reagent = reagent;
        }

        public CvParam getGoTerm() {
            return goTerm;
        }

        public void setGoTerm(CvParam goTerm) {
            this.goTerm = goTerm;
        }
    }


}
