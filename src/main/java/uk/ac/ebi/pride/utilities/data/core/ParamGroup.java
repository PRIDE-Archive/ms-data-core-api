package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ParamGroup is a container for CvParams and UserParams.
 * This class is included in neither mzML nor PRIDE xml definition.
 * In theory, both cv params and user params can be null.
 * This container is extended for some several classes that contain
 * cvParameters as information.
 * <p/>
 * @author Yasset Perez-Riverol, Rui Wang
 * Date: 25-Jan-2010
 * Time: 15:58:01
 */
public class ParamGroup implements MassSpecObject{

    /**
     * List of cv paramemters
     */
    private List<CvParam> cvParams = new ArrayList<CvParam>();

    /**
     * List of user parameters
     */
    private List<UserParam> userParams = new ArrayList<UserParam>();

    /**
     * Constructor creates an empty param group
     */
    public ParamGroup() {
    }

    public ParamGroup(ParamGroup params) {
        if (params != null) {
            this.cvParams.addAll(params.getCvParams());
            this.userParams.addAll(params.getUserParams());
        }
    }

    public ParamGroup(CvParam cvParam, UserParam userParam) {
        if (cvParam != null) {
            cvParams.add(cvParam);
        }

        if (userParam != null) {
            userParams.add(userParam);
        }
    }

    /**
     * Constructor
     *
     * @param cvParams   optional.
     * @param userParams optional.
     */
    public ParamGroup(List<CvParam> cvParams, List<UserParam> userParams) {
        this.cvParams = CollectionUtils.createListFromList(cvParams);
        this.userParams = CollectionUtils.createListFromList(userParams);
    }

    /**
     * This method return a List of CvParam, the result List in a new Instance
     * of the current List of CvParam
     *
     * @return List of CvParam
     */
    public List<CvParam> getCvParams() {
        return this.cvParams;
    }

    public void setCvParams(List<CvParam> cvs) {
        CollectionUtils.replaceValuesInCollection(cvs, this.cvParams);
    }

    public void addCvParam(CvParam cv) {
        this.cvParams.add(cv);
    }

    public void addCvParams(List<CvParam> cvs) {
        this.cvParams.addAll(cvs);
    }

    public void removeCvParam(CvParam cv) {
        this.cvParams.remove(cv);
    }

    public void removeCvParams(List<CvParam> cvs) {
        this.cvParams.removeAll(cvs);
    }

    public List<UserParam> getUserParams() {
        return this.userParams;
    }

    public void setUserParams(List<UserParam> ups) {
        CollectionUtils.replaceValuesInCollection(ups, this.userParams);
    }

    public void addUserParam(UserParam up) {
        this.userParams.add(up);
    }

    public void addUserParams(List<UserParam> ups) {
        this.userParams.addAll(ups);
    }

    public void removeUserParam(UserParam up) {
        userParams.remove(up);
    }

    public void removeUserParams(List<UserParam> ups) {
        userParams.removeAll(ups);
    }

    public boolean isEmpty() {
        return !cvParams.isEmpty() && !userParams.isEmpty();
    }

    @Override
    public String toString() {
        return "ParamGroup{" +
                "cvParams=" + cvParams +
                ", userParams=" + userParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamGroup)) return false;

        ParamGroup that = (ParamGroup) o;

        return cvParams.equals(that.cvParams) && userParams.equals(that.userParams);

    }

    @Override
    public int hashCode() {
        int result = cvParams.hashCode();
        result = 31 * result + userParams.hashCode();
        return result;
    }
}



