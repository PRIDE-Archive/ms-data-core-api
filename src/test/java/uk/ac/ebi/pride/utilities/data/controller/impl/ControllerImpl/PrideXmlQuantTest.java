package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Quantification;
import uk.ac.ebi.pride.utilities.data.core.QuantitativeSample;
import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;
import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Test reading quantitative data from PRIDE Xml
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class PrideXmlQuantTest {

    private PrideXmlControllerImpl prideController = null;

    @Before
    public void setUp() throws Exception {
        URL url = PrideXmlControllerImplTest.class.getClassLoader().getResource("quant-pride.xml");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        prideController = new PrideXmlControllerImpl(inputFile);
    }

    @Test
    public void testNumOfSubSamples() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertEquals(4, sample.getNumberOfSubSamples());
    }

    @Test
    public void testHasSubSample() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertTrue(sample.hasSubSample(1));
        assertFalse(sample.hasSubSample(0));
        assertFalse(sample.hasSubSample(5));
    }

    @Test
    public void testHasSpecies() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertTrue(sample.hasSpecies());
    }

    @Test
    public void testSpecies() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getSpecies(1);
        assertEquals("Mus musculus (Mouse)", cvParam.getName());
        cvParam = sample.getSpecies(5);
        assertNull(cvParam);
    }

    @Test
    public void testHasCellLine() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertFalse(sample.hasCellLine());
    }

    @Test
    public void testCellLine() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getCellLine(1);
        assertNull(cvParam);
    }

    @Test
    public void testHasTissue() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertFalse(sample.hasTissue());
    }

    @Test
    public void testTissue() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getTissue(1);
        assertNull(cvParam);
    }

    @Test
    public void testHasDisease() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertFalse(sample.hasDisease());
    }

    @Test
    public void testDisease() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getDisease(1);
        assertNull(cvParam);
    }

    @Test
    public void testHasReagent() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertTrue(sample.hasReagent());
    }

    @Test
    public void testReagent() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getReagent(1);
        assertEquals("iTRAQ reagent 114", cvParam.getName());
        cvParam = sample.getReagent(5);
        assertNull(cvParam);
    }

    @Test
    public void testHasGOTerm() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertTrue(sample.hasGOTerm());
    }

    @Test
    public void testGOTerm() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getGOTerm(1);
        assertEquals("GO:0044456", cvParam.getAccession());
        cvParam = sample.getGOTerm(5);
        assertNull(cvParam);
    }

    @Test
    public void testHasDescription() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        assertTrue(sample.hasDescription());
    }

    @Test
    public void testDescription() throws Exception {
        QuantitativeSample sample = prideController.getQuantSample();
        CvParam cvParam = sample.getDescription(1);
        assertEquals("Biological replicate 2, P30", cvParam.getValue());
        cvParam = sample.getDescription(5);
        assertNull(cvParam);
    }

    @Test
    public void testHasQuantData() throws Exception {
        assertTrue(prideController.hasQuantData());
    }

    @Test
    public void testHasIdentQuantData() throws Exception {
        assertTrue(prideController.hasProteinQuantData());
    }

    @Test
    public void testHasPeptideQuantData() throws Exception {
        assertTrue(prideController.hasPeptideQuantData());
    }

    @Test
    public void testHasLabelFreeQuantMethod() throws Exception {
        assertFalse(prideController.hasLabelFreeQuantMethods());
    }

    @Test
    public void testHashIostopeLabellingQuantMethod() throws Exception {
        assertTrue(prideController.hasIsotopeLabellingQuantMethods());
    }

    @Test
    public void testQuantMethod() throws Exception {
        Collection<QuantCvTermReference> methods = prideController.getQuantMethods();
        assertEquals(1, methods.size());
        QuantCvTermReference cvTermReference = CollectionUtils.getElement(methods, 0);
        assertEquals("PRIDE:0000313", cvTermReference.getAccession());
    }

    @Test
    public void testNumOfReagents() throws Exception {
        assertEquals(4, prideController.getNumberOfReagents());
    }

    @Test
    public void testIdentQuantUnit() throws Exception {
        QuantCvTermReference cvTermReference = prideController.getProteinQuantUnit();
        assertEquals("Ratio", cvTermReference.getName());
    }

    @Test
    public void testPeptideQuantUnit() throws Exception {
        QuantCvTermReference cvTermReference = prideController.getPeptideQuantUnit();
        assertEquals("Ratio", cvTermReference.getName());
    }

    @Test
    public void testIdentQuantData() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertEquals(Quantification.Type.PROTEIN, quant.getType());
    }

    @Test
    public void testPeptideQuantData() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Collection<Comparable> peptideIds = prideController.getPeptideIds(identId);
        Comparable peptideId = CollectionUtils.getElement(peptideIds, 0);
        Quantification quant = prideController.getPeptideQuantData(identId, peptideId);
        assertEquals(Quantification.Type.PEPTIDE, quant.getType());
    }

    @Test
    public void testQuantHasLabelFree() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertFalse(quant.hasLabelFreeMethod());
    }

    @Test
    public void testQuantGetLabelFreeMethods() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertEquals(0, quant.getLabelFreeMethods().size());
    }

    @Test
    public void testQuantHasIsotopeLabelliing() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertTrue(quant.hasIsotopeLabellingMethod());
    }

    @Test
    public void testQuantHasIsotopeLabellingMethod() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertTrue(quant.hasIsotopeLabellingMethod());
    }

    @Test
    public void testQuantGetIsotopeLabellingMethod() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        QuantCvTermReference cvTermReference = quant.getIsotopeLabellingMethod();
        assertEquals("PRIDE:0000313", cvTermReference.getAccession());
    }

    @Test
    public void testQuantGetIsotopeLabellingResults() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        List<Double> results = quant.getIsotopeLabellingResults();
        assertEquals(1.0, results.get(0), 0.00000001);
        assertNull(results.get(4));
    }

    @Test
    public void testQuantGetIsotopeLabellingResult() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertEquals(1.004, quant.getIsotopeLabellingResult(3), 0.000001);
    }

    @Test
    public void testQuantHasTotalIntensities() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertFalse(quant.hasTotalIntensities());
    }

    @Test
    public void testQuantGetUnit() throws Exception {
        Collection<Comparable> identIds = prideController.getProteinIds();
        Comparable identId = CollectionUtils.getElement(identIds, 0);
        Quantification quant = prideController.getProteinQuantData(identId);
        assertEquals("Ratio", quant.getUnit().getName());
    }
}
