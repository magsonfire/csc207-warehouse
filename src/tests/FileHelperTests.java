<!DOCTYPE html>
<html>
<body>
<pre>package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

// import org.junit.After;
// import org.junit.Before;
import org.junit.Test;

import warehouse.FileHelper;

public class FileHelperTests {

  @Test
  public void readWriteTest() {
    String[] lines = new String[] {&quot;one&quot;, &quot;two&quot;, &quot;three&quot;, &quot;four&quot;};
    FileHelper.writeLinesToFile(&quot;test.csv&quot;, lines);

    ArrayList&lt;String&gt; linesIn = FileHelper.getLinesFromFile(&quot;test.csv&quot;);

    for (int i = 0; i &lt; lines.length; i++) {
      assertEquals(lines[i], linesIn.get(i));
    }

  }

  @Test
  public void logTest() {

    FileHelper.logEvent(&quot;Test1&quot;, this);
    FileHelper.logEvent(&quot;Test2&quot;, this);
    FileHelper.logOrders(new String[] {&quot;Order1&quot;, &quot;Order2&quot;, &quot;Order3&quot;, &quot;Order4&quot;}, this);

    FileHelper.writeLogToFile(&quot;test.csv&quot;);
    ArrayList&lt;String&gt; events = FileHelper.getLinesFromFile(&quot;test.csv&quot;);
    assertTrue(events.get(0).contains(&quot;Test1&quot;));
    assertTrue(events.get(1).contains(&quot;Test2&quot;));

    FileHelper.writeOrdersToFile(&quot;test.csv&quot;);
    ArrayList&lt;String&gt; orders = FileHelper.getLinesFromFile(&quot;test.csv&quot;);
    assertTrue(orders.get(0).contains(&quot;Order1&quot;));
    assertTrue(orders.get(1).contains(&quot;Order2&quot;));
    assertTrue(orders.get(2).contains(&quot;Order3&quot;));
    assertTrue(orders.get(3).contains(&quot;Order4&quot;));
  }
}
</pre>
</body>
</html>
