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
package com.thesett.util.hibernate;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL9Dialect;

/**
 * Extends the Postgres dialect to allow json columns to be mapped to obejcts.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Add json types to the postgres dialect.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JsonPostgreSQLDialect extends PostgreSQL9Dialect
{
    public JsonPostgreSQLDialect()
    {
        super();

        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}
