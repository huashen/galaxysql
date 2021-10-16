/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.optimizer.partition.datatype.iterator;

import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.field.TypeConversionStatus;
import com.alibaba.polardbx.optimizer.partition.datatype.PartitionField;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLongs;

public class SimpleInIterator implements PartitionFieldIterator {
    private boolean lowerBoundIncluded;
    private boolean upperBoundIncluded;
    private boolean firstEnumerated;

    private int currentCount;
    private long currentLong;
    private long count;
    private long endLong;

    private final DataType fieldType;

    private final boolean isUnsigned;

    public SimpleInIterator(DataType fieldType) {
        this.fieldType = fieldType;
        this.isUnsigned = fieldType.isUnsigned();
    }

    @Override
    public boolean range(PartitionField from, PartitionField to, boolean lowerBoundIncluded, boolean upperBoundIncluded) {
        Preconditions.checkArgument(from.mysqlStandardFieldType() == to.mysqlStandardFieldType());

        // We have stored a invalid value into the field.
        if (from.lastStatus() != TypeConversionStatus.TYPE_OK
            || to.lastStatus() != TypeConversionStatus.TYPE_OK) {
            count = INVALID_COUNT;
            return false;
        } else if (from.compareTo(to) > 0) {
            count = INVALID_COUNT;
            return true;
        }

        long minValue = from.longValue();
        long maxValue = to.longValue();

        // set the status of this iterator
        count = (maxValue - minValue) + 1;
        if (count < 0) {
            // out of the signed long max value
            count = INVALID_COUNT;
            return true;
        }
        if (!lowerBoundIncluded) {
            count--;
        }
        if (!upperBoundIncluded) {
            count--;
        }
        this.lowerBoundIncluded = lowerBoundIncluded;
        this.upperBoundIncluded = upperBoundIncluded;
        this.firstEnumerated = false;
        this.currentCount = 0;
        this.currentLong = minValue;
        this.endLong = maxValue;
        return true;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public void clear() {
        count = 0;
        currentCount = 0;
        currentLong = 0;
        endLong = -1;
        upperBoundIncluded = false;
        lowerBoundIncluded = false;
        firstEnumerated = false;
    }

    @Override
    public boolean hasNext() {
        if (count == 0 || currentCount >= count) {
            return false;
        }

        // enumerate the first value if the lower bound is included and the first value has not been enumerated.
        if (lowerBoundIncluded && !firstEnumerated) {
            firstEnumerated = true;
            currentCount++;
            return true;
        }
        // try to calc the next value, by add 1
        long newLongValue = currentLong + 1;

        if ((isUnsigned ? UnsignedLongs.compare(newLongValue, endLong) < 0 : newLongValue < endLong)
            || (upperBoundIncluded
            && (isUnsigned ? UnsignedLongs.compare(newLongValue, endLong) <= 0 : newLongValue <= endLong))) {
            currentLong = newLongValue;
            currentCount++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Long next() {
        // get the current datetime value.
        return currentLong;
    }
}
