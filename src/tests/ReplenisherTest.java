<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.FileHelper;
import warehouse.InventoryManager;
import warehouse.Job;
import warehouse.JobManager;
import warehouse.LoadingManager;
import warehouse.Location;
import warehouse.PickingManager;
import warehouse.SequencingManager;
import warehouse.StockExceedsCapacityException;
import warehouse.WarehouseSystem;
import warehouse.Worker;

public class ReplenisherTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  //private Worker jim;
  //private Worker bob;
  //private Worker joe;
  private Worker billy;

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
    //bob = sequencingManager.getWorker(&quot;Bob&quot;);
    //joe = loadingManager.getWorker(&quot;Joe&quot;);
    billy = inventoryManager.getWorker(&quot;Billy&quot;);
  }

  @Test
  public void replenisherNoJob() {
    assertNull(billy.getCurrentJob());
    Location loc = inventoryManager.getLocation(&quot;2&quot;);
    int inventoryBefore = loc.getInventory();
    inventoryManager.setStatus(&quot;Billy&quot;, &quot;replenish 2&quot;);
    assertEquals(loc.getInventory(), inventoryBefore);
  }

  @Test
  public void replenisherWrongReplenish() {
    Location loc = inventoryManager.getLocation(&quot;15&quot;);
    // Clear jobs
    inventoryManager.getJobsToDo().clear();
    inventoryManager.queueJob(new Job(loc));
    inventoryManager.setStatus(&quot;Billy&quot;, &quot;ready&quot;);
    inventoryManager.setStatus(&quot;Billy&quot;, &quot;replenish 5&quot;);
    assertEquals(billy.getLastInstruction(), billy.getCurrentJob().nextInstruction());
  }

  @Test
  public void replenisherJobSuccess() throws StockExceedsCapacityException {
    // Clear other jobs
    inventoryManager.getJobsToDo().clear();

    Location loc = inventoryManager.getLocation(&quot;25&quot;);
    loc.setInventory(5);
    inventoryManager.checkAndReplenish(loc);
    inventoryManager.setStatus(&quot;Billy&quot;, &quot;ready&quot;);
    inventoryManager.setStatus(&quot;Billy&quot;, &quot;replenishes 25&quot;);
    assertEquals(loc.getInventory(), loc.getMax());
  }
}

</pre>
</body>
</html>
