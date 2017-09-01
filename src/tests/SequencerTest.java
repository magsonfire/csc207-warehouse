<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.FileHelper;
import warehouse.InventoryManager;
import warehouse.Job;
import warehouse.JobManager;
import warehouse.LoadingManager;
import warehouse.Pallet;
import warehouse.PickingManager;
import warehouse.SequencingManager;
import warehouse.WarehouseItem;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;

public class SequencerTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  //private Worker jim;
  private Worker bob;
  //private Worker joe;
  //private Worker billy;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * Get all the variables from the warehouse system and add orders/workers.
   */
  @Before
  public void initialize() {
    FileHelper.setSilentLogging(true);
    system = new WarehouseSystem();
    jobManager = system.getJobManager();
    pickingManager = system.getPickingManager();
    sequencingManager = system.getSequencingManager();
    loadingManager = system.getLoadingManager();
    inventoryManager = system.getInventoryManager();

    jobManager.processOrder(&quot;SES Blue&quot;);
    jobManager.processOrder(&quot;SES Red&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);

    pickingManager.hireWorker(&quot;Jim&quot;);
    sequencingManager.hireWorker(&quot;Bob&quot;);
    loadingManager.hireWorker(&quot;Joe&quot;);
    inventoryManager.hireWorker(&quot;Billy&quot;);

    //jim = pickingManager.getWorker(&quot;Jim&quot;);
    bob = sequencingManager.getWorker(&quot;Bob&quot;);
    //joe = loadingManager.getWorker(&quot;Joe&quot;);
    //billy = inventoryManager.getWorker(&quot;Billy&quot;);
  }

  @Test
  public void sequencingFewItems() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    sequencingManager.getJobsToDo().get(0).getItems().remove(0);

    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);

    assertEquals(pickingManager.getJobsToDo().size(), 1);
  }

  @Test
  public void sequencingNoItems() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    sequencingManager.getJobsToDo().get(0).getItems().clear();

    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
  }

  @Test
  public void sequencingWrongOrder() throws WorkerJobException {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    Job job = bob.getCurrentJob();
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);

    // Reorder items
    Pallet pallet = job.getPallets()[0];
    WarehouseItem item = pallet.getItems().remove(0);
    pallet.addItem(item);

    exception.expect(WorkerJobException.class);
    job.verify();
  }

  @Test
  public void sequencingBadJobCompletion() throws WorkerJobException {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 38&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 22&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);

    Job job = bob.getCurrentJob();
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;to loading&quot;);
    // Job was sequenced wrong, discarded and sent back to picking
    assertTrue(pickingManager.getJobsToDo().contains(job));
  }

  @Test
  public void sequencingRescan() throws WorkerJobException {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;,  &quot;rescans&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 38&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 22&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);

    Job job = bob.getCurrentJob();
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;to loading&quot;);
    assertTrue(loadingManager.getJobsToDo().contains(job));
  }
  
  @Test
  public void sequencingWrongSkuSomehow() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 38&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 22&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);

    Job job = bob.getCurrentJob();
    job.getPallets()[0].getItems().get(0).setSku(&quot;15&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;to loading&quot;);
    // Job was sequenced wrong, discarded and sent back to picking
    assertTrue(pickingManager.getJobsToDo().contains(job));
  }

 
}
</pre>
</body>
</html>
