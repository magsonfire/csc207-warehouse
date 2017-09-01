<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
// import static org.junit.Assert.assertTrue;

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
import warehouse.Truck;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;

public class LoaderTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  // private Worker jim;
  // private Worker bob;
  private Worker joe;
  // private Worker billy;

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

    // jim = pickingManager.getWorker(&quot;Jim&quot;);
    // bob = sequencingManager.getWorker(&quot;Bob&quot;);
    joe = loadingManager.getWorker(&quot;Joe&quot;);
    // billy = inventoryManager.getWorker(&quot;Billy&quot;);
  }

  @Test
  public void loadingFull() {
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
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences&quot;);

    // Truck full, send shipment
    Truck truck = loadingManager.getTruck();
    // Fill truck
    for (int i = 0; i &lt; 158; i++) {
      truck.loadPallet(new Pallet());
    }

    loadingManager.setStatus(&quot;Joe&quot;, &quot;ready&quot;);
    loadingManager.setStatus(&quot;Joe&quot;, &quot;loads&quot;);
  }

  @Test
  public void loaderHighestPriorityJobNotReady() {
    jobManager.processOrder(&quot;SES Blue&quot;);
    jobManager.processOrder(&quot;SES Red&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);

    // Pick job index 0 (highest priority on truck)
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

    // Pick job index 1 (second highest priority)
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

    // Sequencer bungles it
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 12&quot;);

    // Sucessfully sequences job index 1
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 38&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 22&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;to loading&quot;);

    // When loader tries to load it, finds out its not the highest priority, waits
    loadingManager.setStatus(&quot;Joe&quot;, &quot;ready&quot;);
    assertEquals(loadingManager.getJobsToDo().size(), 1);
    assertNull(joe.getCurrentJob());
  }

  @Test
  public void loadFillTruck() {
    // Send through picking
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

    // Send through sequencing
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 38&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 22&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;to loading&quot;);

    // Load up truck with fake stuff
    for (int i = 0; i &lt; 159; i++) {
      loadingManager.getTruck().loadPallet(new Pallet());
    }
    assertNotNull(loadingManager.getCurrentTruck());

    // Successfully load last item
    loadingManager.setStatus(&quot;Joe&quot;, &quot;ready&quot;);
    loadingManager.setStatus(&quot;Joe&quot;, &quot;loads&quot;);

    assertNull(loadingManager.getCurrentTruck());
  }

  @Test
  public void loaderLoadInvalidJob() throws WorkerJobException {
    // Send through picking
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

    // Send through sequencing
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;ready&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 37&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 21&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 43&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 38&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 22&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;sequences 44&quot;);
    sequencingManager.setStatus(&quot;Bob&quot;, &quot;to loading&quot;);

    // Accept job
    loadingManager.setStatus(&quot;Joe&quot;, &quot;ready&quot;);

    // Take an item out of the job
    Job job = joe.getCurrentJob();
    job.getPallets()[0].getItems().remove(0);
    loadingManager.setStatus(&quot;Joe&quot;, &quot;loads&quot;);

    // Job is discarded and back on the picking queue
    pickingManager.getJobsToDo().contains(job);
  }
}

</pre>
</body>
</html>
