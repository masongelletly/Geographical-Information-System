import java.util.ArrayList;

/**
 * Data type to be used for insertion into prQuadTree. Behavior should allow for the holding of 
 * primary latitude, longitude information, as well as file offset
 * 
 * @author mason
 * @version 4.21.22
 */
public class gisEntry implements Compare2D
{
    // coordinate fields
    public long xcoord;
    public long ycoord;
    
    // field for offset arraylist
    private ArrayList<Long> offsets;
    
    /**
     * Constructor for the object. Takes in all needed information via
     * parameterization
     * 
     * @param paramLat
     * @param paramLong
     * @param paramOffset
     * 
     */
    public gisEntry(long paramLat, long paramLong, long paramOffset)
    {
        // initialize array list
        offsets = new ArrayList<Long>();
        
        // update offset lists     
        offsets.add(paramOffset);
        
        // update coords
        xcoord = paramLong;
        ycoord = paramLat;
    }
    
    /**
     * simple getter method for offset value
     * 
     * @return offset field
     */
    public ArrayList<Long> getOffsets()
    {
        return offsets;
    }
    
    /**
     * simple getter method for beginning offsets value
     * 
     * @return beginning offset (indice 1)
     */
    public Long getFirstOffset()
    {
        return offsets.get(0);
    }
    
    /**
     * method to add offsets
     * 
     * @param newOffset : new offset to be added
     */
    public void addOffset(Long newOffset)
    {
        offsets.add(newOffset);
    }
    
    
    /**
     * a way to display the gisEntry's data
     * 
     * formatting:
     *      (latitude, longitude) offset
     */
    public String toString()
    {
        return ("(" + Long.toString(xcoord) + ", " + Long.toString(ycoord) + ") " + offsets.toString());
    }

    /**
     * returns the longitude (x value) of the entry
     */
    public long getX() 
    {
        return xcoord;
    }

    /**
     * returns the latitude (y value) of the entry
     */
    public long getY() 
    {
        return ycoord;
    }

    @Override
    public Direction directionFrom(long X, long Y) 
    {
        // simplify caller's coordinates
        long otherX = this.xcoord;
        long otherY = this.ycoord;
        
        // coordinate calculations
        long xDifference = otherX - X;
        long yDifference = otherY - Y;
        
        // center point
        if (X == 0 && Y == 0)
        {
            return Direction.NE;
        }
        // ISSUE WITH BOUNDS
        // positive x axis (same y different x)
        if (yDifference == 0 && xDifference > 0)
        {
            return Direction.NE;
        }
        // negative x axis (same y different x)
        else if (yDifference == 0 && xDifference < 0)
        {
            return Direction.SW;
        }
        // positive y axis (same x different y)
        else if (yDifference > 0 && xDifference == 0)
        {
            return Direction.NW;
        }
        // negative y axis (same x different y)
        else if (yDifference < 0 && xDifference == 0)
        {
            return Direction.SE;
        }
        
        // ----------------------------------------------------
        
        // return direction based on difference coordinates
        // NE CASE : Also handles center point 
        if (xDifference >= 0 && yDifference >= 0)
        {
            return Direction.NE;
        }
        // NW CASE
        else if (xDifference <= 0 && yDifference >= 0)
        {
            return Direction.NW;
        }
        //SW CASE
        else if (xDifference <= 0 && yDifference <= 0)
        {
            return Direction.SW;
        }
        //SE CASE
        else if (xDifference >= 0 && yDifference <= 0)
        {
            return Direction.SE;
        }
               
        // if somehow none are hit
        return Direction.NOQUADRANT;
    }

    /**
     * method that returns the quadrant in which the gisEntry lies in of the specified
     * boundary rectangle
     */
    public Direction inQuadrant(double xLo, double xHi, double yLo, double yHi) 
    {
        // case not in rectangle
        if (!inBox(xLo, xHi, yLo, yHi))
        {
            return Direction.NOQUADRANT;
        }
        
        // find center of space
        long xCenter = (long)((xLo + xHi) / 2);
        long yCenter = (long)((yLo + yHi) / 2);
        
        // use directionFrom on center to return appropriate direction
        return this.directionFrom(xCenter, yCenter);
    }

    /**
     * method that returns if the specified point lies within a given boundary rectangle
     */
    public boolean inBox(double xLo, double xHi, double yLo, double yHi) 
    {
        //              (xHi, yHi)
        //   -----------*
        //  |           |
        //  |           |   
        //  |           |
        //  *-----------
        //  (xLo, yLo)      
        
        // simplify caller's coordinates
        long X = this.xcoord;
        long Y = this.ycoord;
        
        // if valid coordinates
        if (X >= xLo && X <= xHi && Y >= yLo && Y <= yHi)
        {
            return true;
        }
        // invalid coordinates
        else
        {
            return false;
        }
    }
    
    /**
     * equals method used to check for duplicate elements 
     * 
     * @param otherEntry
     * @return true if equal, false if nonequal
     */
    public boolean equals(gisEntry otherEntry)
    {
        return (this.xcoord == otherEntry.xcoord && 
            this.ycoord == otherEntry.ycoord);
    }

}
