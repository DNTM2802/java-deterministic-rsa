package drsa.utils;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Buffer structure that holds the rotating set of last bytes produced by the generator, and
 * also the confusion pattern. It is essentially a Linked List with a fixed size and rotating
 * elements.
 */
public class Buffer {
    private LinkedList<Long> buffer;
    private int size;

    public Buffer(int size) {
        this.buffer = new LinkedList<Long>();
        this.size = size;
    }

    /** Adds the given byte representation to the end of the Linked List,
     * and removes the first element if the buffer is full.
     * @param b A long representation of a byte (0-255)
     */
    public void add(long b) {
        if (this.buffer.size() >= this.size){
            this.buffer.removeFirst();
        }
        this.buffer.addLast(b);
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        this.buffer.clear();
    }

    public LinkedList<Long> getElements() {
        return buffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Buffer buffer1 = (Buffer) o;
        if (this.size != buffer1.size) return false;
        for (int i = 0; i < this.size; i++) {
            if (!Objects.equals(this.buffer.get(i), buffer1.buffer.get(i))) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.buffer.toString();
    }
}
