package deque;
import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>,Iterable<T> {
    private static class Node<T> {
        private Node<T> prev;
        private final T item;
        private Node<T> next;

        Node(Node<T> p, T i, Node<T> n) {
            prev = p;
            item = i;
            next = n;
        }

        Node() {
            prev = null;
            item = null;
            next = null;
        }

        @Override
        public String toString() {
            if (item == null) {
                return "null";
            }
            return item.toString();
        }
    }

    private final Node<T> sentinel = new Node<T>();
    private int size;

    public LinkedListDeque() {
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    public void addFirst(T x) {
        Node oldFirst = sentinel.next;
        Node newFirst = new Node(sentinel, x, oldFirst);
        sentinel.next = newFirst;
        oldFirst.prev = newFirst;
        size++;
    }

    public void addLast(T x) {
        Node oldLast = sentinel.prev;
        Node newLast = new Node(oldLast, x, sentinel);
        sentinel.prev = newLast;
        oldLast.next = newLast;
        size++;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println("\n");
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            Node<T> oldFirst = sentinel.next;
            sentinel.next = oldFirst.next;
            oldFirst.next.prev = sentinel;
            size--;
            return oldFirst.item;
        }
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            Node<T> oldLast = sentinel.prev;
            sentinel.prev = oldLast.prev;
            oldLast.prev.next = sentinel;
            size--;
            return oldLast.item;
        }
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        for (Node<T> p = sentinel.next; p != sentinel; p = p.next) {
            if (index == 0) {
                return p.item;
            }
            index--;
        }
        return null;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return (T) getRecursivehelper(sentinel.next, index);
    }

    private T getRecursivehelper(Node<T> currentnode, int index) {
        if (index == 0) {
            return currentnode.item;
        }
        return getRecursivehelper(currentnode.next, index - 1);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node<T> current;

        LinkedListDequeIterator() {
            current = sentinel.next;
        }
        public boolean hasNext() {
            return current != sentinel;
        }
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            T item = current.item;
            current = current.next;
            return item;
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
