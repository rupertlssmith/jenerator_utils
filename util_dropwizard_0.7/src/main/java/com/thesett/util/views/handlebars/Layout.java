package com.thesett.util.views.handlebars;

import io.dropwizard.views.View;

/**
 * A Layout extends the concept of a DropWizard View, by combining both a layout template and a view template. The view
 * template will be rendered, and inserted into the layout template.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Define a view composed from a view and a layout template. </td></tr>
 * </table></pre>
 */
public class Layout extends View {
    protected final String layoutTemplateName;

    public Layout(String templateName, String layoutTemplateName) {
        super(templateName);

        this.layoutTemplateName = layoutTemplateName;
    }

    public String getLayoutTemplateName() {
        return layoutTemplateName;
    }
}
