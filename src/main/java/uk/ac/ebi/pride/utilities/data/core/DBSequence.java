package uk.ac.ebi.pride.utilities.data.core;

/**
 * A database sequence from the specified SearchDatabase (nucleic acid or amino acid). If the sequence is nucleic acid,
 * the source nucleic acid sequence should be given in the seq attribute rather than a translated sequence.
 * @author Yasset Perez-Riverol
 * Date: 04/08/11
 * Time: 13:47
 */
public class DBSequence extends IdentifiableParamGroup {

    /**
     * The unique accession of this sequence.
     */
    private String accession;

    /**
     * protein accession version
     */
    private String accessionVersion;

    /**
     * The length of the sequence as a number of bases or residues.
     */
    private int length;

    /**
     * The source database of this sequence.
     */
    private SearchDataBase searchDataBase;

    /**
     * The actual sequence of amino acids or nucleic acid.
     */
    private String sequence;

    /**
     * optional splice isoform
     */
    private String spliceIsoform;

    public DBSequence(String accession, SearchDataBase searchDataBase, String accessionVersion,
                      String spliceIsoform) {
        this(null, null, null, -1, accession, searchDataBase, null, accessionVersion, spliceIsoform);
    }

    public DBSequence(ParamGroup params, Comparable id, String name, int length, String accession,
                      SearchDataBase searchDataBase, String sequence, String accessionVersion, String spliceIsoform) {
        super(params, id, name);
        this.length           = length;
        this.accession = accession;
        this.searchDataBase   = searchDataBase;
        this.sequence         = sequence;
        this.accessionVersion = accessionVersion;
        this.spliceIsoform    = spliceIsoform;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public SearchDataBase getSearchDataBase() {
        return searchDataBase;
    }

    public void setSearchDataBase(SearchDataBase searchDataBase) {
        this.searchDataBase = searchDataBase;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getAccessionVersion() {
        return accessionVersion;
    }

    public void setAccessionVersion(String accessionVersion) {
        this.accessionVersion = accessionVersion;
    }

    public String getSpliceIsoform() {
        return spliceIsoform;
    }

    public void setSpliceIsoform(String spliceIsoform) {
        this.spliceIsoform = spliceIsoform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DBSequence that = (DBSequence) o;

        if (length != that.length) return false;
        if (accession != null ? !accession.equals(that.accession) : that.accession != null) return false;
        if (accessionVersion != null ? !accessionVersion.equals(that.accessionVersion) : that.accessionVersion != null)
            return false;
        return !(searchDataBase != null ? !searchDataBase.equals(that.searchDataBase) : that.searchDataBase != null) && !(sequence != null ? !sequence.equals(that.sequence) : that.sequence != null) && !(spliceIsoform != null ? !spliceIsoform.equals(that.spliceIsoform) : that.spliceIsoform != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (accession != null ? accession.hashCode() : 0);
        result = 31 * result + (accessionVersion != null ? accessionVersion.hashCode() : 0);
        result = 31 * result + length;
        result = 31 * result + (searchDataBase != null ? searchDataBase.hashCode() : 0);
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 31 * result + (spliceIsoform != null ? spliceIsoform.hashCode() : 0);
        return result;
    }


}



