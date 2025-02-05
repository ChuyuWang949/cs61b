package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {
    @Test
    public void maxWithoutComTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(new IntegerComparator());

        for(int i = 0; i < 10; i++){
            mad.addLast(i);
        }

        assertEquals(mad.max(), (Integer) 9);
    }

    @Test
    public void maxWithComTest() {
        MaxArrayDeque<String> mad = new MaxArrayDeque<>(new StringComparator());

        mad.addLast("Javaaaaa");
        mad.addLast("Cpppppppp");

        assertEquals(mad.max(), "Javaaaaa");
        assertEquals(mad.max(new StringLengthComparator()), "Cpppppppp");
    }

    public static class IntegerComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    }

    public static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    public static class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }
}
