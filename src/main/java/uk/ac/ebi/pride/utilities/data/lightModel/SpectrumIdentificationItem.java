package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

    /**
     * An identification of a single (poly)peptide, resulting from querying an input spectra, along with the set of confidence values for that identification.
     * PeptideEvidence elements should be given for all mappings of the corresponding Peptide sequence within protein sequences.
     *
     * TODO marshalling/ persistor add validation to check for case where someone gets peptide/massTable/sample and changes its id without updating ref id in
     *      SpectrumIdentificationItem and other such clases.
     *
     * NOTE: There is no setter method for the peptideRef/massTableRef/sampleRef. This simplifies keeping the peptide/massTable/sample object reference and
     * peptideRef/massTableRef/sampleRef synchronized.
     *
     * TODO: write an adaptor for changing List&lt;PeptideEvidenceRef&gt; to List&lt;String&gt;
     *
     * <p>Java class for SpectrumIdentificationItemType complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType name="SpectrumIdentificationItemType"&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="PeptideEvidenceRef" type="{http://psidev.info/psi/pi/mzIdentML/1.1}PeptideEvidenceRefType" maxOccurs="unbounded"/&gt;
     *         &lt;element name="Fragmentation" type="{http://psidev.info/psi/pi/mzIdentML/1.1}FragmentationType" minOccurs="0"/&gt;
     *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="chargeState" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
     *       &lt;attribute name="experimentalMassToCharge" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
     *       &lt;attribute name="calculatedMassToCharge" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
     *       &lt;attribute name="calculatedPI" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *       &lt;attribute name="peptide_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="rank" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
     *       &lt;attribute name="passThreshold" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
     *       &lt;attribute name="massTable_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="sample_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SpectrumIdentificationItemType")
    public class SpectrumIdentificationItem
            extends Identifiable
            implements Serializable
    {

        private final static long serialVersionUID = 100L;

        @XmlAttribute(required = true)
        protected int chargeState;
        @XmlAttribute(required = true)
        protected double experimentalMassToCharge;
        @XmlAttribute
        protected Double calculatedMassToCharge;
        @XmlAttribute
        protected Float calculatedPI;
        @XmlAttribute(name = "peptide_ref")
        protected String peptideRef;
        @XmlAttribute(required = true)
        protected int rank;
        @XmlAttribute(required = true)
        protected boolean passThreshold;
        @XmlTransient
        protected String formattedSpectrumID;

        /**
         * Gets the value of the chargeState property.
         *
         */
        public int getChargeState() {
            return this.chargeState;
        }

        /**
         * Sets the value of the chargeState property.
         *
         */
        public void setChargeState(int value) {
            this.chargeState = value;
        }

        /**
         * Gets the value of the experimentalMassToCharge property.
         *
         */
        public double getExperimentalMassToCharge() {
            return experimentalMassToCharge;
        }

        /**
         * Sets the value of the experimentalMassToCharge property.
         *
         */
        public void setExperimentalMassToCharge(double value) {
            this.experimentalMassToCharge = value;
        }

        /**
         * Gets the value of the calculatedMassToCharge property.
         *
         * @return
         *     possible object is
         *     {@link Double }
         *
         */
        public Double getCalculatedMassToCharge() {
            return calculatedMassToCharge;
        }

        /**
         * Sets the value of the calculatedMassToCharge property.
         *
         * @param value
         *     allowed object is
         *     {@link Double }
         *
         */
        public void setCalculatedMassToCharge(Double value) {
            this.calculatedMassToCharge = value;
        }

        /**
         * Gets the value of the calculatedPI property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getCalculatedPI() {
            return calculatedPI;
        }

        /**
         * Sets the value of the calculatedPI property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setCalculatedPI(Float value) {
            this.calculatedPI = value;
        }

        /**
         * Gets the value of the peptideRef property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getPeptideRef() {
            return peptideRef;
        }


        /**
         * Gets the value of the rank property.
         *
         */
        public int getRank() {
            return rank;
        }

        /**
         * Sets the value of the rank property.
         *
         */
        public void setRank(int value) {
            this.rank = value;
        }

        /**
         * Gets the value of the passThreshold property.
         *
         */
        public boolean isPassThreshold() {
            return passThreshold;
        }

        /**
         * Sets the value of the passThreshold property.
         *
         */
        public void setPassThreshold(boolean value) {
            this.passThreshold = value;
        }

        /**
         * get the value of the formattedSpectrumID property.
         *
         */
        public String getFormattedSpectrumID() {
            return formattedSpectrumID;
        }

        /**
         * Sets the value of the formattedSpectrumID property.
         *
         * @param formattedSpectrumID
         *     allowed object is
         *     {@link String }
         */
        public void setFormattedSpectrumID(String formattedSpectrumID) {
            this.formattedSpectrumID = formattedSpectrumID;
        }
    }

