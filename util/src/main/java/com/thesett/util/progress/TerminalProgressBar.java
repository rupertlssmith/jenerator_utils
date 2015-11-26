package com.thesett.util.progress;

/**
 * TerminalProgressBar is a progress bar that uses simple text printing to the console to display a progress bar.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Print characters to indicate progress. </td></tr>
 * </table></pre>
 */
public class TerminalProgressBar implements ProgressIndicator {
    /** Defines the line width of the progress bar to print. */
    public static final int LINE_WIDTH = 80;

    /** Holds the amount of work to do in order to reach completion. */
    int workToDo;

    /** Holds the last position that a progress character was printed to. */
    int lastPrintPosition;

    /** {@inheritDoc} */
    public void initWorkToDo(String name, int amount) {
        workToDo = amount;

        System.out.println("Processing " + amount + " rows from " + name + ".");
    }

    /** {@inheritDoc} */
    public void onWorkDone(int amount) {
        int x = (amount * LINE_WIDTH) / workToDo;

        int numStars = 0;

        if (x > lastPrintPosition) {
            numStars = x - lastPrintPosition;

            for (int i = 0; i < numStars; i++) {
                System.out.print("=");
            }

            lastPrintPosition = x;
        }

        if (amount == workToDo) {
            System.out.println();
            reset();
        }
    }

    /** Resets the last print position to zero. */
    private void reset() {
        lastPrintPosition = 0;
    }
}
