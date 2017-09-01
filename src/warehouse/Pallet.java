<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

import java.util.ArrayList;

public class Pallet {

  private ArrayList&lt;WarehouseItem&gt; items = new ArrayList&lt;WarehouseItem&gt;();

  /**
   * Default constructor.
   */
  public Pallet() {

  }

  /**
   * Add item to the pallet.
   * 
   * @param item to be added
   */
  public void addItem(WarehouseItem item) {
    items.add(item);
  }

  /**
   * Get the items contained in this pallet.
   * 
   * @return ArrayList
   */
  public ArrayList&lt;WarehouseItem&gt; getItems() {
    return items;
  }
}
</pre>
</body>
</html>
