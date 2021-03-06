/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.action.searchinto.parser;

import java.util.Map;

import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.SearchParseElement;
import org.elasticsearch.search.fetch.script.ScriptFieldsContext;
import org.elasticsearch.search.internal.SearchContext;

import io.crate.action.searchinto.SearchIntoContext;

/**
 * parses the fields field which looks like
 * <p/>
 * "fields": [
 * "_id",
 * ["_index", "'constant_name'"],
 * ["copy_ts", {"script" : "time()"}],
 * ["name", "_source.firstname"]
 * ],
 */

public class FieldsParseElement implements SearchParseElement {

    public static final String SCRIPT_FIELD_PREFIX = "__script_field_";

    public FieldsParseElement() {

    }

    private static String getLiteral(String candidate) {
        if (candidate != null && candidate.length() > 2) {
            if ((candidate.startsWith("'") && candidate.endsWith(
                    "'")) || (candidate.startsWith("\"") && candidate.endsWith(
                    "\""))) {
                return candidate.substring(1, candidate.length() - 1);
            }
        }
        return null;
    }

    @Override
    public void parse(XContentParser parser, SearchContext context) throws
            Exception {

        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.START_ARRAY) {
            boolean added = false;
            while ((token = parser.nextToken()) != XContentParser.Token
                    .END_ARRAY) {
                String source;
                String target;
                String sourceValue = null;
                if (token == XContentParser.Token.START_ARRAY) {
                    // we have a target mapping
                    token = parser.nextToken();
                    target = parser.text();
                    token = parser.nextToken();
                    if (token == XContentParser.Token.START_OBJECT) {
                        String script = null;
                        String scriptLang = null;
                        String scriptName = source = SCRIPT_FIELD_PREFIX +
                                target;
                        Map<String, Object> params = null;
                        boolean ignoreException = false;
                        String currentFieldName = null;
                        while ((token = parser.nextToken()) !=
                                XContentParser.Token.END_OBJECT) {
                            if (token == XContentParser.Token.FIELD_NAME) {
                                currentFieldName = parser.currentName();
                            } else if (token == XContentParser.Token
                                    .START_OBJECT) {
                                params = parser.map();
                            } else if (token.isValue()) {
                                if ("script".equals(currentFieldName)) {
                                    script = parser.text();
                                } else if ("name".equals(currentFieldName)) {
                                    scriptName = parser.text();
                                    source = SCRIPT_FIELD_PREFIX + scriptName;
                                } else if ("lang".equals(currentFieldName)) {
                                    scriptLang = parser.text();
                                } else if ("ignore_failure".equals(
                                        currentFieldName)) {
                                    ignoreException = parser.booleanValue();
                                }
                            }
                        }
                        SearchScript searchScript = context.scriptService()
                                .search(
                                context.lookup(), scriptLang, script, params);
                        context.scriptFields().add(
                                new ScriptFieldsContext.ScriptField(scriptName,
                                        searchScript, ignoreException));
                    } else {
                        source = parser.text();
                        sourceValue = getLiteral(source);
                    }
                    token = parser.nextToken();
                    assert (token == XContentParser.Token.END_ARRAY);
                } else {
                    source = target = parser.text();
                }
                if (!source.startsWith(
                        SCRIPT_FIELD_PREFIX) && sourceValue == null) {
                    if (source.contains("_source.") || source.contains(
                            "doc[")) {
                        // script field to load from source
                        SearchScript searchScript = context.scriptService()
                                .search(
                                context.lookup(), "mvel", source, null);
                        context.scriptFields().add(
                                new ScriptFieldsContext.ScriptField(source,
                                        searchScript, true));
                    } else {
                        added = true;
                        context.fieldNames().add(source);
                    }
                }
                ((SearchIntoContext) context).outputNames().put(source,
                        target);
            }

            if (!added) {
                context.emptyFieldNames();
            }
        } else if (token == XContentParser.Token.VALUE_STRING) {
            String name = parser.text();
            if (name.contains("_source.") || name.contains("doc[")) {
                // script field to load from source
                SearchScript searchScript = context.scriptService().search(
                        context.lookup(), "mvel", name, null);
                context.scriptFields().add(new ScriptFieldsContext.ScriptField(
                        name, searchScript, true));
            } else {
                context.fieldNames().add(name);
            }
            ((SearchIntoContext) context).outputNames().put(name, name);
        }
    }

}
