/*
 * Copyright 2025, AutoMQ HK Limited.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.log.streamaspect;

import java.io.IOException;

public class DefaultStreamSliceSupplier implements StreamSliceSupplier {
    private final ElasticStreamSliceManager streamSliceManager;
    private final String streamName;
    private final SliceRange sliceRange;

    public DefaultStreamSliceSupplier(ElasticStreamSliceManager streamSliceManager, String streamName, SliceRange sliceRange) {
        this.streamSliceManager = streamSliceManager;
        this.streamName = streamName;
        this.sliceRange = sliceRange;
    }

    @Override
    public ElasticStreamSlice get() throws IOException {
        return streamSliceManager.loadOrCreateSlice(streamName, sliceRange);
    }

    /**
     * reset the slice to an open empty slice. This is used in segment index recovery.
     *
     * @return a new open empty slice
     */
    @Override
    public ElasticStreamSlice reset() throws IOException {
        return streamSliceManager.newSlice(streamName);
    }

}
