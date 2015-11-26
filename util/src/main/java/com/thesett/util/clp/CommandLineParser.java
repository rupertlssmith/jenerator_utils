package com.thesett.util.clp;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandLineParser provides a utility for specifying the format of a command line and parsing command lines to ensure
 * that they fit their specified format. A command line is made up of flags and options, both may be refered to as
 * options. A flag is an option that does not take an argument (specifying it means it has the value 'true' and not
 * specifying it means it has the value 'false'). Options must take arguments but they can be set up with defaults so
 * that they take a default value when not set. Options may be mandatory in wich case it is an error not to specify them
 * on the command line. Flags are never mandatory because they are implicitly set to false when not specified.
 *
 * <p/>Some examples command line are:
 *
 * <ul>
 * <li>This one has two options that expect arguments:
 *
 * <pre>
 * cruisecontrol -configfile cruisecontrol.xml -port 9000
 * </pre>
 * <li>This has one no-arg flag and two 'free' arguments:
 *
 * <pre>
 * zip -r project.zip project/*
 * </pre>
 * <li>This one concatenates multiple flags into a single block with only one '-':
 *
 * <pre>
 * jar -tvf mytar.tar
 * </pre>
 *
 * <p/>The parsing rules are:
 *
 * <pre><ol>
 * <li>Flags may be combined after a single '-' because they never take arguments. Normally such flags are single letter
 * flags but this is only a convention and not enforced. Flags of more than one letter are usually specified on their
 * own.
 * <li>Options expecting arguments must always be on their own.
 * <li>The argument to an option may be seperated from it by whitespace or appended directly onto the option.
 * <li>The argument to an option may never begin with a '-' character.
 * <li>All other arguments not beginning with a '-' character are free arguments that do not belong to any option.
 * <li>The second or later of a set of duplicate or repeated flags override earlier ones.
 * <li>Options are matched up to the shortest matching option. This is because of the possibility of having no space
 * between an option and its argument. This rules out the possibility of using two options where one is an opening
 * substring of the other. For example, the options "foo" and "foobar" cannot be used on the same command line because
 * it is not possible to distinguish the argument "-foobar" from being the "foobar" option or the "foo" option with the
 * "bar" argument.
 * </ol></pre>
 *
 * <p/>By default, unknown options are simply ignored if specified on the command line. This behaviour may be changed so
 * that the parser reports all unknowns as errors by using the {@link #setErrorsOnUnknowns} method.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th>Responsibilities<th>Collaborations
 * <tr><td>Accept a command line specification.
 * <tr><td>Parse a command line into properties, validating it against its specification.
 * <tr><td>Report all errors between a command line and its specification.
 * <tr><td>Provide a formatted usage string for a command line.
 * <tr><td>Provide a formatted options in force string for a command line.
 * <tr><td>Allow errors on unknowns behaviour to be turned on or off.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CommandLineParser {
    /** Used to construct error reports on options. */
    private static final String OPTION = "Option ";

    /**
     * Holds a mapping from command line option names to detailed information about those options. Use of a tree map
     * ensures that the options are easy to print in alphabetical order as a usage string. An alternative might be to
     * use a LinkedHashMap to print them in the order they are specified.
     */
    private final Map<String, CommandLineOption> optionMap = new TreeMap<String, CommandLineOption>();

    /** Holds a list of parsing errors. */
    private List<String> parsingErrors = new ArrayList<String>();

    /** Holds the parsed command line properties after parsing. */
    private Properties parsedProperties = null;

    /** Flag used to indicate that errors should be created for unknown options. False by default. */
    private boolean errorsOnUnknowns = false;

    /**
     * Creates a command line options parser from a command line specification. This is passed to this constructor as an
     * array of arrays of strings. Each array of strings specifies the command line for a single option. A static array
     * may therefore easily be used to configure the command line parser in a single method call with an easily readable
     * format.
     *
     * <p/>Each array of strings must be 2, 3, 4 or 5 elements long. If any of the last three elements are missing they
     * are assumed to be null. The elements specify the following parameters:
     *
     * <pre><ol>
     * <li>The name of the option without the leading '-'. For example, "file". To specify the format of the 'free'
     * arguments use the option names "1", "2", ... and so on.
     * <li>The option comment. A line of text describing the usage of the option. For example, "The file to be
     * processed."
     * <li>The options argument. This is a very short description of the argument to the option, often a single word or
     * a reminder as to the arguments format. When this element is null the option is a flag and does not accept any
     * arguments. For example, "filename" or "(unix | windows)" or null. The actual text specified is only used to print
     * in the usage message to remind the user of the usage of the option.
     * <li>The mandatory flag. When set to "true" an option must always be specified. Any other value, including null,
     * means that the option is mandatory. Flags are always mandatory (see class javadoc for explanation of why) so this
     * is ignored for flags.
     * <li>A regular expression describing the format that the argument must take. Ignored if null.
     * </ol></pre>
     *
     * <p/>An example call to this constructor is:
     *
     * <pre>
     * CommandLineParser commandLine = new CommandLineParser(
     *     new String[][] {{"file", "The file to be processed. ", "filename", "true"},
     *                     {"dir", "Directory to store results in. Current dir used if not set.", "out dir"},
     *                     {"os", "Operating system EOL format to use.", "(windows | unix)", null, "windows\|unix"},
     *                     {"v", "Verbose mode. Prints information about the processing as it goes."},
     *                     {"1", "The processing command to run.", "command", "true", "add\|remove\|list"}});
     * </pre>
     *
     * @param config The configuration as an array of arrays of strings.
     */
    public CommandLineParser(String[][] config) {
        // Loop through all the command line option specifications creating details for each in the options map.
        for (String[] nextOptionSpec : config) {
            addOption(nextOptionSpec[0], nextOptionSpec[1], (nextOptionSpec.length > 2) ? nextOptionSpec[2] : null,
                (nextOptionSpec.length > 3) && ("true".equals(nextOptionSpec[3])),
                (nextOptionSpec.length > 4) ? nextOptionSpec[4] : null);
        }
    }

    /**
     * Right pads a string with a given string to a given size. This method will repeat the padder string as many times
     * as is necessary until the exact specified size is reached. If the specified size is less than the size of the
     * original string then the original string is returned unchanged.
     *
     * <pre>
     * Example1 - original string "cat", padder string "white", size 8 gives "catwhite".
     * Example2 - original string "cat", padder string "white", size 15 gives "catwhitewhitewh".
     * Example3 - original string "cat", padder string "white", size 2 gives "cat".
     * </pre>
     *
     * @param  stringToPad The original string.
     * @param  padder      The string to pad onto the original string.
     * @param  size        The required size of the new string.
     *
     * @return The newly padded string.
     */
    public static String rightPad(String stringToPad, String padder, int size) {
        if (padder.length() == 0) {
            return stringToPad;
        }

        StringBuilder strb = new StringBuilder(stringToPad);
        StringCharacterIterator sci = new StringCharacterIterator(padder);

        while (strb.length() < size) {
            for (char ch = sci.first(); ch != CharacterIterator.DONE; ch = sci.next()) {
                if (strb.length() < size) {
                    strb.append(String.valueOf(ch));
                }
            }
        }

        return strb.toString();
    }

    /**
     * Lists all the parsing errors from the most recent parsing in a string.
     *
     * @return All the parsing errors from the most recent parsing.
     */
    public String getErrors() {
        // Return the empty string if there are no errors.
        if (parsingErrors.isEmpty()) {
            return "";
        }

        // Concatenate all the parsing errors together.
        StringBuilder result = new StringBuilder();

        for (String s : parsingErrors) {
            result.append(s);
        }

        return result.toString();
    }

    /**
     * Lists the properties set from the most recent parsing or an empty string if no parsing has been done yet.
     *
     * @return The properties set from the most recent parsing or an empty string if no parsing has been done yet.
     */
    public String getOptionsInForce() {
        // Check if there are no properties to report and return and empty string if so.
        if (parsedProperties == null) {
            return "";
        }

        // List all the properties.
        StringBuilder result = new StringBuilder();
        result.append("Options in force:\n");

        for (Map.Entry<Object, Object> property : parsedProperties.entrySet()) {
            result.append(property.getKey()).append(" = ").append(property.getValue()).append("\n");
        }

        return result.toString();
    }

    /**
     * Generates a usage string consisting of the name of each option and each options argument description and comment.
     *
     * @return A usage string for all the options.
     */
    public String getUsage() {
        StringBuilder result = new StringBuilder();
        result.append("Options:\n");

        int optionWidth = 0;
        int argumentWidth = 0;

        // Calculate the column widths required for aligned layout.
        for (CommandLineOption optionInfo : optionMap.values()) {
            int oWidth = optionInfo.getOption().length();
            int aWidth = optionInfo.getArgument() != null ? optionInfo.getArgument().length() : 0;

            optionWidth = (oWidth > optionWidth) ? oWidth : optionWidth;
            argumentWidth = (aWidth > argumentWidth) ? aWidth : argumentWidth;
        }

        // Print usage on each of the command line options.
        for (CommandLineOption optionInfo : optionMap.values()) {
            String argString = optionInfo.getArgument() != null ? optionInfo.getArgument() : "";
            String optionString = optionInfo.getOption();

            argString = rightPad(argString, " ", argumentWidth);
            optionString = rightPad(optionString, " ", optionWidth);

            result.append("-")
                .append(optionString)
                .append(" ")
                .append(argString)
                .append(" ")
                .append(optionInfo.getComment())
                .append("\n");
        }

        return result.toString();
    }

    /**
     * Control the behaviour of the errors on unkowns reporting. When turned on this reports all unkowns options as
     * errors. When turned off, all unknowns are simply ignored.
     *
     * @param errors The setting of the errors on unkown flag. True to turn it on.
     */
    public void setErrorsOnUnknowns(boolean errors) {
        errorsOnUnknowns = errors;
    }

    /**
     * Parses a set of command line arguments into a set of properties, keyed by the argument flag. The free arguments
     * are keyed by integers as strings starting at "1" and then "2", ... and so on.
     *
     * <p/>See the class level comment for a description of the parsing rules.
     *
     * @param  args The command line arguments.
     *
     * @return The arguments as a set of properties.
     *
     * @throws IllegalArgumentException If the command line cannot be parsed against its specification. If this
     *                                  exception is thrown a call to {@link #getErrors} will provide a diagnostic of
     *                                  the command line errors.
     */
    public Properties parseCommandLine(String[] args) {
        // Create the regular expression matcher for all legal options.
        Pattern pattern = buildOptionRegex();

        // Holds the the parsed options.
        Properties options = new Properties();

        // Create the stateful argument parser and run it on all the arguments.
        ArgParser argParser = new ArgParser(options, pattern);

        for (String arg1 : args) {
            argParser.parse(arg1);
        }

        // Scan through all the specified options to check that all mandatory options have been set and that all flags
        // that were not set are set to false in the set of properties.
        for (CommandLineOption optionInfo : optionMap.values()) {
            // Check if this is a flag.
            if (!optionInfo.isExpectsArgs()) {
                // Check if the flag is not set in the properties and set it to false if so.
                if (!options.containsKey(optionInfo.getOption())) {
                    options.put(optionInfo.getOption(), "false");
                }
            } else if (optionInfo.isMandatory() && !options.containsKey(optionInfo.getOption())) {
                // This is a mandatory option and was not set.
                // Create an error for the missing option.
                parsingErrors.add(OPTION + optionInfo.getOption() + " is mandatory but not was not specified.\n");
            }
        }

        // Check if there were any errors.
        if (!parsingErrors.isEmpty()) {
            // Throw an illegal argument exception to signify that there were parsing errors.
            throw new IllegalArgumentException();
        }

        parsedProperties = options;

        return options;
    }

    /**
     * Resets this command line parser after it has been used to parse a command line. This method will only need to be
     * called to use this parser a second time which is not likely seeing as a command line is usually only specified
     * once. However, it is exposed as a public method for the rare case where this may be done.
     *
     * <p/>Cleans the internal state of this parser, removing all stored errors and information about the options in
     * force.
     */
    public void reset() {
        parsingErrors = new ArrayList<String>();
        parsedProperties = null;
    }

    /**
     * Adds the option to list of available command line options.
     *
     * @param option       The option to add as an available command line option.
     * @param comment      A comment for the option.
     * @param argument     The text that appears after the option in the usage string.
     * @param mandatory    When true, indicates that this option is mandatory.
     * @param formatRegexp The format that the argument must take, defined as a regular expression.
     */
    protected void addOption(String option, String comment, String argument, boolean mandatory, String formatRegexp) {
        // Check if usage text has been set in which case this option is expecting arguments.
        boolean expectsArgs = !((argument == null) || "".equals(argument));

        // Add the option to the map of command line options.
        CommandLineOption opt = new CommandLineOption(option, expectsArgs, comment, argument, mandatory, formatRegexp);
        optionMap.put(option, opt);
    }

    /**
     * Creates the regular expression to match all legal options that are not 'free' arguments.
     *
     * @return The regular expression to match all legal options that are not 'free' arguments.
     */
    private Pattern buildOptionRegex() {
        // Create the regular expression matcher for the command line options.
        StringBuilder regexp = new StringBuilder();
        regexp.append("^(");

        int optionsAdded = 0;

        for (Iterator<String> i = optionMap.keySet().iterator(); i.hasNext();) {
            String nextOption = i.next();

            // Check that the option is not a free argument definition.
            boolean notFree = false;

            try {
                Integer.parseInt(nextOption);
            } catch (NumberFormatException e) {
                notFree = true;
            }

            // Add the option to the regular expression matcher if it is not a free argument definition.
            if (notFree) {
                regexp.append(nextOption).append(i.hasNext() ? "|" : "");
                optionsAdded++;
            }
        }

        // There has to be more that one option in the regular expression or else the compiler complains that the close
        // cannot be nullable if the '?' token is used to make the matched option string optional.
        regexp.append(")").append((optionsAdded > 0) ? "?" : "").append("(.*)");

        return Pattern.compile(regexp.toString());
    }

    /**
     * Checks the format of an argument to an option against its specified regular expression format if one has been
     * set. Any errors are added to the list of parsing errors.
     *
     * @param optionInfo The command line option information for the option which is havings its argument checked.
     * @param matchedArg The string argument to the option.
     */
    private void checkArgumentFormat(CommandLineOption optionInfo, String matchedArg) {
        // Check if this option enforces a format for its argument.
        if (optionInfo.getArgumentFormatRegexp() != null) {
            Pattern pattern = Pattern.compile(optionInfo.getArgumentFormatRegexp());
            Matcher argumentMatcher = pattern.matcher(matchedArg);

            // Check if the argument does not meet its required format.
            if (!argumentMatcher.matches()) {
                // Create an error for this badly formed argument.
                parsingErrors.add("The argument to option " + optionInfo.getOption() +
                    " does not meet its required format.\n");
            }
        }
    }

    /**
     * Holds information about a command line options. This includes what its name is, whether or not it is a flag,
     * whether or not it is mandatory, what its user comment is, what its argument reminder text is and what its regular
     * expression format is.
     *
     * <pre><p/><table id="crc"><caption>CRC Card</caption>
     * <tr><th>Responsibilities<th>Collaborations
     * <tr><td>Hold details of a command line option.
     * </table></pre>
     *
     * @author Rupert Smith
     */
    protected static class CommandLineOption {
        /** Holds the text for the flag to match this argument with. */
        private String option = null;

        /** Holds a string describing how to use this command line argument. */
        private String argument = null;

        /** Flag that determines whether or not this command line argument can take arguments. */
        private boolean expectsArgs = false;

        /** Holds a short comment describing what this command line argument is for. */
        private String comment = null;

        /** Flag that determines whether or not this is an mandatory command line argument. */
        private boolean mandatory = false;

        /** A regular expression describing what format the argument to this option muist have. */
        private String argumentFormatRegexp = null;

        /**
         * Create a command line option object that holds specific information about a command line option.
         *
         * @param option       The text that matches the option.
         * @param expectsArgs  Whether or not the option expects arguments. It is a flag if this is false.
         * @param comment      A comment explaining how to use this option.
         * @param argument     A short reminder of the format of the argument to this option/
         * @param mandatory    Set to true if this option is mandatory.
         * @param formatRegexp The regular expression that the argument to this option must meet to be valid.
         */
        public CommandLineOption(String option, boolean expectsArgs, String comment, String argument, boolean mandatory,
            String formatRegexp) {
            this.setOption(option);
            this.setExpectsArgs(expectsArgs);
            this.setComment(comment);
            this.setArgument(argument);
            this.setMandatory(mandatory);
            this.setArgumentFormatRegexp(formatRegexp);
        }

        /**
         * Provides the text for the flag to match this argument with.
         *
         * @return The text for the flag to match this argument with.
         */
        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        /**
         * Provides a string describing how to use this command line argument.
         *
         * @return A string describing how to use this command line argument.
         */
        public String getArgument() {
            return argument;
        }

        public void setArgument(String argument) {
            this.argument = argument;
        }

        /**
         * Provides a flag that determines whether or not this command line argument can take arguments.
         *
         * @return A flag that determines whether or not this command line argument can take arguments.
         */
        public boolean isExpectsArgs() {
            return expectsArgs;
        }

        public void setExpectsArgs(boolean expectsArgs) {
            this.expectsArgs = expectsArgs;
        }

        /**
         * Provides a short comment describing what this command line argument is for.
         *
         * @return A short comment describing what this command line argument is for.
         */
        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        /**
         * Provides a flag that determines whether or not this is an mandatory command line argument.
         *
         * @return A flag that determines whether or not this is an mandatory command line argument.
         */
        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        /**
         * Provides a regular expression describing what format the argument to this option must have.
         *
         * @return A regular expression describing what format the argument to this option must have.
         */
        public String getArgumentFormatRegexp() {
            return argumentFormatRegexp;
        }

        public void setArgumentFormatRegexp(String argumentFormatRegexp) {
            this.argumentFormatRegexp = argumentFormatRegexp;
        }
    }

    /**
     * ArgParser is a stateful argument parser. It parses arguments one at a time against their expected format. State
     * variables track the positions of free arguments, and whether or not subsequent arguments are themselves arguments
     * to other command line options that are possibly expecting argument values.
     */
    private class ArgParser {
        /** Used to store parsed options in. */
        private final Properties options;

        /** Used to keep count of the current 'free' argument. */
        private int free = 1;

        /** Indicates whether the next item to parse is coming after an option that is expecting arguments. */
        private boolean expectingArgs = false;

        /** The option that is expecting an argument, when one has just previously been parsed. */
        private String optionExpectingArgs = null;

        /** The regular expression to match options with. */
        private final Pattern pattern;

        /**
         * Creates a stateful argument parser.
         *
         * @param options Used to store parsed options in.
         * @param pattern A regular expression that matches all legal options.
         */
        public ArgParser(Properties options, Pattern pattern) {
            this.options = options;
            this.pattern = pattern;
        }

        /**
         * Parses a single command line argument, in the context of possible previously parsed arguments.
         *
         * @param argToParse The single command line argument to parse.
         */
        public void parse(String argToParse) {
            // Check if the next command line argument begins with a '-' character and is therefore the start of
            // an option.
            if (argToParse.startsWith("-")) {
                parseOption(argToParse);
            } else {
                parseArgument(argToParse);
            }
        }

        /**
         * Parses the next command line argument as an option, which may also be immediately followed by more flags or
         * an argument to the option without any whitespace in between.
         *
         * @param argToParse The next command line argument to parse.
         */
        private void parseOption(String argToParse) {
            // Extract the value of the option without the leading '-'.
            String arg = argToParse.substring(1);

            // Match up to the longest matching option.
            Matcher optionMatcher = pattern.matcher(arg);
            optionMatcher.matches();

            String matchedOption = optionMatcher.group(1);

            // Match any argument directly appended onto the longest matching option.
            String matchedArg = optionMatcher.group(2);

            // Check that a known option was matched.
            if ((matchedOption != null) && !"".equals(matchedOption)) {
                // Get the command line option information for the matched option.
                CommandLineOption optionInfo = optionMap.get(matchedOption);

                // Check if this option is expecting arguments.
                if (optionInfo.isExpectsArgs()) {
                    // The option is expecting arguments so swallow the next command line argument as an
                    // argument to this option.
                    expectingArgs = true;
                    optionExpectingArgs = matchedOption;
                }

                // Check if the option was matched on its own and is a flag in which case set that flag.
                if ("".equals(matchedArg) && !optionInfo.isExpectsArgs()) {
                    options.put(matchedOption, "true");
                } else if (!"".equals(matchedArg)) {
                    // The option was matched as a substring with its argument appended to it or is a flag that is
                    // condensed together with other flags.
                    // Check if the option is a flag and therefore is allowed to be condensed together
                    // with other flags.
                    parseFlagsOrArgs(arg, matchedOption, matchedArg, optionInfo);
                }
            } else {
                // No matching option was found.
                // Add this to the list of parsing errors if errors on unkowns is being used.
                if (errorsOnUnknowns) {
                    parsingErrors.add(OPTION + matchedOption + " is not a recognized option.\n");
                }
            }
        }

        /**
         * Parses the next command line argument as an argument to a previously seen option, or as a 'free' argument.
         *
         * @param argToParse The next command line argument to parse.
         */
        private void parseArgument(String argToParse) {
            // The command line argument did not being with a '-' so it is an argument to the previous flag or it
            // is a free argument.
            // Check if a previous flag is expecting to swallow this next argument as its argument.
            if (expectingArgs) {
                // Get the option info for the option waiting for arguments.
                CommandLineOption optionInfo = optionMap.get(optionExpectingArgs);

                // Check the arguments format is correct against any specified format.
                checkArgumentFormat(optionInfo, argToParse);

                // Store the argument against its option (regardless of its format).
                options.put(optionExpectingArgs, argToParse);

                // Clear the expecting args flag now that the argument has been swallowed.
                expectingArgs = false;
                optionExpectingArgs = null;
            } else {
                // This command line option is not an argument to any option. Add it to the set of 'free' options.
                // Get the option info for the free option, if there is any.
                CommandLineOption optionInfo = optionMap.get(Integer.toString(free));

                if (optionInfo != null) {
                    // Check the arguments format is correct against any specified format.
                    checkArgumentFormat(optionInfo, argToParse);
                }

                // Add to the list of free options.
                options.put(Integer.toString(free), argToParse);

                // Move on to the next free argument.
                free++;
            }
        }

        /**
         * A flag can be combined together with other flags OR an option can be immediately followed by its argument
         * without an whitespace in between. This parse method decides which situation has been encountered when an
         * option has been matched but is immediately followed by more text.
         *
         * @param arg           The option as a flag, that is the first character of it.
         * @param matchedOption The option that was matched.
         * @param matchedArg    The argument to the option that was matched.
         * @param optionInfo    The details on the option that was matched.
         */
        private void parseFlagsOrArgs(String arg, String matchedOption, String matchedArg,
            CommandLineOption optionInfo) {
            if (!optionInfo.isExpectsArgs()) {
                // Set the first matched flag.
                options.put(matchedOption, "true");

                // Repeat the longest matching process on the remainder but ensure that the remainder
                // consists only of flags as only flags may be condensed together in this fashion.
                String nextMatchedOption = matchedOption;
                String nextMatchedArg = matchedArg;

                do {
                    // Match the remainder against the options.
                    Matcher optionMatcher = pattern.matcher(nextMatchedArg);
                    optionMatcher.matches();

                    nextMatchedOption = optionMatcher.group(1);
                    nextMatchedArg = optionMatcher.group(2);

                    // Check that an option was matched.
                    if (nextMatchedOption != null) {
                        checkIfOptionCanCombineWithFlags(nextMatchedOption);

                        options.put(nextMatchedOption, "true");
                    } else {
                        // The remainder could not be matched against a flag it is either an unknown flag
                        // or an illegal argument to a flag.
                        parsingErrors.add("Illegal argument to a flag in the option " + arg + "\n");

                        break;
                    }

                    // Continue until the remainder of the argument has all been matched with flags.
                } while (!"".equals(nextMatchedArg));
            } else {
                // The option is expecting an argument, so store the unmatched portion against it
                // as its argument.
                // Check the arguments format is correct against any specified format.
                checkArgumentFormat(optionInfo, matchedArg);

                // Store the argument against its option (regardless of its format).
                options.put(matchedOption, matchedArg);

                // The argument to this flag has already been supplied to it. Do not swallow the
                // next command line argument as an argument to this flag.
                expectingArgs = false;
            }
        }

        /**
         * Checks if the specified option is allowed to be combined with other flags. This is only possible if the
         * option does not expect any arguments.
         *
         * <p/>Note: {@link #parsingErrors} will be updated if the check fails.
         *
         * @param option The option to check.
         */
        private void checkIfOptionCanCombineWithFlags(String option) {
            // Get the command line option information for the next matched option.
            CommandLineOption nextOptionInfo = optionMap.get(option);

            // Ensure that the next option is a flag or raise an error if not.
            if (nextOptionInfo.isExpectsArgs()) {
                parsingErrors.add(OPTION + option +
                    " cannot be combined with flags.\n");
            }
        }
    }
}
