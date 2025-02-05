package deque;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 4;
        nextLast = 5;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int start = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            a[i] = items[(start + i) % items.length];
        }
        items = a;
        nextFirst = items.length - 1;
        nextLast = size;
    }

    public void addFirst(T x) {
        if (size == items.length) {
            resize((int) (size * 2));
        }
        items[nextFirst] = x;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size++;
    }
    public void addLast(T x) {
        if (size == items.length) {
            resize((int) (size * 2));
        }
        items[nextLast] = x;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }

    public int size() {
        return size;
    }
    public void printDeque() {
        int current = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            System.out.print(items[current] + " ");
            current = (current + 1) % items.length;
        }
        System.out.println(); // 可选换行
    }
    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            nextFirst = (nextFirst + 1) % items.length;
            T returnItem = items[nextFirst];
            items[nextFirst] = null;
            size--;
            shrinkSize();
            return returnItem;
        }
    }
    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            nextLast = (nextLast - 1 + items.length) % items.length;
            T returnItem = items[nextLast];
            items[nextLast] = null;
            size--;
            shrinkSize();
            return returnItem;
        }
    }

    private void shrinkSize() {
        if (size < items.length / 4) {
            if (items.length == 8) {
                return;
            }
            resize((int) (items.length / 2));
        }

    }
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        } else {
            return items[(nextFirst + 1 + index) % items.length];
        }
    }
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }
    private class ArrayDequeIterator implements Iterator<T> {
        private int wizPos;
        ArrayDequeIterator() {
            wizPos = 0;
        }
        public boolean hasNext() {
            return wizPos < size;
        }
        public T next() {
            T returnItem = get(wizPos);
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
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;
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
