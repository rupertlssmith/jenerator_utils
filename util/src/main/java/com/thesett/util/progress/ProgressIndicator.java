package com.thesett.util.progress;

/**
 * ProgressIndicator is a call-back interface that a long running process can use to feedback information about the
 * progress being made. This could be used to update a progress bar for example.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Indicate the total amount of work to be done. </td></tr>
 * <tr><td> Indicate the current amount of work done. </td></tr>
 * </table></pre>
 */
public interface ProgressIndicator {
    /**
     * Indicates the total amount of work to be done.
     *
     * @param name   A name for the task being undertaken.
     * @param amount The total amount of work to be done.
     */
    void initWorkToDo(String name, int amount);

    /**
     * Updates the amount of work done so far.
     *
     * @param amount The amount of work done so far.
     */
    void onWorkDone(int amount);
}
