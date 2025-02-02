package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> lst = new AListNoResizing<>();
        BuggyAList<Integer> bst = new BuggyAList<>();
        for (int i = 4; i < 7; i += 1) {
            lst.addLast(i);
            bst.addLast(i);
        }
        for (int i = 0; i < 3; i += 1) {
            assertEquals(lst.removeLast(), bst.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> BL = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                BL.addLast(randVal);
            }
            if (operationNumber == 1) {
                if(L.size() != 0){
                    int last = L.getLast();
                    int lastB = BL.getLast();
                    assertEquals(last, lastB);
                }
            }
            if (operationNumber == 2) {
                if(L.size() != 0){
                    int last = L.removeLast();
                    int lastB = BL.removeLast();
                    assertEquals(last, lastB);
                }
            }
        }
    }
}
