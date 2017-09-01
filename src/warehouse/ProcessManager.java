<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public abstract class ProcessManager {

  // Map of workers for easy access by name
  private LinkedHashMap&lt;String, Worker&gt; workers = new LinkedHashMap&lt;String, Worker&gt;();
  // Queue of jobs to do
  private ArrayList&lt;Job&gt; jobsToDo = new ArrayList&lt;Job&gt;();
  // Map of jobs in progress
  private ArrayList&lt;Job&gt; jobsInProgress = new ArrayList&lt;Job&gt;();

  // Master system
  private WarehouseSystem system;

  /**
   * Default constructor for ProcessManager.
   * 
   * @param system master system
   */
  public ProcessManager(WarehouseSystem system) {
    this.system = system;
  }

  /**
   * Set the status of a worker.
   * 
   * @param name of the worker
   * @param status description
   */
  public void setStatus(String name, String status) {
    Worker worker = getWorker(name);

    try {
      worker.setStatus(status);

      // If they&#39;re ready, give them a job
      if (status.equals(&quot;ready&quot;)) {
        // If there are jobs to do, give them one, otherwise they can wait
        if (jobsToDo.size() &gt; 0) {
          assignJob(worker, getNextJob());
        }
      } else {
        // Otherwise, this means the worker has performed a task.
        // Need to let the worker know they&#39;ve done the task, and give them the next one
        worker.nextTask();
      }
    } catch (WorkerJobException exp) {
      FileHelper.logError(exp, this);
      if (exp instanceof PickerJobException) {
        // Picker screwed up, instruct them to redo the pick
        Picker picker = (Picker) worker;
        if (picker.getCurrentJob() != null) {
          picker.setInstruction(picker.getCurrentJob().nextInstruction());
        } else {
          picker.setInstruction(&quot;Put it back. You have no job, and therefor, no purpose&quot;);
        }
      } else if (exp instanceof SequencerJobException) {
        // Picker or Sequencer screwed up or the job is invalid, throw it out
        worker.setInstruction(&quot;discard job&quot;);
        discardJob(worker);
      } else if (exp instanceof ReplenisherJobException) {
        // Quick reset the job back to the replenisher
        Replenisher replenisher = (Replenisher) worker;
        replenisher.setInstruction(replenisher.getCurrentJob().nextInstruction());
      }
      system.raiseWarning();
    }
  }

  /**
   * Get the next job that should be worked on.
   * 
   * @return Job
   * @throws WorkerJobException for worker error
   */
  public Job getNextJob() throws WorkerJobException {
    Job job = jobsToDo.remove(0);

    if (job == null) {
      throw new WorkerJobException(&quot;No job in queue&quot;);
    }

    return job;
  }

  /**
   * Assign a job to a given worker.
   * 
   * @param worker to perform job
   * @param job to do
   */
  private void assignJob(Worker worker, Job job) throws WorkerJobException {
    worker.startJob(job);
    jobsInProgress.add(job);
  }

  /**
   * Creates and/or checks status of a worker.
   * 
   * @param name name of the worker
   * @return Worker
   */
  public Worker getWorker(String name) {
    // Get worker from map
    Worker worker = workers.get(name);
    // If no worker exists hire new one
    if (worker == null) {
      worker = hireWorker(name);
    }
    return worker;
  }

  /**
   * Queue job to be performed by worker.
   * 
   * @param job to be done
   */
  public void queueJob(Job job) {
    for (Entry&lt;String, Worker&gt; entry : workers.entrySet()) {
      Worker worker = entry.getValue();
      if (worker.getStatus().equals(&quot;ready&quot;)) {
        try {
          assignJob(worker, job);
          return;
        } catch (WorkerJobException exp) {
          system.raiseWarning();
          FileHelper.logError(exp, this);
        }
      }
    }
    jobsToDo.add(job);
  }

  /**
   * Job is complete, hand it off.
   *
   * @param worker who completed job
   * @throws WorkerJobException for worker error
   */
  public void jobComplete(Worker worker) throws WorkerJobException {
    Job job = worker.getCurrentJob();
    try {
      job.verify();
      jobsInProgress.remove(job);
      job.setWorker(null);
      worker.setCurrentJob(null);
      worker.setInstruction(&quot;&quot;);
    } catch (WorkerJobException exp) {
      // Error in the job, notify sub manager so they can deal with it
      throw new WorkerJobException(exp.getMessage());
    }
  }

  /**
   * Discard job.
   * 
   * @param worker whos job should be discarded
   */
  public void discardJob(Worker worker) {
    Job job = worker.getCurrentJob();
    job.resetJob();
    worker.setCurrentJob(null);
    FileHelper.logEvent(
        &quot;Job &quot; + job.getId() + &quot; items discarded. Sending to beginning of Job queue.&quot;, this);
    jobsInProgress.remove(job);
    system.sendToPicking(job);
  }

  /**
   * Hire designated worker type.
   * 
   * @param name of the worker
   * @return Worker
   */
  public abstract Worker hireWorker(String name);

  /**
   * Add a worker to the worker pool.
   * 
   * @param worker to add
   */
  public void addWorker(Worker worker) {
    workers.put(worker.getName(), worker);
  }

  /**
   * Get the jobs to do.
   * 
   * @ArrayList
   */
  public ArrayList&lt;Job&gt; getJobsToDo() {
    return jobsToDo;
  }

  /**
   * Get the jobs in progress.
   * 
   * @ArrayList
   */
  public ArrayList&lt;Job&gt; getJobsInProgress() {
    return jobsInProgress;
  }

  /**
   * Get the master system.
   * 
   * @return WarehouseSystem
   */
  public WarehouseSystem getSystem() {
    return system;
  }
}
</pre>
</body>
</html>
