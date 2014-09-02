ms-data-core-api
===============

# About mz-data-core-api

The primary purpose of ms-data-core-api library is to provide commonly used classes and Object Model for Proteomics Experiments. You may also find it useful for your own computational proteomics projects.

# How to cite it:

 * Wang, R., Fabregat, A., Ríos, D., Ovelleiro, D., Foster, J. M., Côté, R. G., ... & Vizcaíno, J. A. (2012). PRIDE Inspector: a tool to visualize and validate MS proteomics data. Nature biotechnology, 30(2), 135-137. [PDF File](http://www.nature.com/nbt/journal/v30/n2/pdf/nbt.2112.pdf), [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/22318026)
 * Perez-Riverol, Y., Wang, R., Hermjakob, H., Müller, M., Vesada, V., & Vizcaíno, J. A. (2014). Open source libraries and frameworks for mass spectrometry based proteomics: A developer's perspective. Biochimica et Biophysica Acta (BBA)-Proteins and Proteomics, 1844(1), 63-76. [PDF File](http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3898926/) [Pubmed Record](http://www.ncbi.nlm.nih.gov/pubmed/23467006)

# Main Features
* Data Structures to represent AminoAcids, Mass Table, etc.
* Functions to compute different Properties such as isoelectric point, retention time, sequence length and masses.
* Reference table of the main Ontology terms used in PRIDE-toolsuite. 
* General functionalities and data structures classes missing in JDK, such as: Tuple.
* General functionalities like checking email format and availability.

# The library provides four key modules:

* mol: contains classes describing entities at the molecular level, such as: amino acids, neutrual losses, peptides and fragment ions.
* gui: contains several GUI components, you can use them if you want to replicate some of the features in PRIDE Inspector.
* data: contains data structures classes are missing from JDK, such as: Tuple.
* util: contains a selection of convenient classes. For examples: for formatting protein related informations, for checking Internet availability or for verify email addresses.

**Note**: the library is still evolving, we are committed to expand this library and add more useful classes.

# Getting ms-data-core-api

The zip file in the releases section contains the PRIDE Utilities jar file and all other required libraries.

Maven Dependency

PRIDE Utilities library can be used in Maven projects, you can include the following snippets in your Maven pom file.
 
 ```maven
 <dependency>
   <groupId>uk.ac.ebi.pride.utilities</groupId>
   <artifactId>ms-data-core-api</artifactId>
   <version>0.1.23-SNAPSHOT</version>
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

Here we will show you how to use the ms-data-core-api library to calculate m/z delta and calculate theoretical mass of a given peptide.

### Calculate m/z Delta:

You can find the method for calculating m/z delta from MoleculeUtilities in uk.ac.ebi.pride.mol package. It requires four input parameters:

```java 
/*
 * sequence is the peptide sequence in String,
 * precursorMz is the precusor m/z in double,
 * precursorCharge is the precursor charge in double,
 *ptmMasses is a list of post translational modifications in double.
*/

// Direct call on the method
Double mzDelta = MolecularUtilitites.calculateDeltaMz(sequence, precursorMz, precursorCharge, ptmMasses);
```

### Calculate Theoretical Mass

You can also find the method for calculating theoretical mass value from MoleculeUtilities. It needs two input parameters:

```java
 /*
  *sequence is the peptide sequence in String,  
  *masses is a optional list array of masses you want to add as extras.
  The following lines of code shows you how:
 */

 // Direct call on the method
 double result = MolecularUtilitites.calculateTheoreticalMass(sequence, masses);

 // Tip: Take a close look at other methods within MoleculeUtilitites, you might find them useful. 
```
