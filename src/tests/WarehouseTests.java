<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import warehouse.WarehouseItem;

public class WarehouseTests {

  // WarehouseItem test starts here.
  @Test
  public void getskuWarehouseItemTest() {
    WarehouseItem item1 = new WarehouseItem(&quot;a7980&quot;);
    WarehouseItem item2 = new WarehouseItem();

    assertEquals(item1.getsku(), &quot;a7980&quot;);
    assertEquals(item2.getsku(), null);
  }

  @Test
  public void sizeTest() {
    WarehouseItem item1 = new WarehouseItem(&quot;a7980&quot;);
    WarehouseItem item2 = new WarehouseItem();
    WarehouseItem item3 = new WarehouseItem();
    item3.setSize(10);

    assertEquals(item1.getSize(), 1);
    assertEquals(item2.getSize(), 1);
    assertEquals(item3.getSize(), 10);
  }

  @Test
  public void toStringTest() {
    WarehouseItem item = new WarehouseItem(&quot;a8182&quot;);
    String result = &quot;Warehouse Item sku #a8182&quot;;

    assertEquals(item.toString(), result);
  }
}
</pre>
</body>
</html>
