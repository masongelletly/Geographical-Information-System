public class prQuadTree< T extends Compare2D<? super T> >
{
    /**
     * Inner class for nodes (public due to testing)
     *
     * Generic quadnode, see prQuadLeaf and prQuadInternal
     */
    public abstract class prQuadNode
    {
        //purely universal classification purposes, empty class
    }

    // -------------------------- LEAF NODE ------------------------------------------
    /**
     * Inner class for a leaf node (public due to testing)
     *
     */
    public class prQuadLeaf extends prQuadNode
    {
        // initialize arrayList with size pertaining to prQuadTree's given bucket size
        ArrayList<T> Elements = new ArrayList<T>(prQuadTree.this.bucket);
        int size = 0;

        /**
         * Constructor for prQuadLeaf object
         *
         * @param elem : the element in the leaf node
         */
        public prQuadLeaf(T elem)
        {
            // add the element of the leaf node to the list
            this.Elements.add(elem);

            // update size field
            size = Elements.size();
        }

        /**
         * default constructor empty leaf
         */
        public prQuadLeaf()
        {
            size = 0;
            Elements.clear();
        }
    }

    // -------------------------- INTERNAL NODE ------------------------------------------
    /**
     * prQuadTree internal nodes
     *
     * Contains 4 pointers to children that are either leaf nodes or more internal nodes
     *
     */
    public class prQuadInternal extends prQuadNode
    {
        // Use base-type pointers since children can be either leaf nodes
        // or internal nodes.
        prQuadNode NW, NE, SE, SW;

        // field dimensions of the internal node
        long xLo;
        long xHi;
        long yLo;
        long yHi;
    }

    // --------------------- QUADTREE CONSTRUCTORS ---------------------------------------
    // prQuadTree elements (public so test harness has access)
    public prQuadNode root;
    public long xMin, xMax, yMin, yMax;
    private int bucket;

    /**
     * prQuadTree constructor with added parameter to account for changed bucket size
     *
     * Initializes quadtree to empty state, representing specified region
     *
     * @param xMin   // point 1 x value
     * @param xMax   // point 2 x value
     * @param yMin   // point 1 y value
     * @param yMax   // point 2 y value
     * @param bucket // size of bucket for nodes
     */
    public prQuadTree(long xMin, long xMax, long yMin, long yMax, int bucket)
    {
        // initialize quadTree points to parametized values
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;

        // update bucket size of quadtree
        this.bucket = bucket;

        // no initial data, so root remains empty
        this.root = null;
    }

    /**
     * prQuadTree default constructor with default bucket size of 1
     *
     * Initializes quadtree to empty state, representing specified region
     *
     * @param xMin : point 1 x value
     * @param xMax : point 2 x value
     * @param yMin : point 1 y value
     * @param yMax : point 2 y value
     */
    public prQuadTree(long xMin, long xMax, long yMin, long yMax)
    {
        // initialize quadTree using default constructor
        this(xMin, xMax, yMin, yMax, 1);
    }

    // ---------------------------------- INSERT ----------------------------------------------
    /**
     * Method to handle the insertion of a generic element into the prQuadTree
     *
     * @pre elem != null
     * @post if elem lies within tree's region and elem is not present, elem has been inserted
     * @param elem : the element attempting to be added
     * @return true if elem is successfully inserted, false otherwise
     * @throws Exception
     */
    public boolean insert(T elem) throws Exception
    {
        
        // case 1: null
        // if null
        if (elem == null)
        {
            return false;
        }

        // duplicate handling case

        // update inWorld based on if elem has valid coordinates for insertion (world coords)
        boolean inWorld = true;
        if (elem.getX() > prQuadTree.this.xMax ||
                elem.getX() < prQuadTree.this.xMin ||
                elem.getY() > prQuadTree.this.yMax ||
                elem.getY() < prQuadTree.this.yMin)
        {
            inWorld = false;
        }

        // case 2 : elem not within world
        if (!inWorld)
        {
            return false;
        }

        // true insertion
        // utilizes helper method for recursion to delve into tree and properly insert
        this.root = this.insertHelper(elem, this.root, this.xMin, this.xMax, this.yMin,
                this.yMax);

        // since all false insertions would have been returned at this point, true insertion is
        // the only possibility remaining
        return true;

    }

    /**
     * helper function for recursive insert() implementation
     *
     * direction from. inbox
     *
     * @param sRoot : current root
     * @param elem : element to be added
     *
     * @param xLo : coordinates
     * @param xHi
     * @param yLo
     * @param yHi
     *
     * @return the next root
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private prQuadNode insertHelper(T elem, prQuadNode sRoot,  long xLo, long xHi, long yLo, long yHi) throws Exception
    {
        // if tree is presently empty. must create new leaf and begin tree
        if (sRoot == null)
        {
            // BEGIN THE TREE!!
            sRoot = new prQuadLeaf(elem);
        }

        // if point is within the boundaries of the world and tree is non-empty (VALID INSERTION)
        else
        {
            // ----------------- INTERNAL NODE ------------------------------------------
            // if sRoot is an internal node, switch logic to accomodate
            if (sRoot.getClass().equals(prQuadInternal.class))
            {
                // insert node into the internal node using helper method's recursion
                sRoot = this.internalInsert((prQuadInternal) sRoot, elem,
                        xLo, xHi, yLo, yHi);
            }

            // ----------------- LEAF NODE ----------------------------------------------
            // if the root is a leaf node (not an internal node)
            if (sRoot.getClass().equals(prQuadLeaf.class))
            {
                // sentinel boolean to check for insertion
                boolean prevInserted = false;

                // create new leaf using sRoot as a basis
                prQuadLeaf leafNode = ((prQuadLeaf) sRoot);

                // ------------------------ BASE CASE -----------------------------------
                // iterate through bucket to attempt to find elem's node match
                for (int idx = 0; idx < leafNode.size; idx++)
                {
                    // cast nodes to gisEntry
                    gisEntry currEntry = (gisEntry)leafNode.Elements.get(idx);
                    gisEntry elemEntry = (gisEntry)elem;                                    
                    
                    // if (leafNode.Elements.get(idx).equals(elem)) 
                    // above is true for normal quadtree implementation   
                    // below is used due to gisEntry equals() use
                    if (currEntry.equals(elemEntry))
                    {
                        // update sentinel due to finding correct node. BASE CASE
                        elemEntry.addOffset(currEntry.getFirstOffset());
                        prevInserted = true;
                    }
                }

                // if proper insertion already found
                if (prevInserted == true)
                {
                    sRoot = leafNode;
                }

                // --------------------- BUCKET CALCULATIONS -------------------------
                // normal insertion process
                // first begins check to see if bucket is full
                else
                {
                    // FULL BUCKET
                    // must split and then fill the new internal node
                    if (leafNode.size == this.bucket)
                    {
                        // recursive call to helper functions split() to split
                        // and internalInsert() to fill internal node
                        sRoot = this.split(sRoot, xLo, xHi, yLo, yHi);
                        sRoot = this.internalInsert((prQuadInternal) sRoot, elem,
                                xLo, xHi, yLo, yHi);
                    }
                    // NOT FULL BUCKET
                    // simply add the node to the bucket!
                    else if (leafNode.size < this.bucket)
                    {
                        // add to bucket
                        leafNode.Elements.add(elem);

                        // update size and sRoot
                        leafNode.size++;
                        sRoot = leafNode;
                    }
                }
            }
        }

        // finale return!
        return sRoot;
    }


    /**
     * helper method for insert that will split the leaf node. thus, creating
     * an internal node
     *
     * @param node : the leaf node
     *
     * @param xLo : coordinates
     * @param xHi
     * @param yLo
     * @param yHi
     *
     * @return babyInternal : newly created internal node
     * @throws Exception
     */
    private prQuadInternal split(prQuadNode sRoot, long xLo, long xHi, long yLo, long yHi) throws Exception
    {
        // create the internal node that is the result of the split
        // (also the return value)
        prQuadInternal babyInternal = new prQuadInternal();

        // cast sRoot to leaf node so that we may access leafNode properties. this is okay unchecked since
        // only a leafNode should be being split()
        prQuadLeaf leafNode = (prQuadLeaf) sRoot;

        // iterate through leafNode bucket and perform insertion into the newly created internal node
        for (int idx = 0; idx < leafNode.size; idx++)
        {
            // call to recursive private function to handle insertion into babyInternal
            babyInternal = this.internalInsert(babyInternal, leafNode.Elements.get(idx), xLo, xHi,
                    yLo, yHi);
        }

        // return the newly created internal node
        return babyInternal;
    }

    /**
     * helper method to insert into an internal node
     *
     * @param internalNode : node being split
     * @param elem : element being inserted
     *
     * @param xLo : coordinates
     * @param xHi
     * @param yLo
     * @param yHi
     *
     * @return created node
     * @throws Exception
     */
    private prQuadInternal internalInsert(prQuadInternal internalNode, T elem, long xLo, long xHi, long yLo, long yHi) throws Exception
    {
        // ------------------------ MID CALCULATIONS -----------------------------
        // update mid variables
        long middleX = ((xHi + xLo) / 2);
        long middleY = ((yHi + yLo) / 2);

        // ----------------------- QUADRANT INSERTIONS ---------------------------
        // determine in which quadrant element falls
        Direction currQuad = elem.directionFrom(middleX, middleY);

        // perform the insertion on that particular leafNode within the internalNode

        // North East
        if (currQuad == Direction.NE)
        {
            internalNode.NE = this.insertHelper(elem, internalNode.NE, middleX, xHi, middleY, yHi);
        }
        // South East
        if (currQuad == Direction.SE)
        {
            internalNode.SE = this.insertHelper(elem, internalNode.SE, middleX, xHi, yLo, middleY);
        }
        // South West
        if (currQuad == Direction.SW)
        {
            internalNode.SW = this.insertHelper(elem, internalNode.SW, xLo, middleX, yLo, middleY);
        }
        // North West
        if (currQuad == Direction.NW)
        {
            internalNode.NW = this.insertHelper(elem, internalNode.NW, xLo, middleX, middleY, yHi);
        }

        // finale return!
        return internalNode;
    }

    // ---------------------------------- FIND ------------------------------------------------
    /**
     * Method to be able to seek out, find, and return a specific reference based on its data (elem)
     *
     * @pre elem != null
     * @param elem : the element attempting to be found
     * @return reference to element x within tree such that elem.equals(x) is true.
     *       returns null otherwise
     */
    public T find(T elem)
    {
        // simply call the more robust find
        return this.find(elem, root,  this.xMin, this.xMax, this.yMin, this.yMax);
    }

    /**
     * Method to find and return a set of references to all elements in the region described
     *
     * @pre xLo < xHi && yLo < yHi
     * @param xLo : point 1 x value
     * @param xHi : point 2 x value
     * @param yLo : point 1 y value
     * @param yHi : point 2 y value
     * @return a collection of references to all elements x such that x is in the tree
     *       and x lies at coordinates within the parameterized region
     */
    public ArrayList<T> find(long xLo, long xHi, long yLo, long yHi)
    {
        // simply call the MOST robust find
        return this.findNoElem(this.root, xLo, xHi, yLo, yHi);
    }

    /**
     * A helper method to find() that will recursively travel and return a specific node based
     * upon its element
     *
     * @param elem : the data point of the node to be returned
     * @param sRoot : the root at the given recursive iteration
     * @param xLo : coordinates
     * @param xHi
     * @param yLo
     * @param yHi
     *
     * @return the node that corresponds to the element
     */
    private T find(T elem, prQuadNode sRoot, long xLo, long xHi, long yLo, long yHi)
    {
        // call other recursive helper method
        ArrayList<T> elementsList = this.findNoElem(sRoot, xLo, xHi, yLo, yHi);

        // initialize the T value to be updated and returned
        T returnElem;

        System.out.println(elementsList.toString());
        // update returnElem so long as it is valid
        if (elementsList.indexOf(elem) >= 0)
        {
            returnElem = elementsList.get(elementsList.indexOf(elem));
        }
        else
        {
            returnElem = null;
        }

        // finale return!
        return returnElem;
    }

    /**
     * MOST ROBUST FIND METHOD
     *
     * used by all find methods, returns an ArrayList of nodes so long as they are within
     * the specified boundaries. Utilizes recursion to perform its behavior
     *
     * @param sRoot : root that operations are based around
     *
     * @param xLo : coordinates
     * @param xHi
     * @param yLo
     * @param yHi
     *
     * @return returnList : a list of nodes within the specified boundaries
     */
    private ArrayList<T> findNoElem(prQuadNode sRoot, long xLo, long xHi, long yLo, long yHi)
    {
        // ---------------------- SETUP -------------------------------------------------------
        // create the list to be returned
        ArrayList<T> returnList = new ArrayList<T>();

        // if its null, simply return empty list
        if (sRoot == null)
        {
            return returnList;
        }

        // ----------------- FINDING BEGINS --------------------------------------------------
        // if the point is in the world (VALID POINT)
        if (!((xLo > this.xMax) || (yLo > this.yMax) || (xHi < this.xMin) || (yHi < this.yMin)))
        {

            // ------------- INTERNAL NODE ----------------------------------------------------
            if (sRoot.getClass().equals(prQuadInternal.class))
            {
                // perform the cast so that we may access internal sRoot properties
                // unchecked is fine due to condition above...
                prQuadInternal babyInternal = (prQuadInternal) sRoot;

                // midpoint calculations for region cases
                long middleX = ((xHi + xLo) / 2);
                long middleY = ((yHi + yLo) / 2);

                // --------- QUADRANT ARRAYLIST BUILDING ------------------------------------
                // the following will recursively delve into a quadrant (found by conditional calculations
                // with middleX, middleY) and add the nodes found to an ArrayList.

                // North East
                if ((!((middleX > this.xMax) || (middleY > this.yMax) || (xHi < this.xMin) || (yHi < this.yMin))))
                {
                    returnList.addAll(this.findNoElem(babyInternal.NE, xLo, xHi, yLo, yHi));
                }

                // South East
                if ((!((middleX > this.xMax) || (yLo > this.yMax) || (xHi < this.xMin) || (middleY < this.yMin))))
                {
                    returnList.addAll(this.findNoElem(babyInternal.SE, xLo, xHi, yLo, yHi));
                }

                // South West
                if ((!((xLo > this.xMax) || (yLo > this.yMax) || (middleX < this.xMin) || (middleY < this.yMin))))
                {
                    returnList.addAll(this.findNoElem(babyInternal.SW, xLo, xHi, yLo, yHi));
                }

                // North West
                if ((!((xLo > this.xMax) || (middleY > this.yMax) || (middleX < this.xMin) || (yHi < this.yMin))))
                {
                    returnList.addAll(this.findNoElem(babyInternal.NW, xLo, xHi, yLo, yHi));
                }
            }

            // --------------- LEAF NODE ------------------------------------------------------
            if (sRoot.getClass().equals(prQuadLeaf.class))
            {
                // perform the cast so that we may access leaf sRoot properties
                prQuadLeaf leafNode = (prQuadLeaf) sRoot;

                // iterate through elements and check if they are in the specified region
                for (int idx = 0; idx < leafNode.size; idx++)
                {
                    // add to the list if it is found to be within region (inBox)
                    if (leafNode.Elements.get(idx).inBox(xLo, xHi, yLo, yHi))
                    {
                        returnList.add(leafNode.Elements.get(idx));
                    }
                }
            }
        }

        // finale return!
        return returnList;
    }
    
    /**
     * call on to string that will enact the recursive toString() solution
     */
    @Override
    public String toString()
    {
        return toStringHelper(root, "");
    }
    
    /**
     * to string recursive solution. heavily taken from slide 15 of class lecture (also .lewis)
     * 
     * @param sRoot
     * @param Padding
     * @return
     */
    private String toStringHelper(prQuadNode sRoot, String Padding)
    {
        // initialize return string
        String finalReturn = "";
        
        // empty leaf
        if ( sRoot == null ) 
        {
            finalReturn = "\n" + Padding  + "*\n";
            return finalReturn;
        }
        // Check for and process SW and SE subtrees
        if ( sRoot.getClass().equals(new prQuadInternal().getClass()) ) 
        {
            prQuadInternal p = (prQuadInternal) sRoot; 
            finalReturn = finalReturn + toStringHelper(p.SW, Padding + " "); 
            finalReturn = finalReturn + toStringHelper(p.SE, Padding + " ");
        }
        
        // if leaf
        if ( sRoot.getClass().equals(new prQuadLeaf().getClass()) ) 
        {
            prQuadLeaf p = (prQuadLeaf) sRoot;
            finalReturn = finalReturn + "\n" +  Padding + p.Elements ; 
        }
        else
            finalReturn = finalReturn + "\n" + Padding + "@\n" ;
        
        // if internal
        if ( sRoot.getClass().equals(new prQuadInternal().getClass()) ) 
        {
            prQuadInternal p = (prQuadInternal) sRoot; 
            finalReturn += toStringHelper(p.NE, Padding + " "); 
            finalReturn += toStringHelper(p.NW, Padding + " ");
        }
        
        // finale return!
        return finalReturn;
    }
}
