package com.thesett.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.google.common.io.Resources;
import com.thesett.util.string.StringUtils;

/**
 * ResourceUtils provides some helper methods for dealing with resources on the classpath/file system.
 */
public class ResourceUtils {
    /** Private constructor to prevent util class instantiation. */
    private ResourceUtils() {
    }

    /**
     * Provides the absolute file system path of a resource within the classpath.
     *
     * @param  resourceClassPathLocation The path within the classpath.
     *
     * @return The path translated into an absolute file system path.
     */
    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            URL resourceUrl = Resources.getResource(resourceClassPathLocation);
            URI resourceUri = resourceUrl.toURI();
            File file = new File(resourceUri);
            String path = file.getAbsolutePath();

            return path;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Scans the classpath for all resources that match a given regular expression pattern.
     *
     * @param  pattern The pattern to match.
     *
     * @return A list of matching resources.
     */
    public static List<String> getResources(String pattern, String packageName) {
        List<String> retval = new ArrayList<String>();
        String classPath = System.getProperty("java.class.path", ".");
        String[] classPathElements = classPath.split(":");

        for (String element : classPathElements) {
            retval.addAll(getResources(element, pattern, packageName));
        }

        return retval;
    }

    /**
     * Reads the contents of an input stream, one line at a time until the end of stream is encountered, and returns all
     * together as a string.
     *
     * @param  is The input stream.
     *
     * @return The contents of the reader.
     */
    public static String readStreamAsString(InputStream is) {
        try {
            byte[] data = new byte[4096];

            StringBuilder inBuffer = new StringBuilder();

            int read;

            while ((read = is.read(data)) != -1) {
                String s = new String(data, 0, read, Charset.defaultCharset());
                inBuffer.append(s);
            }

            return inBuffer.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Scans a named element of the classpath that matches a given regular expression.
     *
     * @param  element The classpath element to scan, this can be a jar file or a directory.
     * @param  pattern The pattern to match.
     *
     * @return A list of matching resources.
     */
    private static Collection<String> getResources(String element, String pattern, String packageName) {
        Collection<String> retval = new ArrayList<String>();
        File file = new File(element);

        if (file.isDirectory()) {
            // Allowing for the empty package, attempt to walk down directory matching the package being
            // searched for, to find the corresponding directory within the classpath.
            if (!"".equals(packageName)) {
                String[] splits = packageName.split("\\.");

                for (String split : splits) {
                    File[] files = file.listFiles();

                    boolean match = false;

                    for (File subDir : files) {
                        String name = subDir.getName();

                        if (name.equals(split)) {
                            file = subDir;
                            match = true;

                            break;
                        }
                    }

                    if (!match) {
                        return retval;
                    }
                }
            }

            retval.addAll(getResourcesFromDirectory(file, pattern, packageName));
        } else {
            retval.addAll(getResourcesFromJarFile(file, pattern, packageName));
        }

        return retval;
    }

    /**
     * Scans a jar file for resources that match a given regular expression.
     *
     * @param  file        The file to scan the resources of.
     * @param  pattern     The pattern to match.
     * @param  packageName
     *
     * @return A list of matching resources.
     */
    private static Collection<String> getResourcesFromJarFile(File file, String pattern,
        String packageName) {
        Pattern regexPattern = Pattern.compile(packageName + (StringUtils.nullOrEmpty(packageName) ? "" : "/") + pattern);
        Collection<String> retval = new ArrayList<String>();
        ZipFile zf;

        try {
            zf = new ZipFile(file);
        } catch (ZipException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Enumeration e = zf.entries();

        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String fileName = ze.getName();
            boolean accept = regexPattern.matcher(fileName).matches();

            if (accept) {
                retval.add(fileName);
            }
        }

        try {
            zf.close();
        } catch (IOException e1) {
            throw new IllegalStateException(e1);
        }

        return retval;
    }

    /**
     * Scans a directory for resources that match a given regular expression.
     *
     * @param  directory   The directory to scan.
     * @param  pattern     The pattern to match.
     * @param  packageName
     *
     * @return A list of matching resources.
     */
    private static Collection<String> getResourcesFromDirectory(File directory, String pattern, String packageName) {
        List<String> retval = new ArrayList<String>();
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            Pattern regexPattern = Pattern.compile(pattern);
            addFileToListIfMatches(regexPattern, file, retval, packageName);
        }

        return retval;
    }

    /**
     * Checks if a files full canonical name matches a regular expression, and adds it to a list of matches files iff it
     * does.
     *
     * @param pattern     The regular expression to match.
     * @param file        The file to check for a match against.
     * @param matches     The list of matches files to add to.
     * @param packageName
     */
    private static void addFileToListIfMatches(Pattern pattern, File file, Collection<String> matches,
        String packageName)
    {
        String fileName = file.getName();
        boolean accept = pattern.matcher(fileName).matches();

        if (accept)
        {
            // A slash is only added if there is a package name, to allow for the root package.
            matches.add(packageName + (StringUtils.nullOrEmpty(packageName) ? "" : "/") + fileName);
        }
    }
}
