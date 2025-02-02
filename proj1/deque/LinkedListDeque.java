package deque;
import java.util.Iterator;

public class LinkedListDeque<Item> implements Deque<Item>, Iterable<Item>{
    private static class Node<T> {
        private Node<T> prev;
        private final T item;
        private Node<T> next;

        public Node(Node<T> p, T i, Node<T> n) {
            prev = p;
            item = i;
            next = n;
        }

        public Node() {
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

    private final Node<Item> sentinel = new Node<Item>();
    private int size;

    public LinkedListDeque() {
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    public void addFirst(Item x) {
        Node oldFirst = sentinel.next;
        Node newFirst = new Node(sentinel, x, oldFirst);
        sentinel.next = newFirst;
        oldFirst.prev = newFirst;
        size++;
    }

    public void addLast(Item x) {
        Node oldLast = sentinel.prev;
        Node newLast = new Node(oldLast, x, sentinel);
        sentinel.prev = newLast;
        oldLast.next = newLast;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for(int i = 0; i < size; i++){
            System.out.print(get(i) + " ");
        }
        System.out.println("\n");
    }

    public Item removeFirst() {
        if(size == 0){
            return null;
        }
        else{
            Node<Item> oldFirst = sentinel.next;
            sentinel.next = oldFirst.next;
            oldFirst.next.prev = sentinel;
            size--;
            return oldFirst.item;
        }
    }

    public Item removeLast() {
        if(size == 0){
            return null;
        }
        else{
            Node<Item> oldLast = sentinel.prev;
            sentinel.prev = oldLast.prev;
            oldLast.prev.next = sentinel;
            size--;
            return oldLast.item;
        }
    }

    public Item get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        for (Node<Item> p = sentinel.next; p != sentinel; p = p.next) {
            if (index == 0) {
                return p.item;
            }
            index--;
        }
        return null;
    }

    public Item getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return (Item) getRecursivehelper(sentinel.next, index);
    }

    private Item getRecursivehelper(Node<Item> currentnode, int index) {
        if (index == 0) {
            return currentnode.item;
        }
        return getRecursivehelper(currentnode.next, index - 1);
    }

    public Iterator<Item> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<Item> {
        private Node<Item> current;

        LinkedListDequeIterator() {
            current = sentinel.next;
        }
        public boolean hasNext() {
            return current != sentinel;
        }
        public Item next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            Item item = current.item;
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
        if (!(o instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<?> other = (LinkedListDeque<?>) o;
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
