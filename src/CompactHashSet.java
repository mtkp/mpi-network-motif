// CompactHashSet.java

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.NoSuchElementException;

public class CompactHashSet implements Serializable {

    private static final int DEFAULT_CAPACITY = 29;
    private static final int STARTING_BUCKET_SIZE = 4;
    private static final int NULL_ELEMENT = -1;

    private int[][] table;
    private int size;

    public CompactHashSet() {
        this(DEFAULT_CAPACITY);
    }

    public CompactHashSet(int tableSize) {
        if (tableSize < 0) {
            throw new IllegalArgumentException(
                "Argument out of range (must be non-negative).");
        }

        size = 0;
        table = new int[(tableSize == 0 ? 1 : tableSize)][];
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
    }

    public CompactHashSet copy() {
        CompactHashSet copy = new CompactHashSet(table.length);
        Iter iter = iterator();
        while (iter.hasNext()) {
            copy.add(iter.next());
        }
        return copy;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void add(int element) {
        if (element < 0) {
            throw new IllegalArgumentException(
                "Argument out of range (must be non-negative).");
        }

        int bucket = hash(element) % table.length;
        if (table[bucket] == null) {

            // create a bucket if it does not exist
            table[bucket] = new int[STARTING_BUCKET_SIZE];
            for (int i = 1; i < table[bucket].length; i++) {
                table[bucket][i] = NULL_ELEMENT;
            }

            // add element to bucket
            table[bucket][0] = element;
            size++;
            return;
        }

        // check bucket if element already exists
        for (int i = 0; i < table[bucket].length; i++) {
            if (table[bucket][i] == element) {
                return;
            }
        }

        // try to add element if there is space
        for (int i = 0; i < table[bucket].length; i++) {
            if (table[bucket][i] == NULL_ELEMENT) {
                table[bucket][i] = element;
                size++;
                return;
            }
        }

        // otherwise grow the bucket and add to first new position
        int previousLength = table[bucket].length;
        grow(bucket);
        table[bucket][previousLength] = element;
        size++;
    }

    public boolean contains(int element) {
        if (element < 0) {
            return false;
        }

        int bucket = hash(element) % table.length;
        if (table[bucket] == null) {
            return false;
        } else {
            for (int index = 0; index < table[bucket].length; index++) {
                if (table[bucket][index] == element) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean remove(int element) {
        if (element < 0) {
            return false;
        }

        int bucket = hash(element) % table.length;
        if (table[bucket] == null) {
            return false;
        } else {
            for (int index = 0; index < table[bucket].length; index++) {
                if (table[bucket][index] == element) {
                    table[bucket][index] = NULL_ELEMENT;
                    size--;
                    return true;
                }
            }
            return false;
        }
    }

    // increase the size of the bucket at the given index
    private void grow(int bucketIndex) {
        // double the bucket size
        int[] newBucket = new int[table[bucketIndex].length * 2];

        int index = 0;
        for (; index < table[bucketIndex].length; index++) {
            newBucket[index] = table[bucketIndex][index];
        }
        for (; index < newBucket.length; index++) {
            newBucket[index] = NULL_ELEMENT;
        }
        table[bucketIndex] = newBucket;
    }

    private int hash(int element) {
        return element;
    }

    public Iter iterator() {
        return new Iter(this);
    }

    @Override
    public String toString() {
        String s = "[";
        Iter iter = iterator();
        while (iter.hasNext()) {
            s = s + iter.next() + ", ";
        }
        s = s + "]";
        return s;
    }

    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        table = new int[ois.readInt()][];
        int elements = ois.readInt();
        for (; elements > 0; elements--) {
            add(ois.readInt());
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeInt(table.length);
        oos.writeInt(size());
        Iter iter = iterator();
        while (iter.hasNext()) {
            oos.writeInt(iter.next());
        }
    }

    public static class Iter {

        private CompactHashSet set;
        private int row;
        private int col;
        private int prevCol;
        private int prevRow;

        public Iter(CompactHashSet set) {
            this.set = set;
            row = 0;
            col = -1;
            prevRow = row;
            prevCol = col;

            moveToNext();
        }

        private void moveToNext() {
            col++;
            while (row < set.table.length) {
                if (set.table[row] != null && col < set.table[row].length) {
                    for (; col < set.table[row].length; col++) {
                        if (set.table[row][col] != NULL_ELEMENT) {
                            return;
                        }
                    }
                }
                col = 0;
                row++;
            }
        }

        public boolean hasNext() {
            return row < set.table.length;
        }

        public int next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            prevRow = row;
            prevCol = col;
            moveToNext();
            return set.table[prevRow][prevCol];
        }

        public void remove() throws IllegalStateException {
            if (prevCol == -1 || set.table[prevRow][prevCol] == NULL_ELEMENT) {
                throw new IllegalStateException();
            }
            set.table[prevRow][prevCol] = NULL_ELEMENT;
            set.size--;
        }
    }
}