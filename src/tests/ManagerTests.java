<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.FileHelper;
import warehouse.InventoryException;
import warehouse.InventoryManager;
import warehouse.Job;
import warehouse.JobManager;
import warehouse.Loader;
import warehouse.LoadingManager;
import warehouse.Location;
import warehouse.NoSuchLocationException;
import warehouse.Pallet;
import warehouse.Picker;
import warehouse.PickingManager;
import warehouse.Sequencer;
import warehouse.SequencingManager;
import warehouse.StockExceedsCapacityException;
import warehouse.WarehouseItem;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;
import warehouse.Zone;

public class ManagerTests {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  private Worker jim;
  private Worker bob;
  private Worker joe;
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

    jim = pickingManager.getWorker(&quot;Jim&quot;);
    bob = sequencingManager.getWorker(&quot;Bob&quot;);
    joe = loadingManager.getWorker(&quot;Joe&quot;);
    //billy = inventoryManager.getWorker(&quot;Billy&quot;);
  }

  /**
   * JobManager tests.
   */
  @Test
  public void jobManagerTranslation() throws WorkerJobException {
    Job job = jobManager.getJobs().get(0);
    assertNotNull(job);

    // Sku testing
    String[] skus = job.getSkus();
    assertNotNull(skus);

    assertEquals(skus[0], &quot;37&quot;);
    assertEquals(skus[1], &quot;38&quot;);
    assertEquals(skus[2], &quot;21&quot;);
    assertEquals(skus[3], &quot;22&quot;);
    assertEquals(skus[4], &quot;43&quot;);
    assertEquals(skus[5], &quot;44&quot;);
    assertEquals(skus[6], &quot;43&quot;);
    assertEquals(skus[7], &quot;44&quot;);

    // Pick Testing
    Location[] pickingOrder = job.getPickingOrder();
    assertEquals(pickingOrder[0].toString(), &quot;B 1 0 0&quot;);
    assertEquals(pickingOrder[7].toString(), &quot;B 1 1 3&quot;);

    Location nextPick = job.getNextPick();
    assertEquals(nextPick.toString(), &quot;B 1 0 0&quot;);
    for (int i = 0; i &lt; 7; i++) {
      job.getNextPick();
    }
    assertNull(job.getNextPick());

    // Order testing
    String[] orders = job.getOrders();
    assertEquals(orders[0], &quot;SES Blue&quot;);
    assertEquals(orders[3], &quot;SE Black&quot;);


    // Worker
    Worker worker = new Picker(&quot;Jerry&quot;, null);
    worker.startJob(job);
    assertEquals(job.getWorker(), worker);

    // Complete
    pickingManager.jobComplete(worker);
    assertNull(job.getWorker());

    // ID
    job.setId(&quot;test&quot;);
    assertEquals(job.getId(), &quot;test&quot;);
  }

  @Test
  public void jobLocation() {
    Job job = jobManager.getJobs().get(0);

    // Location testing
    Location testLocation = new Location(&quot;B&quot;, 3, 2, 1, &quot;37&quot;);
    job.setLocation(testLocation);
    assertEquals(job.getLocation().toString(), &quot;B 3 2 1&quot;);
  }

  @Test
  public void jobContents() throws WorkerJobException {
    Job job = jobManager.getJobs().get(0);
    String[] skus = job.getSkus();

    // Items and pallets
    WarehouseItem item1 = new WarehouseItem(skus[0]);
    WarehouseItem item2 = new WarehouseItem(skus[1]);
    WarehouseItem item3 = new WarehouseItem(skus[2]);
    WarehouseItem item4 = new WarehouseItem(skus[3]);

    job.addItem(item1);
    job.addItem(item2);
    job.addItem(item3);
    job.addItem(item4);
    assertEquals(job.getItems().size(), 4);

    Pallet[] pallets = new Pallet[1];
    pallets[0] = new Pallet();
    pallets[0].addItem(item1);
    pallets[0].addItem(item2);
    pallets[0].addItem(item3);
    pallets[0].addItem(item4);

    job.setPallets(pallets);
    assertEquals(job.getPallets()[0], pallets[0]);

    job.verify();

    job.resetJob();
    assertEquals(job.getItems().size(), 0);
    assertNull(job.getPallets());
  }

  @Test
  public void jobContentsFail() throws WorkerJobException {
    Job job = jobManager.getJobs().get(0);
    String[] skus = job.getSkus();
    jim.startJob(job);

    // Items and pallets
    WarehouseItem item1 = new WarehouseItem(skus[0]);
    WarehouseItem item2 = new WarehouseItem(skus[1]);
    WarehouseItem item3 = new WarehouseItem(skus[2]);
    WarehouseItem item4 = new WarehouseItem(&quot;12&quot;);

    job.addItem(item1);
    job.addItem(item2);
    job.addItem(item3);
    job.addItem(item4);

    exception.expect(WorkerJobException.class);
    job.verify();
  }

  @Test
  public void jobManagerAddRemoval() {
    Job job1 = new Job(new Location(&quot;A&quot;, 0, 1, 2, &quot;39&quot;));
    Job job2 = new Job(new Location(&quot;B&quot;, 1, 0, 3, &quot;40&quot;));

    jobManager.addJob(job1);
    jobManager.addJob(job2);
    jobManager.removeJob(job1);

    ArrayList&lt;Job&gt; jobs = jobManager.getJobs();
    assertEquals(jobs.size(), 2);
    assertEquals(jobs.get(1), job2);

    jobManager.removeJob(job2);
    assertEquals(jobs.size(), 1);
  }

  @Test
  public void processManagerFailure() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    jim.getCurrentJob().resetJob();
    pickingManager.discardJob(jim);

    assertNull(jim.getCurrentJob());
    assertEquals(pickingManager.getJobsToDo().size(), 1);

    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertNotNull(jim.getCurrentJob());
    assertEquals(pickingManager.getJobsToDo().size(), 0);
    assertEquals(pickingManager.getJobsInProgress().size(), 1);
  }

  @Test
  public void processManagerNextJob() throws WorkerJobException {
    assertNotNull(pickingManager.getNextJob());

    sequencingManager.queueJob(null);
    exception.expect(WorkerJobException.class);
    sequencingManager.getNextJob();
  }

  /**
   * Basic worker tests.
   */
  @Test
  public void workerValidity() {

    assertNotNull(jim);
    assertTrue(jim instanceof Picker);
    assertTrue(bob instanceof Sequencer);
    assertTrue(joe instanceof Loader);
    assertEquals(joe.getManager(), loadingManager);
    assertNotNull(pickingManager.getWorker(&quot;Steven&quot;));
  }

  @Test
  public void workerAlreadyHasJob() throws WorkerJobException {
    jim.startJob(pickingManager.getNextJob());

    jobManager.processOrder(&quot;SES Blue&quot;);
    jobManager.processOrder(&quot;SES Red&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);
    exception.expect(WorkerJobException.class);
    jim.startJob(pickingManager.getNextJob());
  }

  @Test
  public void workerNullJob() throws WorkerJobException {
    exception.expect(WorkerJobException.class);
    jim.startJob(null);
  }

  @Test
  public void workerMethods() {
    assertEquals(jim.getRole(), &quot;Picker&quot;);
    assertEquals(joe.getRole(), &quot;Loader&quot;);
    assertEquals(bob.toString(), &quot;Bob&quot;);

    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getLastInstruction(), jim.getCurrentJob().nextInstruction());
  }

  @Test
  public void workerNoJob() throws WorkerJobException {
    // Check status
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    jim.setCurrentJob(null);
    assertNull(jim.getCurrentJob());
    exception.expect(WorkerJobException.class);
    jim.nextTask();
  }

  @Test
  public void workerDoubleJob() throws WorkerJobException {
    // Check status
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    final Job job = jim.getCurrentJob();

    // Place new orders which attemps to give jim a new job
    jobManager.processOrder(&quot;SES Blue&quot;);
    jobManager.processOrder(&quot;SES Red&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);
    jobManager.processOrder(&quot;SE Black&quot;);

    assertEquals(jim.getCurrentJob(), job);
  }

  /**
   * InventoryManager tests.
   */
  @Test
  public void inventoryLocationWrongSize() {
    Location l1 = new Location(&quot;A&quot;, 1, 2, 3, &quot;A&quot;);
    Location l2 = new Location(&quot;A&quot;, 5, 6, 7, &quot;B&quot;);

    assertTrue(inventoryManager.locationObeysDimensions(l1));
    assertFalse(inventoryManager.locationObeysDimensions(l2));
  }

  @Test
  public void inventoryFloorSize() {
    LinkedHashMap&lt;String, Zone&gt; floor = inventoryManager.getFloor();

    assertTrue(floor.entrySet().size() &gt; 0);
  }

  @Test
  public void inventoryRemoveFromEmpty()
      throws NoSuchLocationException, StockExceedsCapacityException {
    Location loc = inventoryManager.getLocation(&quot;15&quot;);
    loc.setInventory(0);

    // Remove what isnt there
    inventoryManager.removeInventory(loc);
    assertEquals(loc.getInventory(), 0);
  }

  @Test
  public void inventoryInitialWrongDimensions() throws InventoryException {
    exception.expect(InventoryException.class);
    inventoryManager.setDimensions(&quot;inventory_dimensions_wrong.csv&quot;);
  }

  @Test
  public void inventoryGetWrongZone() throws NoSuchLocationException {
    exception.expect(NoSuchLocationException.class);
    inventoryManager.getLocationAtCoordinates(&quot;ABSSDFASDF&quot;, 0, 1, 3);
  }

  @Test
  public void inventoryGetWrongLocation() throws NoSuchLocationException {
    exception.expect(NoSuchLocationException.class);
    inventoryManager.getLocationAtCoordinates(&quot;A&quot;, 3, 3, 3);
  }

  @Test
  public void inventoryInitialStockOver() throws InventoryException, StockExceedsCapacityException {
    exception.expect(StockExceedsCapacityException.class);
    inventoryManager.stockFloor(&quot;initial_overstock.csv&quot;);
  }

  @Test
  public void inventoryInitialStockWrongFileType()
      throws InventoryException, StockExceedsCapacityException {
    exception.expect(InventoryException.class);
    inventoryManager.stockFloor(&quot;initial_wrong.csv&quot;);
  }

  @Test
  public void inventoryInitialLayoutOversized() throws InventoryException, NoSuchLocationException {
    exception.expect(NoSuchLocationException.class);
    inventoryManager.csvToFloor(&quot;traversal_table_oversized.csv&quot;);
  }

  @Test
  public void inventoryInitialLayoutWrong() throws InventoryException, NoSuchLocationException {
    exception.expect(InventoryException.class);
    inventoryManager.csvToFloor(&quot;traversal_table_wrong.csv&quot;);
  }

  /**
   * General system test.
   */
  @Test
  public void systemTest() throws NoSuchLocationException, StockExceedsCapacityException {
    // Getters
    assertEquals(system.getJobManager(), jobManager);
    assertEquals(system.getPickingManager(), pickingManager);
    assertEquals(system.getSequencingManager(), sequencingManager);
    assertEquals(system.getLoadingManager(), loadingManager);
    assertEquals(system.getInventoryManager(), inventoryManager);

    // Constructor
    system = new WarehouseSystem();
    ArrayList&lt;String&gt; input = system.getInput();
    assertNotNull(input);
    system.simulate();
    system.endDay();
    assertEquals(system.getInput().size(), 0);
    system.updateInput(&quot;orders.txt&quot;);
    assertEquals(system.getInput(), input);
    system.simulate();
    system.endDay();
    assertEquals(system.getInput().size(), 0);

    // Call functions
    WarehouseSystem.main(new String[] {&quot;orders.txt&quot;});

    Location location = inventoryManager.getLocationAtCoordinates(&quot;A&quot;, 0, 0, 1);
    location.setInventory(2);
    system.replenishInventory(location);
    inventoryManager.replenish(location);
    assertEquals(location.getMax(), location.getInventory());
  }
}

</pre>
</body>
</html>
