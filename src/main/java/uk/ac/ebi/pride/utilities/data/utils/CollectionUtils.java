package uk.ac.ebi.pride.utilities.data.utils;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A utility class to enhance Collections class in JDK
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 * Date: 16-Apr-2010
 * Time: 11:05:42
 */
public final class CollectionUtils {

    /**
     * Private Constructor
     */
    private CollectionUtils() {

    }

    /**
     * Get the index of a element in a collection
     *
     * @param collection collection to search
     * @param element    element to search for
     * @return int  element index in the collection
     */
    public static <T> int getIndex(Collection<T> collection, T element) {
        int index = -1;

        if (collection != null) {
            int count = 0;

            for (T entry : collection) {
                if (entry.equals(element)) {
                    index = count;
                }

                count++;
            }
        }

        return index;
    }

    public static <T> T getElement(Collection<T> collection, int index) {
        T element = null;

        if (collection != null) {
            int count = 0;

            for (T entry : collection) {
                if (count == index) {
                    element = entry;
                }

                count++;
            }
        }

        return element;
    }

    public static <T> T getLastElement(Collection<T> collection) {
        return getElement(collection, collection.size() - 1);
    }

    public static <T> List<T> createListFromList(Collection<T> collection) {
        List<T> newList = new ArrayList<T>();

        if (collection != null) {
            newList.addAll(collection);
        }

        return newList;
    }

    public static <T> void replaceValuesInCollection(Collection<T> from, Collection<T> to) {
        to.clear();
        if (from != null) {
            to.addAll(from);
        }
    }

    public static <T> List<T> createEmptyList() {
        return Collections.emptyList();
    }
}



