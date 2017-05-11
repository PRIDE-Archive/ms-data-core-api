package uk.ac.ebi.pride.utilities.data.utils;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * This class contains general functions to validate Double or Integers
 *
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 10/05/2017.
 */
public class Utils {

    public static boolean isParsableAsDouble(final String s) {
        try {
            Double.valueOf(s);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
}
