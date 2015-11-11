package com.thesett.util.views.handlebars;

import java.util.List;
import java.util.Map;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class HandlebarsConfig {
    private boolean cacheTemplates = true;
    private List<Map<String, String>> overrides;

    public boolean isCacheTemplates() {
        return cacheTemplates;
    }

    public void setCacheTemplates(boolean cacheTemplates) {
        this.cacheTemplates = cacheTemplates;
    }

    public List<Map<String, String>> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<Map<String, String>> overrides) {
        this.overrides = overrides;
    }
}
