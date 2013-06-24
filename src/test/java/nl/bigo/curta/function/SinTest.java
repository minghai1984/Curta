package nl.bigo.curta.function;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SinTest {

    private final Function function = new Sin();

    @Test
    public void evalTest() {

        double value = -500.0;

        assertThat((Double)(function.eval(value)), is(Math.sin(value)));

        value = 500.00001;

        assertThat((Double)(function.eval(value)), is(Math.sin(value)));
    }

    @Test(expected = RuntimeException.class)
    public void evalTestInvalid1() {
        function.eval();
    }

    @Test(expected = RuntimeException.class)
    public void evalTestInvalid2() {
        function.eval(null);
    }

    @Test(expected = RuntimeException.class)
    public void evalTestInvalid3() {
        function.eval("Foo");
    }
}