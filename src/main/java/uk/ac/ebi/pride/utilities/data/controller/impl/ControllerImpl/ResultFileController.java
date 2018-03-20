package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.access.ResultFileValidation;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;

import java.io.File;
import java.util.*;

/**
 * This class is an abstract implementation of the {@link ResultFileValidation} to provide methods to validate the ResultFiles.
 *
 *
 * @author ypriverol
 * @version $Id$
 *
 */
public class ResultFileController extends CachedDataAccessController implements ResultFileValidation{

    private static final Logger logger = LoggerFactory.getLogger(ResultFileController.class);


    /**
     * Default constructor
     * @param source
     * @param mode
     */
    public ResultFileController(Object source, DataAccessMode mode) {
        super(source, mode);
    }

    /**
   * This function check randomly if the spectra is well referenced for a couple of spectra
   * for that it is using the the number of Spectra to be check.
   *
   * @param numberSpectra the number of spectra to be checked
   * @return boolean if all the reference as fine
   */
  public boolean checkRandomSpectraByDeltaMassThreshold(int numberSpectra, Double deltaThreshold){
      boolean result = true;
      List<Comparable> listIds = new ArrayList<>(getProteinIds());
      Random r = new Random();
      int randomProtIdNumb;
      int randomPepIdNumb;
      if(hasSpectrum()) {
          for (int i=0; i < numberSpectra && result; i++){
              randomProtIdNumb = r.ints(0, listIds.size()).findFirst().getAsInt();
              Comparable proteinId = listIds.get(randomProtIdNumb);
              Protein protein = getProteinById(proteinId);
              randomPepIdNumb = r.ints(0, protein.getPeptides().size()).findFirst().getAsInt();
              Peptide peptide = protein.getPeptides().get(randomPepIdNumb);
              if (peptide==null) {
                  logger.error("Random peptide is null! Index:" + randomPepIdNumb);
                  result = false;
              } else {
                  Spectrum spectrum = getSpectrumById(peptide.getSpectrumIdentification().getId());
                  Integer charge = getPeptidePrecursorCharge(proteinId, peptide.getId());
                  double mz = getPeptidePrecursorMz(proteinId, peptide.getId());
                  List<Double> ptmMasses = new ArrayList<>();
                  for (Modification mod : peptide.getModifications()) {
                      List<Double> monoMasses = mod.getMonoisotopicMassDelta();
                      if (monoMasses != null && !monoMasses.isEmpty())
                          ptmMasses.add(monoMasses.get(0));
                  }
                  if ((charge == null || mz == -1)) {
                      if (spectrum!=null) {
                          charge = getSpectrumPrecursorCharge(spectrum.getId());
                          mz = getSpectrumPrecursorMz(spectrum.getId());
                      } else {
                          charge = null;
                      }
                      if (charge != null && charge == 0) {
                          charge = null;
                      }
                  }
                  if (charge == null) {
                      result = false;
                  } else {
                      Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptide.getSequence(), mz, charge, ptmMasses);
                      if (deltaMass == null || Math.abs(deltaMass) > deltaThreshold) {
                          result = false;
                      }
                  }
              }
          }
      } else {
          result = false;
    }
    return result;
  }

    /**
     * Todo: Please remove this part. This is DataAccess Controller
     * @return
     */
  public int getNumberOfIdentifiedSpectra() {
    return getIdentifiedSpectraNumber(false);
  }

    /**
     * Todo: Please remove this part. This is DataAccess Controller
     * @return
     */
  public int getNumberOfUniqueIdentifiedSpectra() {
    return getIdentifiedSpectraNumber(true);
  }

    /**
     * Todo: Idnetified Spectra comes from DataAccessController, this needs to be write in another part.
     * @param unique
     * @return
     */
  private int getIdentifiedSpectraNumber(boolean unique) {
    Collection<Comparable> allIdentifiedSpectrumIds;
    if (unique) {
      allIdentifiedSpectrumIds = new HashSet<>();
    } else {
      allIdentifiedSpectrumIds = new ArrayList<>();
    }
    for (Comparable proteinId : getProteinIds()) {
      for (Comparable peptideId : getPeptideIds(proteinId)) {
        Comparable specId = getPeptideSpectrumId(proteinId, peptideId);
        if (specId != null) {
          allIdentifiedSpectrumIds.add(specId);
        }
      }
    }
    return allIdentifiedSpectrumIds.size();
  }

    /**
     * Todo: Please review this code, it can be in the MissingSpectra part.
     * @return
     */
  public int getNumberOfMissingSpectra() {
    Set<Comparable> existingIdentifiedSpectrumIds = new HashSet<>();
    Set<Comparable> allIdentifiedSpectrumIds = new HashSet<>();
    Spectrum spectrum;
    for (Comparable proteinId : getProteinIds()) {
      for (Comparable peptideId : getPeptideIds(proteinId)) {
        Comparable specId = getPeptideSpectrumId(proteinId, peptideId);
        if (specId != null) {
          allIdentifiedSpectrumIds.add(specId);
        }
        spectrum = getSpectrumById(specId);
        if (spectrum != null) {
          existingIdentifiedSpectrumIds.add(spectrum.getId());
        }
      }
    }
    return allIdentifiedSpectrumIds.size() - existingIdentifiedSpectrumIds.size();
  }

    @Override
    public double checkRandomSpectraByDeltaMassThreshold(int numberOfSpectra) {
        return 0;
    }
}
