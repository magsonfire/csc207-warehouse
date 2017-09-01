<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

import java.util.ArrayList;

public class Sequencer extends Worker {

  private int scanIndex = 0;

  /**
   * Default constructor for sequencer.
   * 
   * @param name name of the sequencer
   * @param manager manager of this sequencer
   */
  public Sequencer(String name, ProcessManager manager) {
    super(name, &quot;Sequencer&quot;, manager);
  }

  /**
   * Set current job.
   * 
   * @param job to set
   */
  public void setCurrentJob(Job job) {
    scanIndex = 0;
    super.setCurrentJob(job);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() throws WorkerJobException {
    // Check the sku of the item they sequenced and make sure it is the next sku that should be
    // sequenced
    // all items the ware house makes, use this to set the next item?
    String[] splitStatus = getStatus().split(&quot; &quot;);
    if (splitStatus[0].equals(&quot;rescans&quot;)) {
      scanIndex = 0;
    } else if (splitStatus[0].equals(&quot;sequences&quot;)) {

      // Check the job exists
      if (getCurrentJob() == null) {
        throw new WorkerJobException(&quot;no job to seqeunce items from&quot;);
      }

      if (splitStatus.length &lt; 2) {
        throw new WorkerJobException(&quot;no sku attached to input status&quot;);
      }

      // Get variables and prepare pallets
      String[] skus = getCurrentJob().getSkus();
      int numOrders = getCurrentJob().getOrders().length;
      String scannedSku = getStatus().split(&quot; &quot;)[1];
      // Pallets can store 4 items (8 items = 2 pallets)
      int numPallets = (int) Math.ceil(skus.length / numOrders);
      
      if (scanIndex == 0) {
        getCurrentJob().preparePallets(numPallets);
      }

      // Make sure there are items to sequence
      ArrayList&lt;WarehouseItem&gt; items = getCurrentJob().getItems();
      if (items.size() == 0) {
        throw new SequencerJobException(&quot;no items to sequence for job &quot; + getCurrentJob().getId());
      }

      // Calculate index
      int expectedSkuIndex =
          (scanIndex % numOrders) * numPallets + (int) Math.floor(scanIndex / numOrders);
      // make sure the item scanned is correct item
      if (scannedSku.equals(skus[expectedSkuIndex])) {
        WarehouseItem nextItem = null;
        for (WarehouseItem item : items) {
          if (item.getsku().equals(skus[expectedSkuIndex])) {
            nextItem = item;
            items.remove(item);
            break;
          }
        }
        // Oops, there&#39;s a problem, missing item!
        if (nextItem == null) {
          scanIndex = 0;
          throw new SequencerJobException(&quot;missing items to sequence&quot;);
        } else {
          ((SequencingManager) getManager()).sequenceItem(this, expectedSkuIndex % numPallets,
              nextItem);
        }
        scanIndex = (scanIndex + 1) % skus.length;
      } else {
        throw new SequencerJobException(&quot;wrong item sequenced&quot;);
      }
    } else if (getStatus().equals(&quot;to loading&quot;)) {
      // Everything&#39;s honkey dorry, job is complete
      getManager().jobComplete(this);
    }
  }
}
</pre>
</body>
</html>
