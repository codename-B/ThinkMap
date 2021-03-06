/*
 * Copyright 2014 Matthew Collins
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

package uk.co.thinkofdeath.thinkcraft.shared.building;

import uk.co.thinkofdeath.thinkcraft.shared.platform.Platform;
import uk.co.thinkofdeath.thinkcraft.shared.platform.buffers.UByteBuffer;
import uk.co.thinkofdeath.thinkcraft.shared.platform.buffers.UShortBuffer;
import uk.co.thinkofdeath.thinkcraft.shared.platform.buffers.ViewBuffer;

public class DynamicBuffer {

    private static final boolean IS_LITTLE_ENDIAN = detectByteOrder();
    private final boolean littleEndian;

    private UByteBuffer buffer;
    private ViewBuffer dataStream;
    private int offset = 0;

    /**
     * Creates a DynamicBuffer which resizes as it needs more space.
     * The endianness of the buffer is that of the current system.
     * The starting start has a minimum value of 16.
     *
     * @param size
     *         The starting size of the buffer
     */
    public DynamicBuffer(int size) {
        this(size, IS_LITTLE_ENDIAN);
    }

    public DynamicBuffer(int size, boolean littleEndian) {
        this.littleEndian = littleEndian;
        if (size < 16) size = 16;
        buffer = Platform.alloc().ubyteBuffer(size);
        dataStream = Platform.alloc().viewBuffer(buffer, littleEndian, 0, buffer.byteSize());
    }

    /**
     * Adds a single byte to the buffer
     *
     * @param val
     *         The byte to the buffer
     */
    public void add(int val) {
        if (offset >= buffer.size()) {
            resize();
        }
        buffer.set(offset++, val);
    }

    /**
     * Adds a unsigned short to the buffer
     *
     * @param val
     *         The short to add
     */
    public void addUnsignedShort(int val) {
        if (offset + 1 >= buffer.size()) {
            resize();
        }
        if (littleEndian) {
            buffer.set(offset, val & 0xFF);
            buffer.set(offset + 1, (val >> 8) & 0xFF);
        } else {
            buffer.set(offset, (val >> 8) & 0xFF);
            buffer.set(offset + 1, val & 0xFF);
        }
        offset += 2;
    }

    public void addInt(int val) {
        if (offset + 3 >= buffer.size()) {
            resize();
        }
        dataStream.setInt32(offset, val);
        offset += 4;
    }

    /**
     * Adds a float to the buffer
     *
     * @param val
     *         The float to add
     */
    public void addFloat(float val) {
        if (offset + 3 >= buffer.size()) {
            resize();
        }
        dataStream.setFloat32(offset, val);
        offset += 4;
    }

    // Doubles the size of the buffer
    private void resize() {
        UByteBuffer oldBuffer = buffer;
        buffer = Platform.alloc().ubyteBuffer(buffer.size() * 2);
        buffer.set(0, oldBuffer);
        dataStream = Platform.alloc().viewBuffer(buffer, littleEndian, 0, buffer.byteSize());
    }

    /**
     * Resets the buffer for reused
     */
    public void reset() {
        offset = 0;
    }

    /**
     * Returns a view into the buffer sized at the final size of the buffer
     *
     * @return The view into the array
     */
    public UByteBuffer getArray() {
        return buffer;
    }

    public int getOffset() {
        return offset;
    }

    private static boolean detectByteOrder() {
        UShortBuffer buffer = Platform.alloc().ushortBuffer(1);
        buffer.set(0, 1);
        return Platform.alloc().ubyteBuffer(buffer, 0, 2).get(0) == 1;
    }
}
