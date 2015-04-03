package uk.ac.ebi.pride.utilities.data.core;

/**
 * <p/>
 * Description of source file, including identification file, location and type.
 * the attributes fileFormat is used to manage the cvterm for file format in MzIdentMl
 * and the externalFormatDocumentURI is used to store the information of the external shcema.
 * <p/>
 * In mzML 1.1.0.1, the following cv terms must be added:
 * <p/>
 * 1. Must include only one a child term of "native spectrum identifier format"
 * (thermo nativeID format, waters nativeID format, WIFF nativeID format and et al)
 * <p/>
 * 2. Must include one or more child terms of "data file check-sum type" (MD5, SHA-1)
 * <p/>
 * 3. Must include only one child term of "source file type" (waters raw file,
 * ABI WIFF file, Thermo RAW file and et al)
 * <p/>
 * @author Rui Wang, Yasset Perez-Riverol
 * Date: 04-Feb-2010
 * Time: 15:53:36
 */
public class SourceFile extends IdentifiableParamGroup {

    /**
     * A URI to access documentation and tools to interpret the external format
     * of the ExternalData instance. For example, XML Schema or static libraries
     * (APIs) to access binary formats.
     */
    private String externalFormatDocumentationURI;

    /**
     * The format of the ExternalData file, for example "tiff" for image files.
     */
    private CvParam fileFormat;

    /**
     * location of the source file
     */
    private String path;

    public SourceFile(String name, String path) {
        // there should be a single source file per spectrum
        super(null, name);
        this.path = path;
    }

    public SourceFile(ParamGroup params, String id, String name, String path) {
        super(params, id, name);
        this.path = path;
    }

    public SourceFile(ParamGroup params, String id, String name, String path, CvParam fileFormat,
                      String externalFormatDocumentationURI) {
        super(params, id, name);
        this.path = path;
        this.fileFormat = fileFormat;
        this.externalFormatDocumentationURI = externalFormatDocumentationURI;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CvParam getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(CvParam fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getExternalFormatDocumentationURI() {
        return externalFormatDocumentationURI;
    }

    public void setExternalFormatDocumentationURI(String externalFormatDocumentationURI) {
        this.externalFormatDocumentationURI = externalFormatDocumentationURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SourceFile that = (SourceFile) o;

        return !(externalFormatDocumentationURI != null ? !externalFormatDocumentationURI.equals(that.externalFormatDocumentationURI) : that.externalFormatDocumentationURI != null) && !(fileFormat != null ? !fileFormat.equals(that.fileFormat) : that.fileFormat != null) && !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (externalFormatDocumentationURI != null ? externalFormatDocumentationURI.hashCode() : 0);
        result = 31 * result + (fileFormat != null ? fileFormat.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}



