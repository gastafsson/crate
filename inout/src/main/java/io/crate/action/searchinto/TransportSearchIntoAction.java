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

package io.crate.action.searchinto;

import io.crate.action.searchinto.parser.SearchIntoParser;
import io.crate.searchinto.Writer;
import org.elasticsearch.cache.recycler.CacheRecycler;
import org.elasticsearch.cache.recycler.PageCacheRecycler;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;


/**
 *
 */
public class TransportSearchIntoAction extends AbstractTransportSearchIntoAction {

    @Inject
    public TransportSearchIntoAction(Settings settings,
            ThreadPool threadPool, ClusterService clusterService,
            TransportService transportService, CacheRecycler cacheRecycler,
            PageCacheRecycler pageCacheRecycler, BigArrays bigArrays,
            IndicesService indicesService, ScriptService scriptService,
            SearchIntoParser parser, Writer writer) {
        super(settings, threadPool, clusterService, transportService, cacheRecycler,
                pageCacheRecycler, bigArrays, indicesService, scriptService, parser, writer);
    }

    @Override
    protected String transportAction() {
        return SearchIntoAction.NAME;
    }

}
