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

package io.crate.metadata.shard.blob;

import io.crate.blob.v2.BlobShard;
import io.crate.expression.NestableInput;
import io.crate.expression.reference.LiteralNestableInput;
import io.crate.expression.reference.ReferenceResolver;
import io.crate.expression.reference.sys.shard.ShardMinLuceneVersionExpression;
import io.crate.expression.reference.sys.shard.ShardPathExpression;
import io.crate.expression.reference.sys.shard.ShardPrimaryExpression;
import io.crate.expression.reference.sys.shard.ShardRecoveryExpression;
import io.crate.expression.reference.sys.shard.ShardRelocatingNodeExpression;
import io.crate.expression.reference.sys.shard.ShardRoutingStateExpression;
import io.crate.expression.reference.sys.shard.ShardStateExpression;
import io.crate.expression.reference.sys.shard.blob.BlobShardBlobPathExpression;
import io.crate.expression.reference.sys.shard.blob.BlobShardNumDocsExpression;
import io.crate.expression.reference.sys.shard.blob.BlobShardSizeExpression;
import io.crate.expression.reference.sys.shard.blob.BlobShardTableNameExpression;
import io.crate.metadata.MapBackedRefResolver;
import io.crate.metadata.ReferenceIdent;
import io.crate.metadata.blob.BlobSchemaInfo;
import io.crate.metadata.shard.NodeNestableInput;
import io.crate.metadata.sys.SysShardsTableInfo;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;

import java.util.HashMap;

public class BlobShardReferenceResolver {

    public static ReferenceResolver<NestableInput<?>> create(BlobShard blobShard, DiscoveryNode localNode) {
        IndexShard indexShard = blobShard.indexShard();
        ShardId shardId = indexShard.shardId();
        HashMap<ReferenceIdent, NestableInput> implementations = new HashMap<>(15);
        implementations.put(SysShardsTableInfo.ReferenceIdents.ID, new LiteralNestableInput<>(shardId.id()));
        implementations.put(SysShardsTableInfo.ReferenceIdents.NUM_DOCS, new BlobShardNumDocsExpression(blobShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.PRIMARY, new ShardPrimaryExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.RELOCATING_NODE,
            new ShardRelocatingNodeExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.SCHEMA_NAME,
            new LiteralNestableInput<>(new BytesRef(BlobSchemaInfo.NAME)));
        implementations.put(SysShardsTableInfo.ReferenceIdents.SIZE, new BlobShardSizeExpression(blobShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.STATE, new ShardStateExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.ROUTING_STATE, new ShardRoutingStateExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.TABLE_NAME, new BlobShardTableNameExpression(shardId));
        implementations.put(SysShardsTableInfo.ReferenceIdents.PARTITION_IDENT,
            new LiteralNestableInput<>(new BytesRef("")));
        implementations.put(SysShardsTableInfo.ReferenceIdents.ORPHAN_PARTITION,
            new LiteralNestableInput<>(false));
        implementations.put(SysShardsTableInfo.ReferenceIdents.PATH, new ShardPathExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.BLOB_PATH, new BlobShardBlobPathExpression(blobShard));
        implementations.put(
            SysShardsTableInfo.ReferenceIdents.MIN_LUCENE_VERSION,
            new ShardMinLuceneVersionExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.RECOVERY, new ShardRecoveryExpression(indexShard));
        implementations.put(SysShardsTableInfo.ReferenceIdents.NODE, new NodeNestableInput(localNode));
        return new MapBackedRefResolver(implementations);
    }

}
