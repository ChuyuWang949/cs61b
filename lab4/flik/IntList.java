package flik;

import java.util.Iterator;
import java.util.LinkedList;

public class IntList {
    public int first;
    public IntList rest;

    public IntList(int f, IntList r) {
        first = f;
        rest = r;
    }

    public static IntList[] partition(IntList lst, int k) {
        IntList[] array = new IntList[k];
        int index = 0;
        IntList L = ;
        while (L != null) {
            array[index++] = L;
            L = L.rest;
        }

        return array;
    }

    public class IteratorOfIterators implements Iterator<Integer> {
        LinkedList<Iterator<Integer>> iterators;
        public int index = 0;

        IteratorOfIterators(LinkedList<Iterator<Integer>> a) {
            iterators = new LinkedList<>();
            for (Iterator<Integer> i : a) {
                if (i.hasNext()) {
                    iterators.add(i);
                }
            }
        }
        @Override
        public boolean hasNext() {
            for (Iterator<Integer> i : iterators) {
                if (i.hasNext()) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public Integer next() {
            Iterator<Integer> iterator = iterators.get(index);
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                index = (index + 1) % iterators.size();
                return next();
            }
        }
    }
}
