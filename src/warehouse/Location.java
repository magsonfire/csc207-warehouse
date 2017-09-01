<!DOCTYPE html>
<html>
<body>
<pre>package warehouse;

public class Location {
  // Unique identifier for this location object based on its attributes
  private String id;

  // Attributes indicating this location&#39;s location in physical warehouse
  private String zone;
  private int aisle;
  private int rack;
  private int level;
  // The SKU of items stored here
  private String sku;
  // The inventory level (number of items) here
  private int inventory = 0;
  // The maximum capacity here
  private int max = 0;
  // The minimum inventory, at which a replenish request is made
  private int min = 0;

  /**
   * Construct a location.
   * 
   * @param zone The zone of this location.
   * @param aisle The aisle of this location.
   * @param rack The rack of this location.
   * @param level The level of this location.
   * @param sku The SKU of items at this location.
   */
  public Location(String zone, int aisle, int rack, int level, String sku) {
    this.zone = zone;
    this.aisle = aisle;
    this.rack = rack;
    this.level = level;
    this.sku = sku;
    setId();
  }

  /**
   * Construct a location without sku, but with its maximum and minimum capacities.
   * 
   * @param zone The zone of this location.
   * @param aisle The aisle of this location.
   * @param rack The rack of this location.
   * @param level The level of this location.
   * @param max The capacity of this location
   * @param min The minimum number of items before this location needs replenishing.
   */
  public Location(String zone, int aisle, int rack, int level, int max, int min) {
    this.zone = zone;
    this.aisle = aisle;
    this.rack = rack;
    this.level = level;
    // Capacities
    this.max = max;
    this.min = min;
    setId();
  }

  /**
   * Construct a location, including its maximum and minimum capacities.
   * 
   * @param zone The zone of this location.
   * @param aisle The aisle of this location.
   * @param rack The rack of this location.
   * @param level The level of this location.
   * @param sku The SKU of items at this location.
   * @param max The capacity of this location
   * @param min The minimum number of items before this location needs replenishing.
   */
  public Location(String zone, int aisle, int rack, int level, String sku, int max, int min) {
    this.zone = zone;
    this.aisle = aisle;
    this.rack = rack;
    this.level = level;
    this.sku = sku;
    // Capacities
    this.max = max;
    this.min = min;
    setId();
  }

  /**
   * Construct a location from a csv string, with 30 as default max and 5 as default min capacities.
   * 
   * @param csv The comma-separated value to generate this location&#39;s attributes
   */
  public Location(String csv) {
    this(csv, 30, 5);
  }

  /**
   * Construct a location from a csv string with max and min values.
   * 
   * @param csv The comma-separated value to generate this location&#39;s attributes
   * @param max The capacity of this location
   * @param min The minimum number of items before this location needs replenishing.
   */
  public Location(String csv, int max, int min) {
    String[] split = csv.split(&quot;,&quot;);
    this.zone = split[0];
    this.aisle = Integer.valueOf(split[1]);
    this.rack = Integer.valueOf(split[2]);
    this.level = Integer.valueOf(split[3]);
    this.sku = split[4];
    this.max = max;
    this.min = min;
    setId();
  }

  /**
   * Set id of this location based on its attributes.
   */
  private void setId() {
    this.id = zone + &quot; &quot; + aisle + &quot; &quot; + rack + &quot; &quot; + level;
  }

  /**
   * Get id of this location.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Get the zone of this location.
   * 
   * @return Zone of this location
   */
  public String getZone() {
    return this.zone;
  }

  /**
   * Get the aisle of this location.
   * 
   * @return aisle of this location
   */
  public int getAisle() {
    return this.aisle;
  }

  /**
   * Get the rack of this location.
   * 
   * @return rack of this location
   */
  public int getRack() {
    return this.rack;
  }

  /**
   * Get the level of this location.
   * 
   * @return Level of this location
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * Get the SKU at this location.
   * 
   * @return sku at this location
   */
  public String getSku() {
    return this.sku;
  }

  /**
   * Set the SKU at this location.
   * 
   * @param sku The SKU of items here
   */
  public void setSku(String sku) {
    this.sku = sku;
  }

  /**
   * Get the inventory level at this location.
   * 
   * @return inventory level at this location
   */
  public int getInventory() {
    return this.inventory;

  }

  /**
   * Set the inventory level at this location.
   * 
   * @param amount inventory level at this location
   * @throws StockExceedsCapacityException if amount to stock location exceeds its capacity
   */
  public void setInventory(int amount) throws StockExceedsCapacityException {
    if (amount &gt; this.max || amount &lt; 0) {
      throw new StockExceedsCapacityException(
          &quot;Attempted stock &quot; + amount + &quot; exceeds maximum capacity of &quot; + this.max);
    }
    this.inventory = amount;
  }

  /**
   * Get the maximum capacity at this location.
   * 
   * @return maximum capacity at this location
   */
  public int getMax() {
    return this.max;
  }

  /**
   * Set the maximum capacity at this location.
   * 
   * @param max maximum capacity at this location
   */
  public void setMax(int max) {
    this.max = max;
  }

  /**
   * Get the minimum inventory at this location.
   * 
   * @return minimum inventory at this location
   */
  public int getMin() {
    return this.min;

  }

  /**
   * Set the minimum inventory at this location.
   * 
   * @param min minimum inventory at this location
   */
  public void setMin(int min) {
    this.min = min;
  }

  /**
   * Return a string representation of Location (id).
   * 
   * @return String The ID of this location.
   */
  public String toString() {
    return this.id;
  }


  /**
   * Checks if the inventory of this location has reached its minimum level.
   * 
   * @return boolean If location has reached min inventory
   */
  public boolean atMin() {
    return inventory &lt;= min;
  }

  /*
   * Checks if this location is the same as another&#39;s, based on their id, which consists of the
   * location&#39;s coordinates (zone, aisle, rack, level, sku).
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    // Check if same obj
    if (this == obj) {
      return true;
    }
    // Check if other obj exists
    if (obj == null) {
      return false;
    }
    // Check same class
    if (getClass() != obj.getClass()) {
      return false;
    }
    // Sameness based on id
    Location other = (Location) obj;
    if (!getId().equals(other.getId())) {
      return false;
    }

    if (!getSku().equals(other.getSku())) {
      return false;
    }
    return true;
  }
}
</pre>
</body>
</html>
