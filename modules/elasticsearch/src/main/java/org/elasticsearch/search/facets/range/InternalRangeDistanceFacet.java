/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.facets.range;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.builder.XContentBuilder;
import org.elasticsearch.search.facets.Facet;
import org.elasticsearch.search.facets.internal.InternalFacet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author kimchy (shay.banon)
 */
public class InternalRangeDistanceFacet implements RangeFacet, InternalFacet {

    private String name;

    private String keyFieldName;

    private String valueFieldName;

    private Entry[] entries;

    InternalRangeDistanceFacet() {
    }

    public InternalRangeDistanceFacet(String name, String keyFieldName, String valueFieldName, Entry[] entries) {
        this.name = name;
        this.keyFieldName = keyFieldName;
        this.valueFieldName = valueFieldName;
        this.entries = entries;
    }

    @Override public String name() {
        return this.name;
    }

    @Override public String getName() {
        return name();
    }

    @Override public Type type() {
        return Type.RANGE;
    }

    @Override public Type getType() {
        return type();
    }

    @Override public String keyFieldName() {
        return this.keyFieldName;
    }

    @Override public String getKeyFieldName() {
        return keyFieldName();
    }

    @Override public String valueFieldName() {
        return this.valueFieldName;
    }

    @Override public String getValueFieldName() {
        return valueFieldName();
    }

    @Override public List<Entry> entries() {
        return ImmutableList.copyOf(entries);
    }

    @Override public List<Entry> getEntries() {
        return entries();
    }

    @Override public Iterator<Entry> iterator() {
        return entries().iterator();
    }

    @Override public Facet aggregate(Iterable<Facet> facets) {
        InternalRangeDistanceFacet agg = null;
        for (Facet facet : facets) {
            if (!facet.name().equals(name)) {
                continue;
            }
            InternalRangeDistanceFacet geoDistanceFacet = (InternalRangeDistanceFacet) facet;
            if (agg == null) {
                agg = geoDistanceFacet;
            } else {
                for (int i = 0; i < geoDistanceFacet.entries.length; i++) {
                    agg.entries[i].count += geoDistanceFacet.entries[i].count;
                    agg.entries[i].total += geoDistanceFacet.entries[i].total;
                }
            }
        }
        return agg;
    }

    public static InternalRangeDistanceFacet readRangeFacet(StreamInput in) throws IOException {
        InternalRangeDistanceFacet facet = new InternalRangeDistanceFacet();
        facet.readFrom(in);
        return facet;
    }

    @Override public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        keyFieldName = in.readUTF();
        valueFieldName = in.readUTF();
        entries = new Entry[in.readVInt()];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new Entry(in.readDouble(), in.readDouble(), in.readVLong(), in.readDouble());
        }
    }

    @Override public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(keyFieldName);
        out.writeUTF(valueFieldName);
        out.writeVInt(entries.length);
        for (Entry entry : entries) {
            out.writeDouble(entry.from);
            out.writeDouble(entry.to);
            out.writeVLong(entry.count);
            out.writeDouble(entry.total);
        }
    }

    @Override public void toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field("_type", "range");
        builder.field("_key_field", keyFieldName);
        builder.field("_value_field", valueFieldName);
        builder.startArray("ranges");
        for (Entry entry : entries) {
            builder.startObject();
            if (!Double.isInfinite(entry.from)) {
                builder.field("from", entry.from);
            }
            if (!Double.isInfinite(entry.to)) {
                builder.field("to", entry.to);
            }
            builder.field("count", entry.count());
            builder.field("total", entry.total());
            builder.field("mean", entry.mean());
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
    }
}
