ms-data-core-api
===============
[![Build Status](https://travis-ci.org/PRIDE-Utilities/ms-data-core-api.svg)](https://travis-ci.org/PRIDE-Utilities/ms-data-core-api)

# About

The primary purpose of ms-data-core-api library is to provide commonly used classes and Object Model for Proteomics Experiments. You may also find it useful for your own computational proteomics projects.

# Main Features
* Common Object Model for different proteomics experiments, with classes to represent proteins, peptides, psms, psectrums 
* DataAccessControllers for mzTab, mzIdentML, PRIDE XML, PRIDE Database, mzML, mzXML, mgf, pkl, apl, ms2, dta files
* Proteomics Standard compleint Data model with classes for Ontologies and User params 
* Read different file formats in proteomics in a common Object Model
* Export the current model to mzTab files in for Identification Experiments.

**Note**: the library is still evolving, we are committed to expand this library and add more useful classes.

# Getting ms-data-core-api

The zip file in the releases section contains the ms-data-core-api jar file and all other required libraries.

Maven Dependency

PRIDE Utilities library can be used in Maven projects, you can include the following snippets in your Maven pom file.
 
 ```maven
 <dependency>
   <groupId>uk.ac.ebi.pride.utilities</groupId>
   <artifactId>ms-data-core-api</artifactId>
   <version>x.x.x</version>
 </dependency> 
 ```
 ```maven
 <!-- EBI repo -->
 <repository>
     <id>nexus-ebi-release-repo</id>
     <url>http://www.ebi.ac.uk/Tools/maven/repos/content/groups/ebi-repo/</url>
 </repository>
 
 <!-- EBI SNAPSHOT repo -->
 <snapshotRepository>
    <id>nexus-ebi-snapshots-repo</id>
    <url>http://www.ebi.ac.uk/Tools/maven/repos/content/groups/ebi-snapshots/</url>
 </snapshotRepository>
```
Note: you need to change the version number to the latest version.

For developers, the latest source code is available from our SVN repository.

How to use ms-data-core-api
===============

# Using ms-data-core-api 

### Reading a mzIdentML file:

This example shows how to read an mzIdentML file and retrieve the information from them:

```java
import uk.ac.ebi.pride.utilities.data.core.*;

//Open an inputFile mzIdentml File using memory 
MzIdentMLControllerImpl mzIdentMlController = new MzIdentMLControllerImpl(inputFile, true);

//Print size of the Sample List
List<Sample> samples = mzIdentMlController.getSamples();
System.out.println(samples.size());

//Print the Id of the first sample
System.out.println(samples.get(0).getId());

// Get the list of softwares from the mzIdentMlController
List<Software> software = mzIdentMlController.getSoftwares();

//Print size of the Software List
System.out.println(software.size());

//Print the Name of the first Software
System.out.println(software.get(0).getName());

//Retrieve the Identification Metadata    
IdentificationMetaData experiment = mzIdentMlController.getIdentificationMetaData();
// test SearchDatabase
List<SearchDataBase> databases = experiment.getSearchDataBases();
        
// test SpectrumIdentificationProtocol
List<SpectrumIdentificationProtocol> spectrumIdentificationProtocol = experiment.getSpectrumIdentificationProtocols();

// Retrieve the Protein Identification Protocol
Protocol proteinDetectionProtocol = experiment.getProteinDetectionProtocol();

//Retrieve all Protein Identifications
List<Comparable> identifications = new ArrayList<Comparable>(mzIdentMlController.getProteinIds());
```

### Reading a PRIDE XML file:

This example shows how to read an PRIDE XML file and retrieve the information from them:

```java
//Open an inputFile mzIdentml File using memory 
PrideXmlControllerImpl prideXMLController = new PrideXmlControllerImpl(inputFile);

// You can use the example above and the same functions to retrieve the data using this controller for example:
//Print size of the Sample List
List<Sample> samples = prideXMLController.getSamples();
System.out.println(samples.size());
```


### Using tools in ms-data-core-api:

#### File format conversion

Convert from mzIdentML to mzTab
```java
java -jar ms-data-core-api<version>.jar -c -mzid <input.mzid> -outputfile <output.mztab>
```

Convert from PRIDE XML to mzTab
```java
java -jar ms-data-core-api<version>.jar -c -pridexml <pride.xml> -outputformat <output.mztab>
```

Convert from annotated mzTab to (sorted, filtered*) proBed
```java
java -jar ms-data-core-api<version>.jar -c -mztab <input.mztab> -chromsizes <chrom.txt> -outputformat probed
```

Convert from annotated mzIdentML to (sorted, filtered*) proBed
```java
java -jar ms-data-core-api<version>.jar -c -mzid <input.mztab> -chromsizes <chrom.txt> -outputformat probed
```

Convert from (sorted, filtered*) proBed to bigBed
```java
java -jar ms-data-core-api<version>.jar -c -mztab <input.pro.bed> -chromsizes <chrom.txt> -asqlfile <aSQL.as> -bigbedconverter <bedToBigBed>
```

#### File Validation

MzIdentML validation
```java
java -jar ms-data-core-api<version>.jar -v -mzid <sample.mzid> -peak <spectra.mgf> -skipserialization -reportfile <outputReport.txt>
```

MzTab validation
```java
java -jar ms-data-core-api<version>.jar -v -mztab <input.mztab> -peaks <spectra1.mgf>##<spectra2.mgf> -skipserialization -reportfile <outputReport.txt>
```

PRIDE XML validation
```java
java -jar ms-data-core-api<version>.jar -v -pridexml <input.pride.xml> -skipserialization -reportfile <outputReport.txt>
```

#### XML schema validation

MzIdentML schema validation and normal validation
```java
java -jar ms-data-core-api<version>.jar -v -mzid <input.mzid> -peak <spectra.mgf> -scehma -skipserialization -reportfile <outputReport.txt>
```

PRIDE XML schema validation only, without normal validation
```java
java -jar ms-data-core-api<version>.jar -v -pridexml <input.pride.xml> -schemaonly -skipserialization -reportfile <outputReport.txt>
```

#### ProBed validation

ProBed validation with the default schema
```java
java -jar ms-data-core-api<version>.jar -v -proBed <input.pro.bed> -reportfile <outputReport.txt>
```

ProBed validation with a custom schema
```java
java -jar ms-data-core-api<version>.jar -v -proBed -proBed <input.pro.bed> -asqlfile <input.as> -reportfile <outputReport.txt>
```

#### Miscellaneous

Check Results Files
```java
java -jar ms-data-core-api<version>.jar -check -inputfile <inputfile>
```

Convert PRIDE or mzIdentML file to MzTab
```java
java -jar ms-data-core-api<version>.jar -convert -inputfile <inputfile> -format <format>
```

Print Error/Warn detail message based on code
```java
java -jar ms-data-core-api<version>.jar -error -code <code>
```

Help
```java
java -jar ms-data-core-api<version>.jar -h or --help 
```

# How to cite it:

 * Perez-Riverol Y, Uszkoreit J, Sanchez A, Ternent T, Del Toro N, Hermjakob H, Vizcaíno JA, Wang R. (2015). ms-data-core-api: An open-source, metadata-oriented library for computational proteomics. Bioinformatics. 2015 Apr 24. [PDF File](http://www.ncbi.nlm.nih.gov/pubmed/25910694) [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/25910694)


# This library has been used in:

* Wang, R., Fabregat, A., Ríos, D., Ovelleiro, D., Foster, J. M., Côté, R. G., ... & Vizcaíno, J. A. (2012). PRIDE Inspector: a tool to visualize and validate MS proteomics data. Nature biotechnology, 30(2), 135-137. [PDF File](http://www.nature.com/nbt/journal/v30/n2/pdf/nbt.2112.pdf), [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/22318026)
* Vizcaíno, J. A., Côté, R. G., Csordas, A., Dianes, J. A., Fabregat, A., Foster, J. M., ... & Hermjakob, H. (2013). The PRoteomics IDEntifications (PRIDE) database and associated tools: status in 2013. Nucleic acids research, 41(D1), D1063-D1069. [PRIDE-Archive](http://www.ebi.ac.uk/pride/archive/)

# Getting Help

If you have questions or need additional help, please contact the PRIDE Helpdesk at the EBI: pride-support at ebi.ac.uk (replace at with @).

Please send us your feedback, including error reports, improvement suggestions, new feature requests and any other things you might want to suggest to the PRIDE team.

# License

ms-data-core-api is a PRIDE API licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).


