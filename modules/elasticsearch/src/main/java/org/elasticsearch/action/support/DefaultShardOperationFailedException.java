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

package org.elasticsearch.action.support;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.index.shard.IndexShardException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static org.elasticsearch.ExceptionsHelper.*;

/**
 * @author kimchy (shay.banon)
 */
public class DefaultShardOperationFailedException implements ShardOperationFailedException {

    private String index;

    private int shardId;

    private String reason;

    private DefaultShardOperationFailedException() {

    }

    public DefaultShardOperationFailedException(IndexShardException e) {
        this.index = e.shardId().index().name();
        this.shardId = e.shardId().id();
        this.reason = detailedMessage(e);
    }

    public DefaultShardOperationFailedException(String index, int shardId, Throwable t) {
        this.index = index;
        this.shardId = shardId;
        this.reason = detailedMessage(t);
    }

    @Override public String index() {
        return this.index;
    }

    @Override public int shardId() {
        return this.shardId;
    }

    @Override public String reason() {
        return this.reason;
    }

    public static DefaultShardOperationFailedException readShardOperationFailed(DataInput in) throws IOException, ClassNotFoundException {
        DefaultShardOperationFailedException exp = new DefaultShardOperationFailedException();
        exp.readFrom(in);
        return exp;
    }

    @Override public void readFrom(DataInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            index = in.readUTF();
        }
        shardId = in.readInt();
        reason = in.readUTF();
    }

    @Override public void writeTo(DataOutput out) throws IOException {
        if (index == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(index);
        }
        out.writeInt(shardId);
        out.writeUTF(reason);
    }
}
