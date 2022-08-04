**
 * bufferpool data structure in order to increase efficiency of J5 searching
 * 
 * holds up to 15 records (raw strings) & utilizes LRU (least recently used) replacement
 * 
 * the pool will automatically update positions based upon recency of use. repositioning
 * will be handled by the insertion method
 * 
 * indice [0] will be the most recently used
 * indice [14] will be the least recently used
 * 
 * underlying stack data structure is utilized to represent the pool
 * 
 * @author mason gelletly
 * @version 4.22.22
 */
public class bufferPool 
{        
    // initialize underlying stack 
    private Stack<String> pool = new Stack<String>(); 
    
    /**
     * method to insert given entry and reposition other elements. additionally acts as an update() 
     * of sorts if given a string that is already present in the pool
     * 
     * @param newRecord : new record to be inserted
     */
    public void insert(String newRecord)
    {        
        // check if already in pool (only first 15 indices)
        for (int idx = 0; idx < pool.size(); idx++)
        {
            // if match, remove element from its current indice
            if (pool.get(idx).equals(newRecord))
            {
                pool.remove(idx);
            }
        }
        
        // move to front due to LRU
        pool.add(0, newRecord);
        
        // LIMITING THE SIZE OF THE STACK TO 15
        // 
        // remove the 16th record if stack exceeds size 15
        if (pool.size() > 15)
        {
            // remove the 16th record 
            pool.remove(15);
        }
    }
    
    /**
     * getter method for the pool
     * 
     * @param index : the index of the string to be found
     * @return : the string at the parameterized indice
     */
    public String get(int index)
    {
        // simply use stack library's get()
        return pool.get(index);
    }
    
    /**
     * contains method for the pool
     * 
     * returns true or false depending on if the pool contains the given string
     */
    public boolean contains(String compareString)
    {
        return pool.contains(compareString);
    }
    
    
    /**
     * getter method for the capacity of the stack (size)
     * 
     * @return : the number of elements in the pool
     */
    public int size()
    {
        // return pool's size(should be 0-15)
        return pool.size();
    }
    /**
     * display function meant to return a string that is capable of 
     * accurately reprenting the records within it
     * 
     * @return : the contents of the buffer pool
     */
    public String display()
    {
        // string builder object initialization for ease of concatenation
        StringBuilder sb = new StringBuilder();
        
        // iterates through pool and add contents to sb 
        for (String currString : pool)
        {
            sb.append(currString + "\n");           
        }
        
        // return string
        return sb.toString();
    }
    
}
