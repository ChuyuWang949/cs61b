package deque;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayDeque<String> lld1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();

    }

    @Test
    public void fillEmptyFillAgainTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        // 填充至满（假设初始容量为8）
        for (int i = 0; i < 8; i++) ad.addLast(i);
        // 清空
        for (int i = 0; i < 8; i++) ad.removeFirst();
        // 再次填充并移除
        ad.addLast(100);
        assertEquals(100, (int) ad.removeFirst()); // 检查是否越界
    }

    @Test
    public void ArrayEqualsLinked() {
        Deque<Integer> ad = new ArrayDeque<>();
        Deque<Integer> lld = new LinkedListDeque<>();

        // 填充并比较
        for (int i = 0; i < 100; i++) {
            ad.addLast(i);
            lld.addLast(i);
        }

        // 比较大小
        assertEquals(ad.size(), lld.size());
        assertEquals(ad.equals(lld), true);
    }
    @Test
    public void multiInstanceInteractionTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<>();
        ArrayDeque<String> ad2 = new ArrayDeque<>();

        // 操作 ad1
        ad1.addLast("A");
        ad1.addLast("B");
        ad1.removeFirst();

        // 操作 ad2
        ad2.addLast("X");
        ad2.addLast("Y");
        ad2.removeFirst();

        // 验证 ad1 和 ad2 是否独立
        assertEquals("B", ad1.removeFirst());
        assertEquals("Y", ad2.removeFirst());
    }

    @Test
    public void circularPointerBoundaryTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        // 强制指针移动到数组末尾
        ad.addFirst(1);  // 头指针左移
        ad.addFirst(2);  // 头指针继续左移
        ad.removeLast(); // 尾指针左移

        // 在边界处添加元素
        ad.addLast(3);
        ad.addFirst(4);

        // 预期顺序：4 -> 2 -> 3
        assertEquals(4, (int) ad.removeFirst());
        assertEquals(2, (int) ad.removeFirst());
        assertEquals(3, (int) ad.removeFirst());
    }

    @Test
    public void removeAllThenAddTest() {
        ArrayDeque<Character> ad = new ArrayDeque<>();
        // 填充并移除所有元素
        for (char c = 'a'; c <= 'h'; c++) ad.addLast(c);
        for (int i = 0; i < 8; i++) ad.removeFirst();

        // 确认队列为空后添加新元素
        assertTrue(ad.isEmpty());
        ad.addLast('z');
        assertEquals('z', (char) ad.removeFirst()); // 检查是否越界
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);

    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {


        ArrayDeque<String> lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean>  lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayDeque<Integer>  lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());


    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }


    }
    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        Random rand = new Random();
        int totalCalls = 10000; // 总调用次数
        int[] operationWeights = {3, 3, 1, 1, 1, 1}; // 操作权重：addFirst, addLast, removeFirst, removeLast, isEmpty, size

        for (int i = 0; i < totalCalls; i++) {
            int operation = getRandomOperation(operationWeights, rand);
            switch (operation) {
                case 0: // addFirst
                    ad.addFirst(rand.nextInt(100));
                    break;
                case 1: // addLast
                    ad.addLast(rand.nextInt(100));
                    break;
                case 2: // removeFirst
                    if (!ad.isEmpty()) {
                        ad.removeFirst();
                    }
                    break;
                case 3: // removeLast
                    if (!ad.isEmpty()) {
                        ad.removeLast();
                    }
                    break;
                case 4: // isEmpty
                    ad.isEmpty();
                    break;
                case 5: // size
                    ad.size();
                    break;
                default:
                    throw new IllegalStateException("Unexpected operation: " + operation);
            }

            // 检查队列状态是否合法
            assertTrue("Size should not be negative", ad.size() >= 0);
            assertTrue("isEmpty should be consistent with size", ad.isEmpty() == (ad.size() == 0));
        }
    }

    /**
     * 根据权重随机选择一个操作
     *
     * @param weights 操作权重数组
     * @param rand    随机数生成器
     * @return 操作编号（0到weights.length-1）
     */
    private int getRandomOperation(int[] weights, Random rand) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        int randomValue = rand.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return i;
            }
        }
        throw new IllegalStateException("Should not reach here");
    }
}
