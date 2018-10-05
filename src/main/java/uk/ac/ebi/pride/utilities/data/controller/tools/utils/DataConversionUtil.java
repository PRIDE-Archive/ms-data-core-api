package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.person.Title;
import uk.ac.ebi.pride.archive.repo.assay.*;
import uk.ac.ebi.pride.archive.repo.assay.Contact;
import uk.ac.ebi.pride.archive.repo.assay.instrument.InstrumentComponent;
import uk.ac.ebi.pride.archive.repo.assay.instrument.InstrumentComponentCvParam;
import uk.ac.ebi.pride.archive.repo.assay.instrument.InstrumentComponentUserParam;
import uk.ac.ebi.pride.archive.repo.assay.software.SoftwareCvParam;
import uk.ac.ebi.pride.archive.repo.assay.software.SoftwareUserParam;
import uk.ac.ebi.pride.archive.repo.project.*;
import uk.ac.ebi.pride.data.model.CvParam;
import uk.ac.ebi.pride.data.model.Param;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.util.*;

/**
 * Converts various data models for validation purposes.
 *
 * @author Tobias Ternent
 */
public final class DataConversionUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataConversionUtil.class);

    /**
     * Combine a list of strings to a single string
     *
     * @param strToCombine strings to be combined
     * @param separator    string separator
     *
     * @return the combined String
     */
    public static String combineToString(Set<String> strToCombine, String separator) {
        StringBuilder builder = new StringBuilder();
        if (strToCombine != null) {
            for (String s : strToCombine) {
                builder.append(s);
                builder.append(separator);
            }
        }
        // remove the last the separator
        String str = builder.toString();
        if (strToCombine != null && strToCombine.size() > 0) {
            str = str.substring(0, str.length() - separator.length());
        }
        return str;
    }


    public static Collection<AssaySampleCvParam> convertAssaySampleCvParams(Set<CvParam> sampleParams) {
        Collection<AssaySampleCvParam> retval = new HashSet<>();
        retval.addAll(convertAssayCvParams(AssaySampleCvParam.class, sampleParams));
        return retval;
    }

    public static Collection<AssayQuantificationMethodCvParam> convertAssayQuantitationMethodCvParams(Set<CvParam> sampleParams) {
        Collection<AssayQuantificationMethodCvParam> retval = new HashSet<>();
        retval.addAll(convertAssayCvParams(AssayQuantificationMethodCvParam.class, sampleParams));
        return retval;
    }


    @SuppressWarnings("unchecked")
    private static Collection convertAssayCvParams(Class clz, Set<? extends Param> sampleParams) {
        try {
            Collection<AssayCvParam> retval = new HashSet<>();
            if (sampleParams != null) {
                for (Param param : sampleParams) {
                    if (param instanceof CvParam) {
                        CvParam cvParam = (CvParam) param;
                        uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);

                        AssayCvParam acvParam = (AssayCvParam) clz.newInstance();
                        acvParam.setCvParam(repoParam);
                        acvParam.setValue(cvParam.getValue());
                        retval.add(acvParam);
                    } else {
                        logger.info("Ignored sample userParam " + param.getName() + "->" + param.getValue());
                    }
                }
            }
            return retval;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Error creating cv param.", e);
        }

    }

    private static uk.ac.ebi.pride.archive.repo.param.CvParam createCvParam(CvParam cvParam) {
        uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = new uk.ac.ebi.pride.archive.repo.param.CvParam();
        repoParam.setCvLabel(cvParam.getCvLabel());
        repoParam.setAccession(cvParam.getAccession());
        repoParam.setName(cvParam.getName());
        return repoParam;
    }


    public static Collection<ProjectExperimentType> convertProjectExperimentTypeCvParams(Project project, Set<CvParam> params) {
        Collection<ProjectExperimentType> retval = new HashSet<>();
        retval.addAll(convertProjectCvParams(project, ProjectExperimentType.class, params));
        return retval;
    }

    public static Collection<ProjectSampleCvParam> convertProjectSampleCvParams(Project project, Set<CvParam> params) {
        Collection<ProjectSampleCvParam> retval = new HashSet<>();
        retval.addAll(convertProjectCvParams(project, ProjectSampleCvParam.class, params));
        return retval;
    }

    public static Collection<ProjectGroupCvParam> convertProjectGroupCvParams(Project project, Set<Param> params) {
        Collection<ProjectGroupCvParam> retval = new HashSet<>();
        retval.addAll(convertProjectCvParams(project, ProjectGroupCvParam.class, params));
        return retval;
    }

    public static Collection<ProjectGroupUserParam> convertProjectGroupUserParams(Project project, Set<Param> additional) {
        Collection<ProjectGroupUserParam> retval = new HashSet<>();
        if (additional != null) {
            for (Param param : additional) {
                if (!(param instanceof CvParam)) {
                    ProjectGroupUserParam userParam = new ProjectGroupUserParam();
                    userParam.setName(param.getName());
                    userParam.setValue(param.getValue());
                    userParam.setProject(project);
                    retval.add(userParam);
                } //don't process cv params
            }
        }
        return retval;
    }


    public static Collection<ProjectQuantificationMethodCvParam> convertProjectQuantificationMethodCvParams(Project project, Set<CvParam> params) {
        Collection<ProjectQuantificationMethodCvParam> retval = new HashSet<>();
        retval.addAll(convertProjectCvParams(project, ProjectQuantificationMethodCvParam.class, params));
        return retval;
    }


    @SuppressWarnings("unchecked")
    private static Collection convertProjectCvParams(Project project, Class clz, Set<? extends Param> projectParams) {
        try {
            Collection<ProjectCvParam> retval = new HashSet<>();
            if (projectParams != null) {
                for (Param param : projectParams) {
                    if (param instanceof CvParam) {
                        CvParam cvParam = (CvParam) param;
                        uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);

                        ProjectCvParam projectCvParam = (ProjectCvParam) clz.newInstance();
                        projectCvParam.setProject(project);
                        projectCvParam.setCvParam(repoParam);
                        projectCvParam.setValue(cvParam.getValue());
                        retval.add(projectCvParam);
                    } else {
                        logger.info("Ignored project userParam" + param.getName() + "->" + param.getValue());
                    }
                }
            }
            return retval;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Error creating cv param.", e);
        }

    }

    public static Collection<uk.ac.ebi.pride.archive.repo.assay.software.Software> convertSoftware(Set<Software> softwares) {

        Set<uk.ac.ebi.pride.archive.repo.assay.software.Software> softwareSet = new HashSet<>();
        int orderIndex = 0;
        if (softwares != null) {
            for (Software oldSoftware : softwares) {
                uk.ac.ebi.pride.archive.repo.assay.software.Software newSoftware = new uk.ac.ebi.pride.archive.repo.assay.software.Software();
                newSoftware.setName(oldSoftware.getName());
                newSoftware.setOrder(orderIndex++);
                newSoftware.setSoftwareCvParams(convertSoftwareCvParams(newSoftware, oldSoftware.getCvParams()));
                newSoftware.setSoftwareUserParams(convertSoftwareUserParams(newSoftware, oldSoftware.getUserParams()));
                newSoftware.setVersion(oldSoftware.getVersion());
                newSoftware.setCustomization(oldSoftware.getCustomization());
                softwareSet.add(newSoftware);
            }
        }

        return softwareSet;

    }

    private static Collection<SoftwareUserParam> convertSoftwareUserParams(uk.ac.ebi.pride.archive.repo.assay.software.Software software,
                                                                           List<UserParam> userParams) {
        Collection<SoftwareUserParam> retval = new HashSet<>();
        if (userParams != null) {
            for (UserParam userParam : userParams) {
                SoftwareUserParam swuPavam = new SoftwareUserParam();
                swuPavam.setName(userParam.getName());
                swuPavam.setValue(userParam.getValue());
                swuPavam.setSoftware(software);
                retval.add(swuPavam);
            }
        }
        return retval;
    }

    private static Collection<SoftwareCvParam> convertSoftwareCvParams(uk.ac.ebi.pride.archive.repo.assay.software.Software software,
                                                                       List<uk.ac.ebi.pride.utilities.data.core.CvParam> cvParams) {
        Collection<SoftwareCvParam> retval = new HashSet<>();
        if (cvParams != null) {
            for (uk.ac.ebi.pride.utilities.data.core.CvParam cvParam : cvParams) {
                uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);
                SoftwareCvParam swcvPavam = new SoftwareCvParam();
                swcvPavam.setSoftware(software);
                swcvPavam.setCvParam(repoParam);
                swcvPavam.setValue(cvParam.getValue());
                retval.add(swcvPavam);
            }
        }
        return retval;

    }

    private static uk.ac.ebi.pride.archive.repo.param.CvParam createCvParam(uk.ac.ebi.pride.utilities.data.core.CvParam cvParam) {
        uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = new uk.ac.ebi.pride.archive.repo.param.CvParam();
        repoParam.setCvLabel(cvParam.getCvLookupID());
        repoParam.setName(cvParam.getName());
        repoParam.setAccession(cvParam.getAccession());
        return repoParam;
    }

    public static Collection<InstrumentComponentCvParam> convertInstrumentComponentCvParam(InstrumentComponent instrumentComponent,
                                                                                           List<uk.ac.ebi.pride.utilities.data.core.CvParam> cvParams) {
        Collection<InstrumentComponentCvParam> retval = new HashSet<>();
        if (cvParams != null) {
            for (uk.ac.ebi.pride.utilities.data.core.CvParam cvParam : cvParams) {
                uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);
                InstrumentComponentCvParam iccvParam = new InstrumentComponentCvParam();
                iccvParam.setInstrumentComponent(instrumentComponent);
                iccvParam.setCvParam(repoParam);
                iccvParam.setValue(cvParam.getValue());
                retval.add(iccvParam);
            }
        }
        return retval;
    }


    public static Collection<InstrumentComponentUserParam> convertInstrumentComponentUserParam(InstrumentComponent instrumentComponent,
                                                                                               List<UserParam> userParams) {
        Collection<InstrumentComponentUserParam> retval = new HashSet<>();
        if (userParams != null) {
            for (UserParam userParam : userParams) {
                InstrumentComponentUserParam icuserParam = new InstrumentComponentUserParam();
                icuserParam.setInstrumentComponent(instrumentComponent);
                icuserParam.setName(userParam.getName());
                icuserParam.setValue(userParam.getValue());
                retval.add(icuserParam);
            }
        }
        return retval;
    }


    public static Collection<AssayPTM> convertAssayPTMs(Set<uk.ac.ebi.pride.utilities.data.core.CvParam> ptms) {
        Set<AssayPTM> retval = new HashSet<>();
        if (ptms != null) {
            for (uk.ac.ebi.pride.utilities.data.core.CvParam cvParam : ptms) {
                uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);
                AssayPTM aPTM = new AssayPTM();
                aPTM.setCvParam(repoParam);
                aPTM.setValue(cvParam.getValue());
                retval.add(aPTM);
            }
        }
        return retval;
    }

    public static Collection<ProjectInstrumentCvParam> convertProjectInstruments(Project project, Set<CvParam> instruments) {
        Set<ProjectInstrumentCvParam> retval = new HashSet<>();
        if (instruments != null) {
            for (CvParam cvParam : instruments) {
                uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);
                ProjectInstrumentCvParam projectInstrument = new ProjectInstrumentCvParam();
                projectInstrument.setProject(project);
                projectInstrument.setCvParam(repoParam);
                projectInstrument.setValue(cvParam.getValue());
                retval.add(projectInstrument);
            }
        }
        return retval;

    }

    public static Collection<ProjectPTM> convertProjectPTMs(Project project, Set<CvParam> ptms) {
        Set<ProjectPTM> retval = new HashSet<>();
        if (ptms != null) {
            for (CvParam cvParam : ptms) {
                uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);
                ProjectPTM projectPTM = new ProjectPTM();
                projectPTM.setProject(project);
                projectPTM.setCvParam(repoParam);
                projectPTM.setValue(cvParam.getValue());
                retval.add(projectPTM);
            }
        }
        return retval;
    }

    public static ProjectSampleCvParam convertAssaySampleToProjectSample(Project project, AssaySampleCvParam sample) {
        ProjectSampleCvParam retval = new ProjectSampleCvParam();
        retval.setCvParam(sample.getCvParam());
        retval.setValue(sample.getValue());
        retval.setProject(project);
        return retval;

    }

    public static ProjectPTM convertAssayPTMtoProjectPTM(Project project, AssayPTM ptm) {
        ProjectPTM projectPTM = new ProjectPTM();
        projectPTM.setProject(project);
        projectPTM.setCvParam(ptm.getCvParam());
        projectPTM.setValue(ptm.getValue());
        return projectPTM;
    }

    public static ProjectQuantificationMethodCvParam convertAssayQuantitationMethodToProjectQuantitationMethod(Project project, AssayQuantificationMethodCvParam param) {
        ProjectQuantificationMethodCvParam method = new ProjectQuantificationMethodCvParam();
        method.setCvParam(param.getCvParam());
        method.setValue(param.getValue());
        method.setProject(project);
        return method;
    }

    public static Collection<Contact> convertContact(Collection<Person> personContacts) {
        Set<Contact> retval = new HashSet<>();
        if (personContacts != null) {
            for (Person person : personContacts) {
                Contact contact = new Contact();
                contact.setTitle(Title.UNKNOWN);
                if (person.getFirstname() != null && person.getLastname() != null) {
                    StringBuilder sb = new StringBuilder(person.getFirstname());
                    if (person.getMidInitials() != null) {
                        sb.append(" ").append(person.getMidInitials());
                    }
                    contact.setFirstName(sb.toString());
                    contact.setLastName(person.getLastname());
                } else if (person.getName() != null) {
                    //try to split the name on whitespace
                    String[] tokens = person.getName().split("\\s+");
                    if (tokens.length > 1) {
                        //put everything in first name except last token
                        String lastName = tokens[tokens.length - 1];
                        tokens[tokens.length - 1] = "";
                        StringBuilder sb = new StringBuilder();
                        for (String token : tokens) {
                            sb.append(token).append(" ");
                        }
                        contact.setFirstName(sb.toString().trim());
                        contact.setLastName(lastName);
                    } else {
                        contact.setFirstName(tokens[0]);
                        //set to blank string so that db doesn't barf
                        contact.setLastName(" ");
                    }
                } else {
                    //can't find a proper name for the contact, ignore it
                    logger.warn("No name given for contact: " + person.toString());
                    continue;
                }
                //email
                String email = person.getContactInfo();
                if (email == null || "".equals(email.trim())) {

                    //contact info might be in params
                    for (uk.ac.ebi.pride.utilities.data.core.CvParam param : person.getCvParams()) {
                        if (param.getAccession().equals(Utility.MS_CONTACT_EMAIL_AC)) {
                            //found it
                            email = param.getValue();
                            break;
                        }
                    }
                    //can't find a proper name for the contact, ignore it
                    if (email == null || "".equals(email.trim())) {
                        logger.warn("No email given for contact: " + person.toString());
                        continue;
                    }
                }
                contact.setEmail(email);
                //affiliations
                StringBuilder sb = new StringBuilder();
                for (Organization org : person.getAffiliation()) {
                  if (org != null) {
                    if (org.getName() != null) {
                      sb.append(org.getName()).append(", ");
                    }
                    if (org.getMail() != null) {
                      sb.append(org.getMail()).append(", ");
                    }
                  }
                }
                //remove last 2 chars
                String affiliation = sb.toString().trim();
                if (sb.length() > 0) {
                    affiliation = affiliation.substring(0, affiliation.length() - 1);
                }
                if ("".equals(affiliation)) {
                    logger.warn("No affiliation given for contact: " + person.toString());
                    continue;
                }
                contact.setAffiliation(affiliation);
                retval.add(contact);
            }
        }
        return retval;
    }

    public static Collection<AssayGroupCvParam> convertAssayGroupCvParams(ParamGroup additional) {
        Set<AssayGroupCvParam> retval = new HashSet<>();
        if (additional != null) {
            for (uk.ac.ebi.pride.utilities.data.core.CvParam cvParam : additional.getCvParams()) {
                uk.ac.ebi.pride.archive.repo.param.CvParam repoParam = createCvParam(cvParam);
                AssayGroupCvParam agcvParam = new AssayGroupCvParam();
                agcvParam.setCvParam(repoParam);
                agcvParam.setValue(cvParam.getValue());
                retval.add(agcvParam);
            }
        }
        return retval;
    }

    public static Collection<AssayGroupUserParam> convertAssayGroupUserParams(ParamGroup additional) {
        Set<AssayGroupUserParam> retval = new HashSet<>();
        if (additional != null) {
            for (UserParam userParam : additional.getUserParams()) {
                AssayGroupUserParam auserParam = new AssayGroupUserParam();
                auserParam.setName(userParam.getName());
                auserParam.setValue(userParam.getValue());
                retval.add(auserParam);
            }
        }
        return retval;
    }

    public static Collection<ProjectTag> convertProjectTags(Project project, Set<String> tags) {
        List<ProjectTag> projectTags = new ArrayList<>();
        for (String tag : tags) {
            ProjectTag projectTag = new ProjectTag();
            projectTag.setProject(project);
            projectTag.setTag(tag);
            projectTags.add(projectTag);
        }
        return projectTags;
    }

    public static LabHead convertLabHead(Project project, uk.ac.ebi.pride.data.model.Contact labHeadContact) {
        LabHead labHead = new LabHead();
        labHead.setProject(project);
        labHead.setTitle(Title.Dr); // default title for lab head is Dr
        //try to split the name on whitespace
        String[] tokens = labHeadContact.getName().split("\\s+");
        if (tokens.length > 1) {
            //put everything in first name except last token
            String lastName = tokens[tokens.length - 1];
            tokens[tokens.length - 1] = "";
            StringBuilder sb = new StringBuilder();
            for (String token : tokens) {
                sb.append(token).append(" ");
            }
            labHead.setFirstName(sb.toString().trim());
            labHead.setLastName(lastName);
        } else {
            labHead.setFirstName(tokens[0]);
            //set to blank string so that db doesn't barf
            labHead.setLastName(" ");
        }
        String email = labHeadContact.getEmail();
        if (email == null || "".equals(email.trim())) {
            logger.warn("No email given for labHead: " + labHeadContact.toString());
        }
        labHead.setEmail(email);
        String affiliation = labHeadContact.getAffiliation();
        if (affiliation == null || "".equals(affiliation)) {
            logger.warn("No affiliation given for labHead: " + labHeadContact.toString());
        }
        labHead.setAffiliation(affiliation);
        return labHead;
    }
}
