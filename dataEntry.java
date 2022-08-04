import java.util.ArrayList;

public class dataEntry implements Hashable<dataEntry> 
{
    String key; // GIS feature name
    ArrayList<Long> locations; // file offsets of matching records
    
    /** 
     * Initialize a new nameEntry object with the given feature name
     * and a single file offset.
     */
    public dataEntry(String name, Long offset)
    {
        // update name 
        key = name;
        
        // append offset arraylist
        locations = new ArrayList<Long>();
        locations.add(offset);
    }
    
    /** 
     * Return feature name.
     */
    public String key() 
    {
        return this.key;
    }
    
    /** 
     * Return list of file offsets.
     */
    public ArrayList<Long> locations() 
    {
        return this.locations;
    }
    
    /** 
     * Append a file offset to the existing list.
     */
    public boolean addLocation(Long offset) 
    {
        // no duplicates (secondary check basically)
        if (locations.contains(offset))
        {
            return false;
        }
        
        // attempts to add offset to locations
        try
        {
            locations.add(offset);
        }
        // in event of failure, simply return false
        catch(Exception e)
        {
            return false;
        }
        
        // return true (procs if no exception was caught)
        return true;
    }
    
    /** Fowler/Noll/Vo hash function is mandatory for this assignment. 
     * 
     */
    public int Hash() 
    {
        final int fnvPrime = 0x01000193; // Constant values for FNV
        final int fnvBasis = 0x811c9dc5; // hash algorithm
        int hashValue = fnvBasis;
        
        for (int i = 0; i < key.length(); i++) 
        {
            hashValue ^= key.charAt(i);
            hashValue *= fnvPrime;
        }
        
        return Math.abs(hashValue);
    }
    
    /** Two nameEntry objects are considered equal iff they
     * hold the same feature name.
     */
    public boolean equals(Object other) 
    {
        // class check
        if (other.getClass().equals(this.getClass()))
        {
            // cast for traits
            dataEntry otherEntry = (dataEntry)other;
            
            // compare names
            return (otherEntry.key.equals(this.key));
        }
        else
        // failed class check
        {
            return false;
        }

    }
    
    /** Return a String representation of the nameEntry object in the
     * format needed for this assignment.
     */
    public String toString() 
    {
     return ( "[" + this.key + ", " + this.locations.toString() + "]" );
    }
 }

