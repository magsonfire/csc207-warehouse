<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

public class Replenisher extends Worker {

  /**
   * Default constructor for replenisher.
   * 
   * @param name name of the replenisher
   * @param manager manager of this replenisher
   */
  public Replenisher(String name, ProcessManager manager) {
    super(name, &quot;Replenisher&quot;, manager);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() throws WorkerJobException {
    if (getCurrentJob() == null) {
      throw new WorkerJobException(&quot;no job to replenish.&quot;);
    }

    String[] splitStatus = getStatus().split(&quot; &quot;);
    String reportedSku = splitStatus[1];
    String expectedSku = getCurrentJob().getSkus()[0];
    // Check that we&#39;re replenishing the right sku
    if (splitStatus[0].equals(&quot;replenish&quot;) &amp;&amp; !reportedSku.equals(expectedSku)) {
      throw new ReplenisherJobException(&quot;wrong sku to replenish&quot;);
    }

    getManager().jobComplete(this);
  }
}
</pre>
</body>
</html>
