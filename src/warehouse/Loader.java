<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

public class Loader extends Worker {

  private int scanIndex = 0;
  
  /**
   * Default constructor for loader.
   * 
   * @param name name of the loader
   * @param manager manager of this loader
   */
  public Loader(String name, ProcessManager manager) {
    super(name, &quot;Loader&quot;, manager);

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
    String[] splitStatus = getStatus().split(&quot; &quot;);
    if (splitStatus[0].equals(&quot;rescans&quot;)) {
      scanIndex = 0;
    } else if (splitStatus[0].equals(&quot;scans&quot;)) {

      // Check the job exists
      if (getCurrentJob() == null) {
        throw new WorkerJobException(&quot;no job to scan items from&quot;);
      }

      if (splitStatus.length &lt; 2) {
        throw new WorkerJobException(&quot;no sku attached to input status&quot;);
      }

      // Get variables and prepare pallets
      String[] skus = getCurrentJob().getSkus();
      int numOrders = getCurrentJob().getOrders().length;
      String scannedSku = getStatus().split(&quot; &quot;)[1];
      int numPallets = (int) Math.ceil(skus.length / numOrders);
      
      // Calculate index
      int expectedSkuIndex =
          (scanIndex % numOrders) * numPallets + (int) Math.floor(scanIndex / numOrders);

      // make sure the item scanned is correct item
      if (scannedSku.equals(skus[expectedSkuIndex])) {
        FileHelper.logEvent(&quot;Item &quot; + skus[expectedSkuIndex] + &quot; in correct sequence&quot;, this);
        scanIndex = (scanIndex + 1) % skus.length;
      } else {
        throw new SequencerJobException(&quot;pallet has wrong sequence of items&quot;);
      }
    } else if (splitStatus[0].equals(&quot;loads&quot;)) {
      LoadingManager loadingManager = (LoadingManager) getManager();
      Truck truck = loadingManager.getTruck();
      if (getCurrentJob() == null) {
        throw new WorkerJobException(&quot;no job to load&quot;);
      }

      for (Pallet p : getCurrentJob().getPallets()) {
        truck.loadPallet(p);
      }
      getManager().jobComplete(this);
    }
  }
}
</pre>
</body>
</html>
