package uk.ac.ebi.pride.utilities.data.controller.cache;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * CacheCategory provides a list of cache categories
 * Each category defines the type of the data and the type of data structure to store the data.
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public enum CacheEntry {

    SPECTRUM(CachedMap.class, 10),                             // Map<Spectrum id, Spectrum>
    SPECTRADATA_TO_SPECTRUMIDS(HashMap.class, null),           // Map<Comparable, List<Comparable>>
    PROTEIN_TO_PEPTIDE_EVIDENCES(HashMap.class, null),         //Map<db squence id,List<Spectrum identification item id>>>
    PROTEIN_TO_PROTEIN_GROUP_ID(HashMap.class, null),          // Map<Comparable, Comparable>
    PROTEIN_GROUP_ID(ArrayList.class, null),                   // List of Protein Groups
    CHROMATOGRAM(CachedMap.class, 10),                         // Map<Chromatogram id, Chromatogram>
    PROTEIN(CachedMap.class, 20),                              // Map<Identification id, Identification>
    PROTEIN_GROUP(CachedMap.class, 5),                         // Map<Protein group id, Protein group>
    PEPTIDE(CachedMap.class, 40),                              // Map<Tuple<Comparable, Comparable>, Peptide>
    EXPERIMENT_ACC(ArrayList.class, null),                     // List<Experiement Accession>
    EXPERIMENT_METADATA(ArrayList.class, null),                // List<Experiment Metadata>
    PROTEIN_METADATA(ArrayList.class, null),                   // List of Identification Metadata for Protein Identification
    MZGRAPH_METADATA(ArrayList.class, null),                   // List of MZGraph Metadata
    SEARCH_ENGINE_TYPE(ArrayList.class, null),                 // List<SearchEngineCvTermReferences>
    PROTEIN_LEVEL_SCORES(ArrayList.class, null),               // List<SearchEngineScoreCvTermReferences>
    PEPTIDE_LEVEL_SCORES(ArrayList.class, null),               // List<SearchEngineScoreCvTermReferences>
    SPECTRUM_ID(HashSet.class, null),                        // List<Spectrum id>
    CHROMATOGRAM_ID(ArrayList.class, null),                    // List<Chromatogram id>
    PROTEIN_ID(ArrayList.class, null),                         // List<Identification id>
    MS_LEVEL(HashMap.class, null),                             // Map<Spectrum id, Ms level>
    SPECTRUM_LEVEL_PRECURSOR_CHARGE(CachedMap.class, 10),      // Map<Spectrum id, Precursor charge>
    PEPTIDE_PRECURSOR_CHARGE(CachedMap.class, 40),             // Map<ProteinID, PeptideID, charge>
    PEPTIDE_PRECURSOR_MZ(CachedMap.class, 40),                 // Map<ProteinID, PeptideID, mz>
    SPECTRUM_LEVEL_PRECURSOR_MZ(CachedMap.class, 10),          // Map<Spectrum id, Precursor m/z>
    PRECURSOR_INTENSITY(HashMap.class, null),                  // Map<Spectrum id, Precursor intensity>
    PROTEIN_ACCESSION(HashMap.class, null),                    // Map<Identification id, Protein accession>
    PROTEIN_ACCESSION_VERSION(HashMap.class, null),            // Map<Identification id, Protein accession version>
    DB_SEQUENCE(CachedMap.class, 40),                          // Map<DBSequence Id, DBSequence>
    SPECTRUM_ID_ITEM(HashMap.class, null),                     // Map<Spectrum Identification Id, Spectrum IdentificationItem>
    PEPTIDE_EVIDENCE(HashMap.class, null),                   // Map<PEPTIDE EVIDENCE ID , PEPTIDE EVIDENCE>
    PROTEIN_SEARCH_DATABASE(HashMap.class, null),              // Map<Identification id, Protein search database>
    PROTEIN_SEARCH_DATABASE_VERSION(HashMap.class, null),      // Map<Identification id, Protein search database version>
    SCORE(HashMap.class, null),                                // Map<Identification id, Score>
    THRESHOLD(HashMap.class, null),                            // Map<Identification id, Threshold>
    PROTEIN_TO_PARAM(HashMap.class, null),                     // Map<Identification id, ParamGroup>
    PROTEIN_TO_PEPTIDE(HashMap.class, null),                   // Map<Identification id, List<Peptide id>>
    PEPTIDE_SEQUENCE(CachedMap.class, 40),                     // Map<Peptide Id, peptide sequence>
    PEPTIDE_RANK(CachedMap.class, 40),                         // Map<Tuple<ProteinID, PetideID>, rank>
    PEPTIDE_START(CachedMap.class, 40),                        // Map<Peptide Id, peptide start location>
    PEPTIDE_END(CachedMap.class, 40),                          // Map<Peptide Id, peptide end location>
    PEPTIDE_TO_SPECTRUM(HashMap.class, null),                  // Map<Peptide Id, spectrum id>  in mzidentml the spectrum have two
    QUANTPEPTIDE_TO_SPECTREUM(HashMap.class, null),            // Map<Peptide Id, spectreum ID> in mztab we will have a Map for quanttative peptides
    PROTEIN_TO_QUANTPEPTIDES(HashMap.class, null),             // Map<db squence id,List<Spectrum identification item id>>>
    // components the spectrum id and the file id, then is Ma<Peptide Id, String[]>
    PEPTIDE_TO_PARAM(CachedMap.class, 40),                     // Map<Peptide Id, ParamGroup>
    NUMBER_OF_FRAGMENT_IONS(HashMap.class, null),              // Map<Peptide Id, number of fragment ions>
    PEPTIDE_TO_MODIFICATION(CachedMap.class, 40),              // Map<Peptide Id, List<Tuple<Accession, location>>>
    MODIFICATION(HashMap.class, null),                         // Map<Accession, Modification>, a light weight implementation
    SUM_OF_INTENSITY(HashMap.class, null),                     // Map<Spectrum id, sum of all intensity>
    NUMBER_OF_PEAKS(HashMap.class, null),                      // Map<Spectrum id, number of peaks>
    SEARCH_DATABASE(HashMap.class, null),

    PROTEIN_QUANT_UNIT(ArrayList.class, null),                 // List<QuantCvTermReference>
    PEPTIDE_QUANT_UNIT(ArrayList.class, null),                 // List<QuantCvTermReference>
    FRAGMENTATION_TABLE(HashMap.class, null),                  // Map<Fragmentation id, IdentifiableParamGroup>
    CV_LOOKUP(HashMap.class, null),                            // Map<cv label, CVLookup>
    SPECTRA_DATA(HashMap.class, null),                         // Map<Spectra data id, SpectraData>

    MGF_INDEX_TITLE(HashMap.class, null),                      // In some cases it would be interesting to retrieve the file instead of using index using the title.
    SPECTRA_DATA_MGF_TITLE(ArrayList.class, null),
    TITLE_MGF_INDEX(HashMap.class, null),
    SPECTRUM_IDENTIFIED(ArrayList.class, 10000);

    private final Class dataStructType;
    private final Integer size;

    CacheEntry(Class dataStructType, Integer size) {
        this.dataStructType = dataStructType;
        this.size = size;
    }

    public Class getDataStructType() {
        return dataStructType;
    }

    public Integer getSize() {
        return size;
    }


}



