package ch.hearc.cafheg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MyTestsIT {

    @Test
    void placeholderTest() {
        assertEquals(1, 1);
        assertNotNull(getClass().getResource("/dbunit/empty-dataset.xml"));
    }
}
