package flik;

import org.junit.Test;

public class FilkTest {
    @Test
    public void test() {
        int a = 129;
        int b = 129;
        assert Flik.isSameNumber(a, b);
    }
}
