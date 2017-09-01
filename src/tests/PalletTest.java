<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import warehouse.Pallet;
import warehouse.WarehouseItem;

public class PalletTest {
  @Test
  public void testAddItem() {
    Pallet pallet = new Pallet();
    WarehouseItem item1 = new WarehouseItem(&quot;d12&quot;);
    WarehouseItem item2 = new WarehouseItem(&quot;g34&quot;);
    WarehouseItem item3 = new WarehouseItem(&quot;7172&quot;);
    pallet.addItem(item1);
    pallet.addItem(item2);
    pallet.addItem(item3);

    ArrayList&lt;WarehouseItem&gt; items = new ArrayList&lt;WarehouseItem&gt;();
    items.add(item1);
    items.add(item2);
    items.add(item3);

    assertEquals(pallet.getItems().get(0), items.get(0));
    assertEquals(pallet.getItems().get(1), items.get(1));
    assertNotEquals(pallet.getItems().get(0), items.get(1));
    assertNotEquals(pallet.getItems().get(1), items.get(2));
  }

  @Test
  public void testGetItems() {
    Pallet pallet = new Pallet();
    WarehouseItem item1 = new WarehouseItem(&quot;d12&quot;);
    WarehouseItem item2 = new WarehouseItem(&quot;g34&quot;);
    WarehouseItem item3 = new WarehouseItem(&quot;7172&quot;);
    pallet.addItem(item1);
    pallet.addItem(item2);
    pallet.addItem(item3);

    ArrayList&lt;WarehouseItem&gt; items = new ArrayList&lt;&gt;();
    items.add(item1);
    items.add(item2);
    items.add(item3);

    boolean bool = true;
    for (int i = 0; i &lt; pallet.getItems().size(); i++) {
      if (pallet.getItems().get(i) != items.get(i)) {
        bool = false;
      }
    }
    assertTrue(bool);
  }

}
</pre>
</body>
</html>
