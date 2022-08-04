/**
 * hash table intended to help with the design implementation of a GIS information manipulation
 * project
 * 
 * resolves collisions by chaining
 * 
 * must store entries that hold a String (GIS Feature Name) and a collection of Long values
 * for file offsets of GIS records that MATCH that name
 * 
 * so,
 * type STRING : type LONG ARRAY
 * featureName : offsets
 * 
 * @author mason
 * @version 4.1.22
 * 
 * Implements a generic chained hash table, using an ArrayList of LinkedLists
* for the physical table.
*
* The ArrayList has a default size of 256 slots, configurable via the class
* constructor.
*
* The size of the ArrayList is doubled when the load factor exceeds the
* load limit (defaulting to 0.7, but configurable via the class constructor).
*
* Elements inserted to the table must implement the Hashable interface:
* public int Hash();
*
* This allows the user to choose an appropriate hash function, rather than
* being tied to a fixed hash function selected by the table designer.
*/
public class hashtable<T extends Hashable<T> > 
{
    private ArrayList< LinkedList<T> > table; // physical basis for the hash table
    private Integer numElements = 0; // number of elements in all the chains
    private Integer maxElements = 0; // max elements in one slot in table
    private Double loadLimit = 0.7; // table resize trigger
    private final Integer defaultTableSize = 256; // default number of table slots
    
    // an array list, of linked lists... alrighty
    
    /** 
     * Constructs an empty hash table with the following properties:
     * Pre:
     * - size is the user's desired number of lots; null for default
     * - ldLimit is user's desired load factor limit for resizing the table;
     * null for the default
     * Post:
     * - table is an ArrayList of size LinkedList objects, 256 slots if
     * size == null
     * - loadLimit is set to default (0.7) if ldLimit == null
     */
    public hashtable(Integer size, Double ldLimit) 
    {
        // size handling
        if (size == null)
        {
            // create with default size
            size = defaultTableSize;
            table = new ArrayList< LinkedList<T> >(defaultTableSize);
        }
        else
        {
            // create with custom size
            table = new ArrayList< LinkedList<T> >(size);
        }
        
        // ldLimit handling
        if (ldLimit != null)
        {
            loadLimit = ldLimit;
        }       
        
        // fill table with linked lists (empty)
        if (table.isEmpty())
        {
            for (int i = 0; i < size; i++)
            {
                LinkedList<T> beginning = new LinkedList<T>();
                table.add(i, beginning);
            }
        }
        
    }
    
    /** 
     * Inserts elem at the front of the elem's home slot, unless that
     * slot already contains a matching element (according to the equals()
     * method for the user's data type.
     * 
     * Pre:
     * - elem is a valid user data object
     * 
     * Post:
     * - elem is inserted unless it is a duplicate
     * - if the resulting load factor exceeds the load limit, the
     * table is rehashed with the size doubled
     * 
     * Returns:
     * true if elem has been inserted
     * 
     */
    @SuppressWarnings("unchecked")
    public boolean insert(T elem)
    {
        // CASTING AND FURTHER LOGIC
        //
        // hashing very big
        dataEntry cast = (dataEntry)elem;
        
        // home slot
        int home = cast.Hash(); // hash value != home slot      
        home = home % table.size();
 
        // insertion for nonempty list
        if (!table.get(home).isEmpty())
        {
            // find could potentially work            
            // DUPLICATE CHECK
            for (Integer idx = 0; idx < table.get(home).size(); idx++)
            {
                // update current element of list being looked for
                // we are checking for equality using equals()
                T currElem = table.get(home).get(idx);
                
                // equals assertment
                if (currElem.equals(elem))
                {
                    // following code TEST
                    dataEntry xEntry = (dataEntry)elem;

                    T addThisElem = table.get(home).get(idx);
                    dataEntry addThisEntry = (dataEntry)addThisElem;
                    
                    addThisEntry.addLocation(xEntry.locations.get(0));

                    // false return due to duplicate find DOESNT REALLY MATTER
                    return false;
                }
                else
                {
                    // updating locations
                    // firstly, cast the T elem 
                    dataEntry xEntry = (dataEntry)elem;
                    xEntry.addLocation(xEntry.locations.get(0));
                }
                
            }
        }
        
        // actual insertion (both cases)
        table.get(home).add(elem);  
        numElements++;
        
        // max element calculation
        if (table.get(home).size() - 1 > maxElements)
        {
            maxElements = table.get(home).size() - 1;
        }
        
        // ---------------------------------------------
        //LOAD FACTOR & REHASHING
        //
        // calculate load factor
        float loadFactor = (float)numElements / (float)table.size();

        //REHASHING ----------------------------------
        //
        if (loadFactor > loadLimit)
        {
            // rehashed with size doubled
            ArrayList< LinkedList<T> > rehash = new ArrayList< LinkedList<T> >(table.size() * 2);
            
            // fill table with linked lists (empty)
            if (rehash.isEmpty())
            {
                for (int i = 0; i < table.size() * 2; i++)
                {
                    LinkedList<T> beginning = new LinkedList<T>();
                    rehash.add(i, beginning);
                }
            }
            
            // iterate through old hashmap and insert
            for (int idx = 0; idx < table.size(); idx++)
            {
                // if linked list is NOT empty
                if (!table.get(idx).isEmpty())
                {           
                    // current list being copied
                    LinkedList<T> currList = table.get(idx);
                    
                    // iterate through linked list and add to rehash
                    for (int jdx = 0; jdx < currList.size(); jdx++)
                    {
                        // current T element being considered
                        dataEntry currEntry = (dataEntry)currList.get(jdx);
                        
                        // add old stuff to new list
                        LinkedList<T> fresh = rehash.get(currEntry.Hash() % (table.size() * 2));
                        fresh.add((T)currEntry);
                    }
                }
            }
            
            // update table 
            table = rehash;
        }
        
        // will always be true at this point
        return true;
    }
    
    /** Searches the table for an element that matches elem (according to
     * the equals() method for the user's data type).
     * 
     * Pre:
     * - elem is a valid user data object
     * Returns:
     * reference to the matching element; null if no match is found
     */
    public T find(T elem) 
    {
        
        // iterate through table 
        for (int idx = 0; idx < table.size(); idx++)
        {
            // iterate through linked list
            for (int jdx = 0; jdx < table.get(idx).size(); jdx++)
            {
                // check for match //MAYBE ITERATE THROUGH CHAIN
                // null check
                if (table.get(idx).isEmpty())
                {
                    continue;
                }
                else if (table.get(idx).get(jdx).equals(elem))
                {
                    return table.get(idx).get(jdx);
                } 
            }
        }
        
        // no match was found
        return null;
    }
    
    /**
     * simple getter for size of table
     * 
     * @return the table size
     */
    public int getSize()
    {
        return table.size();
    }
    
    /** Writes a formatted display of the hash table contents.
     * Pre:
     * - fw is open on an output file
     */
    public String display() throws IOException 
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Number of elements: " + numElements + "\n");
        sb.append("Number of slots: " + table.size() + "\n");
        sb.append("Maximum elements in a slot: " + maxElements + "\n");
        sb.append("Load limit: " + loadLimit + "\n");
        sb.append("\n");

        sb.append("Slot Contents\n");

        for (int idx = 0; idx < table.size(); idx++) 
        {
            LinkedList<T> curr = table.get(idx);

            if ( curr != null && !curr.isEmpty() ) 
            {

                sb.append(String.format("%5d: %s\n", idx, curr.toString()));
            }
        }
        
        return sb.toString();
    }
}

