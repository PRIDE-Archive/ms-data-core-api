package uk.ac.ebi.pride.utilities.data.io.file;

import ucar.ma2.InvalidRangeException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.utilities.netCDF.NetCDFFile;
import uk.ac.ebi.pride.utilities.netCDF.core.Metadata;
import uk.ac.ebi.pride.utilities.netCDF.core.MsScan;
import uk.ac.ebi.pride.utilities.netCDF.utils.netCDFParsingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 28/09/15
 */
public class NetCDFUnmarshallerAdaptor {

    NetCDFFile netCDFFile;


    public NetCDFUnmarshallerAdaptor(NetCDFFile netCDF, boolean useTitle) {
        netCDFFile = netCDF;
    }

    public MsScan getSpectrumById(Integer s) throws netCDFParsingException, InvalidRangeException, IOException {
        return netCDFFile.readNextScan(s);
    }

    public Collection<Comparable> getSpectrumIds() {
        return new ArrayList<Comparable>(netCDFFile.getScanIdentifiers());
    }

    public int getTotalScans(){
        return netCDFFile.getNumberScans();
    }

    public Metadata getMetadata() {
        return netCDFFile.getMetadata();
    }
}
