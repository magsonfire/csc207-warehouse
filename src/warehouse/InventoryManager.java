<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * The manager of inventory, handles locations, inventory levels, replenish requests.
 *
 */
public class InventoryManager extends ProcessManager {
  /**
   * A map of zones that contain inventory.
   */
  private LinkedHashMap&lt;String, Zone&gt; floor = new LinkedHashMap&lt;String, Zone&gt;();
  /**
   * A map of all SKUs pointing to their locations in the floor.
   */
  private HashMap&lt;String, Location&gt; locationMap = new HashMap&lt;String, Location&gt;();
  // Maximum number of aisles per zone
  private int aisles;
  // Maximum number of racks per aisle
  private int racks;
  // Maximum number of levels per rack
  private int levels;
  // Maximum inventory of a level
  private int max;
  // Minimum inventory of a level
  private int min;

  /**
   * Get the zones in the floor.
   * 
   * @return The zones in the floor.
   */
  public LinkedHashMap&lt;String, Zone&gt; getFloor() {
    return floor;
  }

  /**
   * Construct an InventoryManager and the floor it manages, using a given traversal file as a guide
   * to the locations of the items. Assume all levels have the same maximum and minimum capacities.
   * 
   * @param system The WarehouseSystem this manager communicates with
   * @param dimensionFile The file that holds the dimensions of the warehouse floor
   * @param traversalFile The file to generate a map of the inventory
   * @param initialFile The file listing locations that start with less than full inventory
   */
  public InventoryManager(WarehouseSystem system, String dimensionFile, String traversalFile,
      String initialFile) {
    // Connect to WarehouseSystem
    super(system);
    // Initialize attributes
    try {
      setDimensions(dimensionFile);
    } catch (InventoryException exp) {
      FileHelper.logError(exp, this);
    }
    try {
      csvToFloor(traversalFile);
    } catch (NoSuchLocationException exp) {
      FileHelper.logError(exp, this);
      System.out.println(&quot;Please double-check initial.csv to ensure location dimensions&quot;
          + &quot;don&#39;t exceed the maximum numbers of aisles, racks, or levels.&quot;);
    } catch (InventoryException exp2) {
      FileHelper.logError(exp2, this);
    }

    // Fill floor with inventory
    try {
      stockFloor(initialFile);
    } catch (StockExceedsCapacityException exp) {
      FileHelper.logError(exp, this);
      System.out.println(&quot;Please double-check initial.csv to ensure stock levels&quot;
          + &quot;don&#39;t exceed the maximum capacity of locations in the inventory.&quot;);
    } catch (InventoryException exp2) {
      FileHelper.logError(exp2, this);
    }
  }

  /**
   * Set the dimensions of the warehouse that the floor will conform to. Follows format ailes,
   * racks, levels, max inventory, min inventory
   * 
   * @param inputFile The CSV file containing the dimensions of the floor
   */
  public void setDimensions(String inputFile) throws InventoryException {
    String[] settings = FileHelper.getLinesFromFile(inputFile).get(0).split(&quot;,&quot;);
    // Check the correct number of inputs
    if (settings.length &lt; 5) {
      throw new InventoryException(&quot;Dimension file has incorrect number of fields&quot;);
    }

    aisles = Integer.parseInt(settings[0]);
    racks = Integer.parseInt(settings[1]);
    levels = Integer.parseInt(settings[2]);
    max = Integer.parseInt(settings[3]);
    min = Integer.parseInt(settings[4]);
  }

  /**
   * Generates the inventory floor&#39;s locations using a traversal table.
   * 
   * @param inputFile The CSV file based on which to organize the locations in the floor
   * @throws NoSuchLocationException if a row provides dimensions that should not exist in the floor
   */
  public void csvToFloor(String inputFile) throws InventoryException, NoSuchLocationException {
    // Generate an array of size 5 to get location values from
    ArrayList&lt;String&gt; locations = FileHelper.getLinesFromFile(inputFile);
    // Sort array to ensure floor is ordered
    Collections.sort(locations);
    // Go through strings in array from file to populate map
    for (int i = 0; i &lt; locations.size(); i++) {
      // Check that this row contains enough values for location
      String[] line = locations.get(i).split(&quot;,&quot;);
      if (line.length != 5) {
        throw new InventoryException(
            &quot;Traversal file contains missing values in row &quot; + (i + 1) + &quot;. Verify input file.&quot;);
      } else {
        Location location = new Location(locations.get(i), max, min);
        if (locationObeysDimensions(location)) {
          // Add to floor
          addToFloor(location);
          // Add to map
          locationMap.put(location.getSku(), location);
        } else {
          throw new NoSuchLocationException(&quot;Please check the dimensions at row &quot; + (i + 1) + &quot;.&quot;
              + &quot; They exceed the warehouse dimensions provided.&quot;);
        }
      }
    }
  }

  /**
   * Checks that numbers of zones, aisles, racks, and levels obeys their respective dimensions in
   * the inventory floor. E.g.: a location can&#39;t be located at rack index 3 if a given aisle only
   * has 3 racks (maximum of index 2).
   * 
   * @param location to be added to the floor
   */
  public boolean locationObeysDimensions(Location location) {
    // Index of dimension should at most one less than # of that dimension
    if (location.getAisle() &gt;= aisles || location.getRack() &gt;= racks
        || location.getLevel() &gt;= levels) {
      return false;
    }
    return true;
  }

  /**
   * Add locations to floor, creating new zone if necessary.
   * 
   * @param location to be added to the floor
   */
  public void addToFloor(Location location) {
    Zone zone = floor.get(location.getZone());
    if (zone == null) {
      zone = new Zone(location.getZone(), aisles, racks, levels);
      floor.put(location.getZone(), zone);
    }
    zone.getLocations().add(location);
  }

  /**
   * Stock all locations in the warehouse with according to the input CSV file.
   * 
   * @param initialFile The input file, a CSV of all locations and their inventory levels
   * @throws StockExceedsCapacityException if an input line has stock level exceeding max capacity
   */
  public void stockFloor(String initialFile)
      throws InventoryException, StockExceedsCapacityException {
    // Turn input file into an Array of strings
    ArrayList&lt;String&gt; locationStock = FileHelper.getLinesFromFile(initialFile);
    // Stock levels not listed in initialFile to full
    stockEmptyLevels();
    // Iterate through each string to get location data for less than full levels
    for (int i = 0; i &lt; locationStock.size(); i++) {
      // Turn the first four parts of the string into location details
      String line = locationStock.get(i);
      // Remove commas
      String[] details = line.split(&quot;,&quot;);
      // Check that there are enough values in this line
      if (details.length != 5) {
        throw new InventoryException(
            &quot;Traversal file contains extra values in row &quot; + (i + 1) + &quot;. Verify input file.&quot;);
      } else {
        try {
          Location location = getLocationAtCoordinates(details[0], Integer.valueOf(details[1]),
              Integer.valueOf(details[2]), Integer.valueOf(details[3]));
          int amount = Integer.parseInt(details[4]);
          // Check that amount is not above max capacity
          if (amount &gt; location.getMax()) {
            throw new StockExceedsCapacityException(&quot;The input file suggests a stock level in row &quot;
                + (i + 1) + &quot; that exceeds maximum capacity &quot; + location.getMax());
          } else {
            location.setInventory(amount);
            // Replenish racks as necessary (otherwise orders stall)
            checkAndReplenish(location);
          }
        } catch (NoSuchLocationException exp) {
          // TODO Auto-generated catch block
          exp.printStackTrace();
        }
      }
    }
  }

  /**
   * Fills all empty levels in the inventory floor.
   */
  public void stockEmptyLevels() {

    for (Entry&lt;String, Zone&gt; entry : floor.entrySet()) {
      Zone zone = entry.getValue();
      for (Location location : zone.getLocations()) {
        try {
          location.setInventory(location.getMax());
        } catch (StockExceedsCapacityException exp) {
          exp.printStackTrace();
        }
      }
    }
  }

  /**
   * Report the stock of all levels in the warehouse, and output to final.csv
   */
  public void reportFloorStock() {
    ArrayList&lt;String&gt; locationStock = new ArrayList&lt;String&gt;();

    // Iterate through locations

    for (Entry&lt;String, Zone&gt; entry : floor.entrySet()) {
      Zone zone = entry.getValue();
      for (Location location : zone.getLocations()) {
        String locationString = location.toString();
        if (location.getInventory() == location.getMax()) {
          continue;
        }
        // Add commas
        String locationcsv = locationString.replaceAll(&quot; &quot;, &quot;,&quot;);
        locationStock.add(locationcsv + &quot;,&quot; + location.getInventory());
      }
    }

    FileHelper.writeLinesToFile(&quot;final.csv&quot;,
        locationStock.toArray(new String[locationStock.size()]));
  }

  /**
   * Get location at coordinates.
   * 
   * @param zoneName of location
   * @param aisle of location
   * @param rack of location
   * @param level of location
   * @return Location
   * @throws NoSuchLocationException if these coordinates point to a nonexistent location
   */
  public Location getLocationAtCoordinates(String zoneName, int aisle, int rack, int level)
      throws NoSuchLocationException {
    // Find zone of location
    Zone zone = floor.get(zoneName);
    if (zone == null) {
      throw new NoSuchLocationException(&quot;This location doesn&#39;t exist in the inventory floor.&quot;);
    }

    // Make sure the location exists before returning
    Location location = zone.getLocationFromZone(aisle, rack, level);
    if (location == null) {
      throw new NoSuchLocationException(&quot;This location doesn&#39;t exist in the inventory floor.&quot;);
    } else {
      return location;
    }
  }

  /**
   * Get the location of a specific sku.
   * 
   * @param sku integer
   */
  public Location getLocation(String sku) {
    return locationMap.get(sku);
  }

  /**
   * Based on the Integer SKUs in List of &#39;skus&#39;, return a List of locations, where each location is
   * a String containing 5 pieces of information: the zone name, the aisle number in that zone, the
   * rack number in that aisle, the level on the rack, and the SKU number.
   * 
   * @param skus the list of SKUs to retrieve.
   * @return the List of locations.
   */
  public Location[] optimize(String[] skus) {
    Location[] optimizedLocations = new Location[skus.length];

    // Go through each SKU
    for (int i = 0; i &lt; skus.length; i++) {
      // Get locations from sku
      optimizedLocations[i] = getLocation(skus[i]);
    }

    return optimizedLocations;
  }


  /**
   * Remove an item from the associated location. After removal, check if replenishing is needed.
   * 
   * @param location The location of the item to remove
   * @throws NoSuchLocationException if this location does not exist
   */
  public void removeInventory(Location location) throws NoSuchLocationException {
    // Go to location and remove item
    try {
      location.setInventory(location.getInventory() - 1);
    } catch (StockExceedsCapacityException exp) {
      FileHelper.logError(exp, this);
    }
    // Check for replenishing
    checkAndReplenish(location);
  }

  /**
   * Check a given location to see if it needs replenishing. If it does, give the job to a
   * replenisher.
   * 
   * @param location The location to check
   */
  public void checkAndReplenish(Location location) {
    // Check if that level needs replenishing
    if (location.atMin()) {
      // Make sure this job isn&#39;t already queued
      boolean jobExists = false;
      for (Job j : getJobsToDo()) {
        if (j.getLocation().equals(location)) {
          jobExists = true;
          break;
        }
      }
      if (!jobExists) {
        // Create a replenishing job, assign to a worker, and add to queue
        Job job = new Job(location);
        this.queueJob(job);
        FileHelper.logEvent(
            &quot;Item &quot; + location.getSku() + &quot; at &quot; + job.getLocation() + &quot; needs replenishing&quot;, this);
      }
    }
  }

  /**
   * Replenish this location.
   * 
   * @param location The location to replenish.
   */
  public void replenish(Location location) {
    try {
      location.setInventory(location.getMax());
    } catch (StockExceedsCapacityException exp) {
      exp.printStackTrace();
    }
  }

  /**
   * Remove completed replenishing job from job queue.
   * 
   * @param worker The worker who has completed the job
   */
  public void jobComplete(Worker worker) {
    Job job = worker.getCurrentJob();
    try {
      super.jobComplete(worker);
      // Get location from location string of job
      Location location = job.getLocation();
      getSystem().replenishInventory(location);
    } catch (WorkerJobException exp) {
      getSystem().raiseWarning();
      FileHelper.logError(exp, this);
      // Send job to beginning of queue to try again
      job.resetJob();
      worker.setCurrentJob(null);
      FileHelper.logError(exp, this);
      getJobsInProgress().remove(job);
      getJobsToDo().add(job);
    }
  }

  /**
   * Create a new worker.
   * 
   * @param name of the worker
   * @return Worker
   */
  public Worker hireWorker(String name) {
    Worker replenisher = new Replenisher(name, this);
    addWorker(replenisher);
    return replenisher;
  }
}
</pre>
</body>
</html>
