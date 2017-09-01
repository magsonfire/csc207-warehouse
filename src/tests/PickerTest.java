<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import warehouse.PickingManager;
import warehouse.SequencingManager;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;

public class PickerTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  private Worker jim;

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
  }

  @Test
  public void pickerMethods() throws WorkerJobException {
    // Check status
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);

    // Check verify
    exception.expect(WorkerJobException.class);
    jim.getCurrentJob().verify();
  }

  @Test
  public void pickerPickTypes() throws WorkerJobException {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    // Picked wrong item!
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 12&quot;);

    assertEquals(jim.getCurrentJob().getPickingIndex(), 3);

    pickingManager.discardJob(jim);

    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    assertEquals(jim.getCurrentJob().getPickingIndex(), 7);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    // Pick when theres no pick
    assertNull(jim.getCurrentJob());

    exception.expect(WorkerJobException.class);
    jim.nextTask();
  }

  @Test
  public void pickerPickButNoJob() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 2&quot;);
  }

  @Test
  public void pickerEarlyMarshalling() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);
    assertNull(jim.getCurrentJob());
    // assertEquals(jim.getCurrentJob().getItems().size(), 7);
  }

  @Test
  public void pickerOverPick() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    assertEquals(jim.getStatus(), &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    // Extra pick
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    assertEquals(jim.getCurrentJob().getPickingIndex(), 8);
  }

  @Test
  public void pickerWrongSkuSomehow() {
    pickingManager.setStatus(&quot;Jim&quot;, &quot;ready&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 37&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 38&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 21&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 22&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 43&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;pick 44&quot;);
    Job job = jim.getCurrentJob();
    job.getItems().get(0).setSku(&quot;15&quot;);
    pickingManager.setStatus(&quot;Jim&quot;, &quot;to marshalling&quot;);

    // Job was picked wrong, discarded and sent back to picking
    assertTrue(pickingManager.getJobsToDo().contains(job));
  }
}</pre>
</body>
</html>
