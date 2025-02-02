package deque;
import java.util.Iterator;

public class ArrayDeque<Item> implements Deque<Item>, Iterable<Item> {
    private Item[] items;
    private int size;
    private int nextFirst;
    private int nextLast;
    public ArrayDeque() {
        items = (Item[]) new Object[8];
        size = 0;
        nextFirst = 4;
        nextLast = 5;
    }

    private void resize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        int start = nextFirst + 1;
        while ((nextFirst + 1 % items.length) != nextLast) {
            int index = (nextFirst + 1) % items.length;
            a[start] = items[index];
            nextFirst = index;
            start++;
        }
    }

    public void addFirst(Item x) {
            if (size == items.length) {
                resize((int) (size * 2));
            }
            items[nextFirst] = x;
            nextFirst = (nextFirst - 1 + items.length) % items.length;
            size++;

    }
    public void addLast(Item x) {
        if (size == items.length) {
            resize((int) (size * 2));
        }
        items[nextLast] = x;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public int size() {
        return size;
    }
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[i] + " ");
        }
        System.out.println("\n");
    }
    public Item removeFirst() {
        if (size == 0) {
            return null;
        } else {
            Item returnItem = items[nextFirst + 1];
            items[nextFirst + 1] = null;
            nextFirst = (nextFirst + 1) % items.length;
            size--;
            return returnItem;
        }
    }
    public Item removeLast() {
        if (size == 0) {
            return null;
        } else {
            Item returnItem = items[nextLast - 1];
            items[nextLast - 1] = null;
            nextLast = (nextLast - 1 + items.length) % items.length;
            size--;
            return returnItem;
        }
    }
    public Item get(int index) {
        if (index < 0 || index >= size) {
            return null;
        } else {
            return items[(nextFirst + 1 + index) % items.length];
        }
    }
    public Iterator<Item> iterator() {
        return new ArrayDequeIterator();
    }
    private class ArrayDequeIterator implements Iterator<Item> {
        private int wizPos;
        public ArrayDequeIterator() {
            wizPos = 0;
        }
        public boolean hasNext() {
            return wizPos < size;
        }
        public Item next() {
            Item returnItem = get(wizPos);
            wizPos += 1;
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }
}
