ms-data-core-api
===============
[![Build Status](https://travis-ci.org/PRIDE-Utilities/ms-data-core-api.svg)](https://travis-ci.org/PRIDE-Utilities/ms-data-core-api)

# About mz-data-core-api

The primary purpose of ms-data-core-api library is to provide commonly used classes and Object Model for Proteomics Experiments. You may also find it useful for your own computational proteomics projects.

# License

ms-data-core-api is a PRIDE API licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

# How to cite it:

 * Wang, R., Fabregat, A., Ríos, D., Ovelleiro, D., Foster, J. M., Côté, R. G., ... & Vizcaíno, J. A. (2012). PRIDE Inspector: a tool to visualize and validate MS proteomics data. Nature biotechnology, 30(2), 135-137. [PDF File](http://www.nature.com/nbt/journal/v30/n2/pdf/nbt.2112.pdf), [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/22318026)
 * Perez-Riverol, Y., Wang, R., Hermjakob, H., Müller, M., Vesada, V., & Vizcaíno, J. A. (2014). Open source libraries and frameworks for mass spectrometry based proteomics: A developer's perspective. Biochimica et Biophysica Acta (BBA)-Proteins and Proteomics, 1844(1), 63-76. [PDF File](http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3898926/) [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/23467006)

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
     <id>nexus-ebi-repo</id>
     <url>http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo</url>
 </repository>
 
 <!-- EBI SNAPSHOT repo -->
 <snapshotRepository>
    <id>nexus-ebi-repo-snapshots</id>
    <url>http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo-snapshots</url>
 </snapshotRepository>
```
Note: you need to change the version number to the latest version.

For developers, the latest source code is available from our SVN repository.

# Getting Help

If you have questions or need additional help, please contact the PRIDE Helpdesk at the EBI: pride-support at ebi.ac.uk (replace at with @).

Please send us your feedback, including error reports, improvement suggestions, new feature requests and any other things you might want to suggest to the PRIDE team.

# This library has been used in:

* Wang, R., Fabregat, A., Ríos, D., Ovelleiro, D., Foster, J. M., Côté, R. G., ... & Vizcaíno, J. A. (2012). PRIDE Inspector: a tool to visualize and validate MS proteomics data. Nature biotechnology, 30(2), 135-137. [PDF File](http://www.nature.com/nbt/journal/v30/n2/pdf/nbt.2112.pdf), [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/22318026)
* Vizcaíno, J. A., Côté, R. G., Csordas, A., Dianes, J. A., Fabregat, A., Foster, J. M., ... & Hermjakob, H. (2013). The PRoteomics IDEntifications (PRIDE) database and associated tools: status in 2013. Nucleic acids research, 41(D1), D1063-D1069. [PRIDE-Archive](http://www.ebi.ac.uk/pride/archive/)

How to use ms-data-core-api
===============

# Using ms-data-core-api 

### Reading a mzIdentML file:

This example shows how to read an mzIdentML file and retrieve the information from them:

```java
//Open an inputFile mzIdentml File using memory 
MzIdentMLControllerImpl mzIdentMlController = new MzIdentMLControllerImpl(inputFile, true);

//Print size of the Sample List
List<Sample> samples = mzIdentMlController.getSamples();
System.out.println(samples.size());

//Print the Id of the first sample
System.out.println(samples.get(0).getId());

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


