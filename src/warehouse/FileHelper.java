<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHelper {

  private static boolean fancyLogging = true;
  private static boolean silentLogging = false;
  private static ArrayList&lt;String&gt; log = new ArrayList&lt;String&gt;();
  private static ArrayList&lt;String&gt; orders = new ArrayList&lt;String&gt;();

  // Logging information
  private static final Logger logger = Logger.getLogger(WarehouseSystem.class.getName());
  private static final Handler consoleHandler = new ConsoleHandler();

  private static int prevLogTicker = -1;
  private static int logTicker = 0;

  /**
   * Tick the log, this is to more easily identify steps in the simulation.
   */
  public static void tickLog() {
    logTicker++;
  }

  /**
   * Initialize the logger.
   */
  private static void initializeLogger() {
    // Initialize logger
    if (logger.getHandlers().length == 0) {
      logger.setLevel(Level.ALL);

      // Set console handler
      logger.addHandler(consoleHandler);
      logger.setUseParentHandlers(false);
      consoleHandler.setLevel(Level.ALL);
      // java.util.logging.SimpleFormatter.format = &quot;%4$s: %5$s%n&quot;;
      LogFormatter consoleFormatter = new LogFormatter();
      consoleHandler.setFormatter(consoleFormatter);

      // Set file handler
      FileHandler fileHandler;
      try {
        fileHandler = new FileHandler(&quot;./log.txt&quot;);
        logger.addHandler(fileHandler);
        // SimpleFormatter fileFormatter = new SimpleFormatter();
        fileHandler.setFormatter(consoleFormatter);
      } catch (SecurityException exp) {
        exp.printStackTrace();
      } catch (IOException exp) {
        exp.printStackTrace();
      }
    }
  }

  /**
   * Get log prefix according to the tick. If its the first for the tick, give a number, otherwise
   * whitespace.
   */
  private static String getLogPrefix() {
    if (logTicker != prevLogTicker) {
      prevLogTicker = logTicker;

      return String.format(&quot;%3s&quot;, logTicker) + &quot;: &quot;;
    } else {
      // Return 4 spaces to align
      return new String(new char[5]).replace(&quot;\0&quot;, &quot; &quot;);
    }
  }

  /**
   * Set silent logging.
   * 
   * @param silent whether its silent
   */
  public static void setSilentLogging(boolean silent) {
    silentLogging = silent;
  }

  /**
   * Get a list of lines from a given file.
   * 
   * @return ArrayList
   */
  public static ArrayList&lt;String&gt; getLinesFromFile(String file) {
    BufferedReader br = null;
    FileReader fr = null;
    String line = &quot;&quot;;
    ArrayList&lt;String&gt; lines = new ArrayList&lt;String&gt;();
    try {
      fr = new FileReader(file);
      br = new BufferedReader(fr);
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException exp) {
      exp.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
      if (fr != null) {
        try {
          fr.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
    }
    return lines;
  }

  /**
   * Write lines to file.
   * 
   * @param file where to write it
   * @param lines to write
   */
  public static void writeLinesToFile(String file, String[] lines) {
    BufferedWriter bw = null;
    FileWriter fw = null;
    try {
      fw = new FileWriter(file);
      bw = new BufferedWriter(fw);
      for (String line : lines) {
        bw.write(line + &quot;\n&quot;);
      }
    } catch (IOException exp) {
      exp.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
    }
  }


  /**
   * Log an event in the warehouse.
   * 
   * @param event event to log
   * @param reportingClass sending the event
   */
  // public static void logEvent(String event) { FileHelper.logEvent(event, null); };
  public static void logEvent(String event, Object reportingClass) {
    initializeLogger();
    String logPrefix = getLogPrefix();
    String logPostfix = &quot;&quot;;
    if (reportingClass != null) {
      logPostfix = &quot; :&quot; + reportingClass.getClass().getSimpleName();
    }
    String eventMessage = String.format(&quot;%1$-65s&quot;, logPrefix + event) + logPostfix;
    if (!silentLogging) {
      if (fancyLogging) {
        logger.log(Level.INFO, eventMessage);
      } else {
        System.out.println(eventMessage);
      }
    }
    log.add(eventMessage);
  }

  /**
   * Log an error in the warehouse.
   * 
   * @param exp that accompanies error
   * @param reportingClass sending the error
   */
  // public static void logError(Exception exp) { FileHelper.logError(exp, null); }
  public static void logError(Exception exp, Object reportingClass) {
    initializeLogger();
    String logPrefix = getLogPrefix();
    String logPostfix = &quot;&quot;;
    if (reportingClass != null) {
      logPostfix = &quot; :&quot; + reportingClass.getClass().getSimpleName();
    }
    String errorMessage =
        String.format(&quot;%1$-65s&quot;, logPrefix + &quot;Error: &quot; + exp.getMessage()) + logPostfix;
    if (!silentLogging) {
      if (fancyLogging) {
        logger.log(Level.SEVERE, errorMessage, exp);
      } else {
        System.out.println(errorMessage);
      }
    }
    log.add(errorMessage);
  }

  /**
   * Log orders.
   * 
   * @param orders to log
   */
  // public static void logOrders(String[] orders) { FileHelper.logOrders(orders, null); }
  public static void logOrders(String[] orders, Object reportingClass) {
    initializeLogger();
    String logPrefix = getLogPrefix();
    String logPostfix = &quot;&quot;;
    if (reportingClass != null) {
      logPostfix = &quot; :&quot; + reportingClass.getClass().getSimpleName();
    }
    for (int i = 0; i &lt; orders.length; i++) {
      String orderMessage =
          String.format(&quot;%1$-65s&quot;, logPrefix + orders[i] + &quot; is on a truck&quot;) + logPostfix;
      if (!silentLogging) {
        if (fancyLogging) {
          logger.log(Level.INFO, orderMessage);
        } else {
          System.out.println(orderMessage);
        }
      }
      FileHelper.orders.add(orders[i]);
    }
  }

  /**
   * Write log events to file.
   * 
   * @param file name of file
   */
  public static void writeLogToFile(String file) {
    if (log.size() &gt; 0) {
      writeLinesToFile(file, log.toArray(new String[log.size()]));
    }
  }

  /**
   * Write orders to file.
   * 
   * @param file name of file
   */
  public static void writeOrdersToFile(String file) {
    writeLinesToFile(file, orders.toArray(new String[orders.size()]));
  }
}
</pre>
</body>
</html>
