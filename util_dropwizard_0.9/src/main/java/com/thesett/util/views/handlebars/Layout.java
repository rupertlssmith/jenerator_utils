/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.util.views.handlebars;

import io.dropwizard.views.View;

/**
 * A Layout extends the concept of a DropWizard View, by combining both a layout template and a view template. The view
 * template will be rendered, and inserted into the layout template.
 *
 * <p/>A Layout has a special field called "body", into which the body will be rendered, prior to rendering the complete
 * layout.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Define a view composed from a view and a layout template. </td></tr>
 * </table></pre>
 */
public class Layout extends View
{
    protected final String layoutTemplateName;

    protected String body;

    public Layout(String templateName, String layoutTemplateName)
    {
        super(templateName);

        this.layoutTemplateName = layoutTemplateName;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getLayoutTemplateName()
    {
        return layoutTemplateName;
    }
}
