import org.junit.Test;

import junit.framework.TestCase;

public class TestFailure extends TestCase {

    @Test
    public void testFailure() {
        fail();
    }
    
    @Test
    public void testSuccess() {
        assertEquals("success", 2+2, 4);
    }
}
