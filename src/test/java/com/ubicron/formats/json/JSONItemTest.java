package com.ubicron;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for JSONItem
 */
public class JSONItemTest {

    @Test
    public void structParse() {
        try {
            JSONItem item = new JSONItem("{\"ye\": 0, \"ye2\": \"test\"}");
            assertTrue(item.type == JSONItem.Types.STRUCT);
            assertTrue(item.itemOrder.get(0).equals("ye"));
            assertTrue(item.structValue.get("ye").type == JSONItem.Types.INT);
            assertTrue(item.structValue.get("ye").intValue == 0);
            assertTrue(item.itemOrder.get(1).equals("ye2"));
            assertTrue(item.structValue.get("ye2").type == JSONItem.Types.STRING);
            assertTrue(item.structValue.get("ye2").stringValue.equals("test"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        
    }

    @Test
    public void print() {
        String answer = "{\n    \"ye\": 0,\n    \"ye2\": \"test\"\n}";
        try {
            JSONItem item = new JSONItem("{\"ye\": 0, \"ye2\": \"test\"}");
            assertTrue(item.type == JSONItem.Types.STRUCT);
            // System.err.println(answer);
            // System.err.println(item.print());
            assertTrue(item.print().equals(answer));
        }  catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void get() {
        try {
            JSONItem item = new JSONItem("{\"ye\": 0, \"ye2\": [0, 1, 2] }");
            // System.err.println("ye");
            assertTrue(item.get("ye2.1").type == JSONItem.Types.INT);
            assertTrue(item.get("ye2.1").intValue == 1);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
