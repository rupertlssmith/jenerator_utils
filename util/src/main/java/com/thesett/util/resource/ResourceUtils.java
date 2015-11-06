package com.thesett.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
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
    public static List<String> getResources(final String pattern, String packageName) {
        final List<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(":");

        for (final String element : classPathElements) {
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
        List<String> retval = new ArrayList<String>();
        File file = new File(element);

        if (file.isDirectory()) {
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
    private static Collection<String> getResourcesFromJarFile(final File file, final String pattern,
        String packageName) {
        Pattern regexPattern = Pattern.compile(packageName + "/" + pattern);
        final List<String> retval = new ArrayList<String>();
        ZipFile zf;

        try {
            zf = new ZipFile(file);
        } catch (final ZipException e) {
            throw new IllegalStateException(e);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        final Enumeration e = zf.entries();

        while (e.hasMoreElements()) {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = regexPattern.matcher(fileName).matches();

            if (accept) {
                retval.add(fileName);
            }
        }

        try {
            zf.close();
        } catch (final IOException e1) {
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

        for (final File file : fileList) {
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
    private static void addFileToListIfMatches(Pattern pattern, File file, List<String> matches, String packageName) {
        final String fileName = file.getName();
        final boolean accept = pattern.matcher(fileName).matches();

        if (accept) {
            matches.add(packageName + "/" + fileName);
        }
    }
}