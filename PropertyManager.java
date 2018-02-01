package com.jci.car.bpm.common;

import java.util.*;

import com.documentum.fc.common.DfLogger;

/*
 *
 * This Class one Constants.properties file.
 * if it can't find a key in the properties
 * it returns null
 *
 */

/**
 * This class is used to read the property file and retreive the values of the properties
 * stored in the property file.
 */
public class PropertyManager {
    // The Private variables for this class

    private ResourceBundle bundle;
    private static Hashtable managers = new Hashtable();

    /*
    * Private Constructor ..Should not be initialized from outside
    */
    private PropertyManager(String packageName) {
        bundle = ResourceBundle.getBundle(packageName);
    }

    /**
     * Returns the manager for the given package
     *
     * @param packageName The name of the property file.
     * @return returns a PropertyManager object using which we can get the value of the properties.
     */

    public synchronized static PropertyManager getManager(String packageName) {

        PropertyManager manager = (PropertyManager) managers.get(packageName);
        if (manager == null) {
            manager = new PropertyManager(packageName);
            managers.put(packageName, manager);
        }

        return manager;
    }


    /**
     * Returns the value for the given key from the properties file.
     *
     * @param key The key whose valus whould be retrieved from the property file.
     * @return Returns the value of the key.
     */

    public String getString(String key) {
        return getString(key, true);
    }

    private String getString(String key, boolean trim) {
        if (key == null)
            return null;

        String value = null;

        try {
            value = bundle.getString(key);
        } catch (MissingResourceException mse) {
            value = null;
        }
        if (value != null && trim)
            value = value.trim();

        return value;
    }

    /*public static void main(String args[])
    {
            ConstantManager constManager = ConstantManager.getManager("D:\\Ford\\Sql\\");
            String value = constManager.getString("connectionURL");
            System.out.println("The Value: "+value);
    }*/


    /**
     * Replaces the tokens with the absolute values
     *
     * @param text        The text before the tokenization.
     * @param emailTokens A Hashtable where all the toekn names and their values are stored.
     * @return returns a string with all the tokens replaced.
     */
    public String replaceTokens(String text, Hashtable emailTokens) {

        if ((text == null) || (text.equals(""))) {
            return "";
        }
        DfLogger.debug(this, "Pre Process: " + text, null, null);
        Enumeration emailElements = emailTokens.elements();

        Iterator iterator = emailTokens.keySet().iterator();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = (String) emailTokens.get(key);
            DfLogger.debug(this, "Key: " + key + " Value: " + value, null, null);

            text = replaceString(text, key, value);
        }

        System.out.println("Post Process: " + text);

        return text;
    }

    /**
     * This method replaces part of a text in the string with the new text.
     *
     * @param target The target string where text has to be replaced
     * @param from   The text which has to be replaced with.
     * @param to     The new text that will be replaced with old text
     * @return returns a new string with the replaced string.
     */
    public static String replaceString(String target, String from, String to) {
        int start = target.indexOf(from);
        if (start == -1) return target;
        int lf = from.length();
        char [] targetChars = target.toCharArray();
        StringBuffer buffer = new StringBuffer();
        int copyFrom = 0;
        while (start != -1) {
            buffer.append(targetChars, copyFrom, start - copyFrom);
            buffer.append(to);
            copyFrom = start + lf;
            start = target.indexOf(from, copyFrom);
        }
        buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);
        return buffer.toString();
    }


}







