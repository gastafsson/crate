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

package io.crate.operation.predicate;

import io.crate.metadata.*;
import io.crate.planner.RowGranularity;
import io.crate.planner.symbol.*;
import io.crate.DataType;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IsNullPredicateTest {

    IsNullPredicate predicate = new IsNullPredicate(new FunctionInfo(
            new FunctionIdent(IsNullPredicate.NAME, Arrays.asList(DataType.STRING)),
            DataType.BOOLEAN
    ));

    @Test
    public void testNormalizeSymbolFalse() throws Exception {
        Function isNull = new Function(predicate.info(), Arrays.<Symbol>asList(new StringLiteral("a")));
        Symbol symbol = predicate.normalizeSymbol(isNull);
        assertThat(symbol, instanceOf(BooleanLiteral.class));
        assertThat(((BooleanLiteral) symbol).value(), is(false));
    }

    @Test
    public void testNormalizeSymbolTrue() throws Exception {
        Function isNull = new Function(predicate.info(), Arrays.<Symbol>asList(Null.INSTANCE));
        Symbol symbol = predicate.normalizeSymbol(isNull);

        assertThat(symbol, instanceOf(BooleanLiteral.class));
        assertThat(((BooleanLiteral) symbol).value(), is(true));
    }

    @Test
    public void testNormalizeReference() throws Exception {
        Reference name_ref = new Reference(new ReferenceInfo(
                        new ReferenceIdent(new TableIdent(null, "dummy"), "name"),
                        RowGranularity.DOC,
                        DataType.STRING));
        Function isNull = new Function(predicate.info(), Arrays.<Symbol>asList(name_ref));
        Symbol symbol = predicate.normalizeSymbol(isNull);
        assertThat(symbol, instanceOf(Function.class));
    }
}
