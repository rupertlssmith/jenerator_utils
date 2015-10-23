package com.thesett.util.commands.refdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.thesett.util.resource.ResourceUtils;
import com.thesett.util.string.StringUtils;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class RefDataUtils {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(RefDataUtils.class.getName());

    /**
     * Scans a package on the classpath to find all .csv files that it contains.
     *
     * @param  refdataPackage The name of the package to scan.
     *
     * @return A list of fully qualified paths to all .csv files in the package.
     *
     * @throws RefDataLoadException If no reference data .csv files could be found.
     */
    public static List<String> findRefData(String refdataPackage) throws RefDataLoadException {
        // Iterate over all CSV files in the resource path.
        LOG.fine("Figuring out what CSV files are available.");

        List<String> resources = ResourceUtils.getResources(".*\\.csv", refdataPackage);

        if (resources.isEmpty()) {
            throw new RefDataLoadException("Could not find any .csv files to load from the classpath at '" +
                refdataPackage + "'.", "RDL", 1, null);
        }

        return resources;
    }

    /**
     * Parses all reference data .csv files into a set of resource bundles for loading into the database.
     *
     * @param  refdataPackage The name of the reference data package to read from.
     * @param  resources      A list of fully qualified resource paths to load .csv from.
     *
     * @return A set of reference data bundles.
     *
     * @throws RefDataLoadException If there is a problem reading or parsing the data.
     */
    public static Set<RefDataBundle> extractReferenceData(String refdataPackage, List<String> resources)
        throws RefDataLoadException {
        Set<RefDataBundle> refDataBundles = new LinkedHashSet<>();

        for (String resource : resources) {
            LOG.fine("Loading: " + resource);

            InputStream resourceAsStream = RefDataLoadCommand.class.getClassLoader().getResourceAsStream(resource);
            String csv = ResourceUtils.readStreamAsString(resourceAsStream);

            LOG.fine("Parsing: " + resource);

            // Work out the table name from the resource name.
            String tableName = resource.substring((refdataPackage + "/").length());
            tableName = tableName.substring(0, tableName.indexOf(".csv"));
            tableName = tableName.replaceAll("_", "");
            tableName += "_enumeration";

            String modelName = resource.substring((refdataPackage + "/").length());
            modelName = modelName.substring(0, modelName.indexOf(".csv"));
            modelName = StringUtils.toCamelCase(modelName);

            BufferedReader csvReader = new BufferedReader(new StringReader(csv));
            Map<Long, String> refDataMap = new LinkedHashMap<>();

            processLines(csvReader, refDataMap, resource);

            refDataBundles.add(new RefDataBundle(tableName, modelName, refDataMap));

            LOG.fine("Parsed: " + resource + " will insert " + refDataMap.size() + " rows to " + tableName);
        }

        return refDataBundles;
    }

    /**
     * Loads all reference data caches into memory, from their defining CSV files.
     *
     * <p/>An application will typically load reference data from database tables. This loader method loads the
     * reference data caches directly from the CSV files, and does not insert any values into a database. This may
     * typically be used to testing scenarios.
     *
     * <p/>The reference data items will also have their ids set, as per the CSV file.
     *
     * <p/>It is possible that prior to this method being called, some reference data items may already have been
     * created (by constructors and so on). For this reason, the enumerated attribute class is not dropped and re-built
     * from scratch, or existing items will be invalid. Instead any items not already in the attribute class are added,
     * and the ids on all items whether already existing or new, are correctly set.
     */
    public static void loadReferenceDataToCacheOnly() {
        try {
            List<String> resources = findRefData("refdata");
            Set<RefDataBundle> refDataBundles = extractReferenceData("refdata", resources);

            for (RefDataBundle refDataBundle : refDataBundles) {
                String enumName = refDataBundle.getTypeName();

                EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
                    EnumeratedStringAttribute.getFactoryForClass(enumName);

                // Get the set of already existing values, so that they will not be created again.
                factory.getType().getAllPossibleValuesIterator(false);

                Map<String, EnumeratedStringAttribute> existingValues =
                    factory.getType().getAllPossibleValuesMap(false);

                for (Map.Entry<Long, String> entry : refDataBundle.getDataMap().entrySet()) {
                    String value = entry.getValue();

                    EnumeratedStringAttribute attribute;

                    if (!existingValues.containsKey(value)) {
                        attribute = factory.createStringAttribute(value);
                    } else {
                        attribute = existingValues.get(value);
                    }

                    attribute.setId(entry.getKey());
                }
            }
        } catch (RefDataLoadException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Reads lines of CSV data using the supplied reader, and outputs (int, string) pairs of reference data items into
     * the supplied map.
     *
     * @param  csvReader  The buffered reader to read the CSV data as lines of text with.
     * @param  refDataMap A map to output (int, string) reference data pairs into.
     * @param  resource   The name of the reference data resource being parsed.
     *
     * @throws RefDataLoadException If there is an IO error reading the CSV data.
     */
    private static void processLines(BufferedReader csvReader, Map<Long, String> refDataMap, String resource)
        throws RefDataLoadException {
        String line;

        try {
            while ((line = csvReader.readLine()) != null) {
                // Skip any blank lines.
                if ("".equals(line.trim())) {
                    continue;
                }

                String idString = line.substring(0, line.indexOf(","));
                long id = Long.parseLong(idString);
                String value = line.substring(line.indexOf(",") + 1);
                value = value.replaceAll("\"", "");
                value = value.trim();

                refDataMap.put(id, value);
            }
        } catch (IOException e) {
            throw new RefDataLoadException("There was an IO error whilst reading resource " +
                resource + ".", "RDL", 8, e);
        }
    }
}
