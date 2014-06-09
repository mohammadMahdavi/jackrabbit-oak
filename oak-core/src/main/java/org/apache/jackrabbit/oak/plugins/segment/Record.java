/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.segment;

import static org.apache.jackrabbit.oak.plugins.segment.Compactor.readEntry;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

/**
 * Record within a segment.
 */
class Record {

    static boolean fastEquals(Object a, Object b, SegmentStore store) {
        return a instanceof Record && fastEquals((Record) a, b, store);
    }

    static boolean fastEquals(Record a, Object b, SegmentStore store) {
        return b instanceof Record && fastEquals(a, (Record) b, store);
    }

    static boolean fastEquals(Record a, Record b, SegmentStore store) {
        ByteBuffer compaction = store.getCompactionMap();
        if (compaction == null) {
            return a.segmentId == b.segmentId && a.offset == b.offset;
        }
        long[] aId = readEntry(compaction, a.getRecordId());
        long[] bId = readEntry(compaction, b.getRecordId());
        return aId[0] == bId[0] && aId[1] == bId[1] && aId[2] == bId[2];
    }

    /**
     * Identifier of the segment that contains this record.
     */
    private final SegmentId segmentId;

    /**
     * Segment offset of this record.
     */
    private final int offset;

    /**
     * Creates a new object for the identified record.
     *
     * @param id record identified
     */
    protected Record(@Nonnull RecordId id) {
        this(id.getSegmentId(), id.getOffset());
    }

    protected Record(@Nonnull SegmentId segmentId, int offset) {
        this.segmentId = segmentId;
        this.offset = offset;
    }

    /**
     * Returns the segment that contains this record.
     *
     * @return segment that contains this record
     */
    protected Segment getSegment() {
        return segmentId.getSegment();
    }

    /**
     * Returns the segment store.
     *
     * @return segment store
     */
    public SegmentStore getStore() {
        return segmentId.getTracker().getStore();
    }

    /**
     * Returns the identifier of this record.
     *
     * @return record identifier
     */
    public RecordId getRecordId() {
        return new RecordId(segmentId, offset);
    }

    /**
     * Returns the segment offset of this record.
     *
     * @return segment offset of this record
     */
    protected final int getOffset() {
        return offset;
    }

    /**
     * Returns the segment offset of the given byte position in this record.
     *
     * @param position byte position within this record
     * @return segment offset of the given byte position
     */
    protected final int getOffset(int position) {
        return getOffset() + position;
    }

    /**
     * Returns the segment offset of a byte position in this record.
     * The position is calculated from the given number of raw bytes and
     * record identifiers.
     *
     * @param bytes number of raw bytes before the position
     * @param ids number of record identifiers before the position
     * @return segment offset of the specified byte position
     */
    protected final int getOffset(int bytes, int ids) {
        return getOffset(bytes + ids * Segment.RECORD_ID_BYTES);
    }

    //------------------------------------------------------------< Object >--

    @Override
    public boolean equals(Object that) {
        return fastEquals(this, that, getStore());
    }

    @Override
    public int hashCode() {
        return segmentId.hashCode() ^ offset;
    }

    @Override
    public String toString() {
        return getRecordId().toString();
    }

}
