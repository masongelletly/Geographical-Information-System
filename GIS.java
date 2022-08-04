import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * the main class that will utilize prQuadTree and HashTable implementations to create a GIS database that can perform
 * specific behavior such as displaying data entries and searching for specific locations as well as all locations
 * within a particular area. 
 * 
 * @author mason gelletly (masongelletly@vt.edu)
 * @version 4.20.22
 */
public class GIS
{
    // RandomAccessFile fields
    private static RandomAccessFile database;
    private static RandomAccessFile commandScript;
    private static RandomAccessFile log; 
    
    // file field 
    private static File databaseFile;
    
    // prQuadTree field 
    private static prQuadTree quadtree;
    
    // hashtable field
    private static hashtable<dataEntry> table;
    
    // bufferPool field
    private static bufferPool pool;
    
    // command line number
    private static int commandNumber = -1;
    
    /**
     * main method. executes the behavior of the entire project.
     * 
     * designed for specific command line parameters:
     * main (<database file name> ,<command script file name>, <log file name>)
     * 
     * @param args : command line arguments
     * args[0] = database file name 
     * args[1] = command script file name
     * args[2] = log file name
     * 
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception
    {
        // VALIDATE COMMAND LINE PARAMETERS 
        //
        // check proper number of arguments
        if (args.length != 3)
        {
            // display error message and exit runtime
            System.out.println("Invalid parameters \nMake certain you are following the formatting:"
                + " GIS <database file name> <command script file name> <log file name>");
            System.exit(0);
        }        
        // attempt to validate command script file name
        try 
        {
            commandScript = new RandomAccessFile(args[1], "rw");
        }
        catch (FileNotFoundException e) 
        {
            // display FileNotFound error message and exit runtime
            System.out.println("Invalid command script file name. FileNotFound error");
            System.exit(0);
        }
        
        // CREATE HASHTABLE
        //
        table = new hashtable<dataEntry>(256, 0.7);
        
        // CREATE A NEW GIS DATABASE FILE
        // 
        database = new RandomAccessFile(args[0], "rw");
        database.setLength(0);
        
        // CREATE LOG FILE
        //
        log = new RandomAccessFile(args[2], "rw");
        log.setLength(0);
        
        // DATABASE PARSING
        //
        // creates scanner for database parsing
        databaseFile = new File(args[0]);
        Scanner databaseScanner = new Scanner(databaseFile);

        
        // COMMANDSCRIPT PARSING 
        // 
        // creates File for command script, then creates Scanner using said File. 
        // does so in order to be able to effectively parse command script file
        File commandScriptFile = new File(args[1]);
        Scanner commandScanner = new Scanner(commandScriptFile);
        
        
        // array used for storing current command line's information
        // size 4 due to maximum size of command arguments 
        String[] currCommand = new String[4];

        // parsing begins
        while (commandScanner.hasNextLine())
        {
            // update currCommand with next line needing to be processed
            currCommand = validLine(commandScanner.nextLine());
            
            // while current line is invalid
            while (currCommand.length == 1)
            {
                // iterate through command file, read next line
                currCommand = validLine(commandScanner.nextLine());
            }
            
            // from this point onwards, currCommand will ALWAYS be a valid command line
            // so, iterate commandNumber
            commandNumber++;
            
            // "WORLD" COMMAND HANDOFF
            //
            if (currCommand[0].contains("world"))
            {               
                // call world helper (casting String to long via Long class)
                world(currCommand[1], currCommand[2], currCommand[3], currCommand[4]);
            }
            
            // "IMPORT" COMMAND HANDOFF
            //
            if (currCommand[0].contains("import"))
            {
                // call import helper
                importCommand(currCommand[1]);
            }
            
            // "SHOW" COMMAND HANDOFF
            //
            if (currCommand[0].contains("show"))
            {
                // if its requesting pool and pool is null
                if (currCommand[1].contains("pool") && pool == null)
                {
                    // do nothing!
                }
                else
                {
                    show(currCommand[1]);
                }
            }
            
            // "WHAT_IS" COMMAND HANDOFF
            //
            if (currCommand[0].equals("what_is"))
            {  
                // sentinel boolean representing if elem is found in pool
                boolean inPool = false;
                
                // CHECKS BUFFERPOOL
                //
                // if uninstantiated
                if (pool == null)
                {
                    pool = new bufferPool();
                }
                for (int idx = 0; idx < pool.size(); idx++)
                {
                    // if feature name is a subsequence of pool entry
                    if (pool.get(idx).contains(currCommand[1]))
                    {
                        // update sentinel
                        inPool = true;
                        
                        // the line being parsed
                        String currentData = pool.get(idx);
                        
                        // add line to front of pool
                        pool.insert(currentData);
                        
                        // grab and log needed data
                        String[] lineInformation = currentData.split("\\|");
                        
                        // indice 5 contains county name
                        // indice 8 contains primary long
                        // indice 7 contains primany lat
                        // format: offset: county name (long, lat)
                        
                        // FIND OFFSET FROM TABLE
                        // 
                        // create new dataEntry object to utilize find()
                        dataEntry poolFindEntry = new dataEntry(currCommand[1] + ":" + currCommand[2], (long)-1);
                        dataEntry poolFoundEntry = table.find(poolFindEntry);    
                        // find offset using table 
                        Long poolOffset = poolFoundEntry.locations.get(0);
                        
                        log.writeBytes("------------------------------------------------------------------\n" +
                            "Command " + commandNumber + "\twhat_is\t" + lineInformation[1] + "\t" + lineInformation[3] + "\n");
                    
                        log.writeBytes("\t" + poolOffset + ": " + lineInformation[5] + " (" +
                            formatLong(lineInformation[8]) + ", " + formatLat(lineInformation[7]) + ") \n");
                    }
                }               
                
                // if entry was not found to be in the pool, begin to check database
                if (!inPool)
                {
                    // CHECKS DATABASE
                    //
                    // indice 1 contains feature name
                    // indice 2 contains state abbreviation 
                    // create dataEntry object in order to use table's find()
                    dataEntry findEntry = new dataEntry(currCommand[1] + ":" + currCommand[2], (long)-1);
                
                    // call what_is helper
                    dataEntry foundEntry = table.find(findEntry);      
                
                    // initialize offset
                    Long currOffset;  
                
                    // iterate through locations
                    for (int idx = 0; idx < foundEntry.locations.size(); idx++)
                    {
                        // update offset
                        currOffset = foundEntry.locations.get(idx);
                    
                        // set raf to offset
                        database.seek(currOffset);
                        
                        // the line at given offset
                        String currentData = database.readLine();
                    
                        // add line to pool
                        pool.insert(currentData);
                    
                        // grab and log needed data
                        String[] lineInformation = currentData.split("\\|");
                    
                        // indice 5 contains county name
                        // indice 8 contains primary long
                        // indice 7 contains primany lat
                        // format: offset: county name (long, lat)
                        log.writeBytes("------------------------------------------------------------------\n" +
                            "Command " + commandNumber + "\twhat_is\t" + lineInformation[1] + "\t" + lineInformation[3] + "\n");
                    
                        log.writeBytes("\t" + currOffset + ": " + lineInformation[5] + " (" +
                            formatLong(lineInformation[8]) + ", " + formatLat(lineInformation[7]) + ") \n");  
                    
                    }
                }
            }
            // "WHAT_IS_AT" COMMAND HANDOFF
            //
            if (currCommand[0].contains("what_is_at"))
            {;
                // call what_is_at helper
                // 
                // indice 1 contains raw lat
                // indice 2 contains raw long
                what_is_at(currCommand[1], currCommand[2]);
                
            }
            
            // "WHAT_IS_IN" COMMAND HANDOFF
            //
            if (currCommand[0].contains("what_is_in"))
            {
                // call what_is_in helper
                
            }
        } 
    }
    
    /**
     *  Helper method used to process "world" command from command script
     *  
     *  westLong, eastLong, southLat, northLat
     *  
     * @throws IOException 
     */
    public static void world(String westLong, String eastLong, String southLat, String northLat) throws IOException
    {
        // convert values in terms of world creation (DMS to seconds)
        // sum with actual seconds
        long xMin = convertLong(westLong);
        long xMax = convertLong(eastLong);
        long yMin = convertLat(southLat);
        long yMax = convertLat(northLat);
                
        // create new tree with converted values
        quadtree = new prQuadTree(xMin, xMax, yMin, yMax);
        
        // OUTPUT
        // 
        // header
        log.writeBytes("------------------------------------------------------------------\n");
        log.writeBytes("world\t" + westLong + " " + eastLong + " " + southLat + " " + northLat + "\n\n");
        log.writeBytes("The dimensions of the world are\n");
        
        // coordinates
        log.writeBytes("\t\t\t" + yMax + "\n");
        log.writeBytes("\t\t" + xMin + "\t\t" + xMax + "\n");
        log.writeBytes("\t\t\t" + yMin + "\n");               
    }
    
    /**
     * Adds parametized GIS file data to our database
     * 
     * @throws Exception 
     */
    public static void importCommand(String appendFileName) throws Exception
    {
        // header for log file
        log.writeBytes("------------------------------------------------------------------\n");
        log.writeBytes("Command " + commandNumber + "\timport " + appendFileName + "\n"); 
            
        // create scanner for file so that we may begin to parse and add to our database
        File appendFile = new File(appendFileName);
        RandomAccessFile dataFind = new RandomAccessFile(appendFileName, "r");
        Scanner appendScanner = new Scanner(appendFile);
        
        // scanner for copying line by line to database
        Scanner readLine = new Scanner(appendFile);
        
        // apply delimiter
        appendScanner.useDelimiter("\\|"); 
        
        // init offset & array that will be filled with GIS information
        long offset;
        String[] currEntry = new String[19];
        
        // iterate scanners
        appendScanner.nextLine();
        readLine.nextLine();

        // parse through entire file
        while (readLine.hasNextLine())
        {
            // define our current line
            String newLine = readLine.nextLine();     
            System.out.println(newLine);
            
            // offset for creating our dataEntry
            Long previousOffset = database.getFilePointer();
            
            // append line to database
            database.writeBytes(newLine + "\n");
            
            // HASHTABLE INSERT
            // 
            // iterates and fills variables with proper information
            for (int idx = 0; idx < 19; idx++)
            {
                String nextElem = appendScanner.next();
                
                // if indice is beginning, do not let it contain "/"
                if (idx == 0 && nextElem.contains("//"))
                {
                    nextElem = appendScanner.next();
                }
                
                currEntry[idx] = nextElem;            
            }
            
            // update offset
            dataFind.readLine();
            offset = dataFind.getFilePointer();
            
            // create dataEntry object for table
            dataEntry newTableEntry = new dataEntry(currEntry[1] + ":" + currEntry[3], previousOffset);
            
            // insert entry
            table.insert(newTableEntry);
            
            //QUADTREE INSERT
            // 
            // value of currEntry[7] is primary latitude
            // value of currEntry[8] is primary longitude
            // create a gisEntry to be inserted with the above information & previously found offset

            gisEntry newGisEntry = new gisEntry(convertLat(currEntry[7]), convertLong(currEntry[8]), offset);
            
            // insert entry
            quadtree.insert(newGisEntry);
        }
        
        // close scanner
        appendScanner.close();       
    }
    
    /**
     * helper method to execute the behavior of the what_is_at command
     * 
     * @param rawLat : raw latitude parameter of command
     * @param rawLong : raw longitude parameter of command
     * 
     * @throws IOException 
     */
    private static void what_is_at(String rawLat, String rawLong) throws IOException
    {
        // iterate & check buffer pool for match
        for (int idx = 0; idx < pool.size(); idx++)
        {
            // represents current string being checked
            String currRecord = pool.get(idx);
            
            // if contains both raw datasets
            if (currRecord.contains(rawLat) && currRecord.contains(rawLong))
            {
                // record is in the pool
                // grab needed information from pool record
                // offset, feature name, county, state abbreviation
                
                // form data array
                String[] lineInformation = currRecord.split("\\|");
                
                // log appropriate data
                log.writeBytes("whatever is below");
            }
        }
        
        // DATABASE FINDING
        //       
        // convert raw coordinates to seconds
        Long latitude = convertLat(rawLat);
        Long longitude = convertLong(rawLong);
        
        // set database to beginning
        database.seek(0);
        
        // create scanner for parsing
        Scanner databaseScanner = new Scanner(databaseFile);
        
        // parse database
        while (databaseScanner.hasNextLine())
        {
            // current line
            Long currOffset = database.getFilePointer();
            String currLine = databaseScanner.nextLine();
            
            // if contains coordinates
            if (currLine.contains(rawLat) && currLine.contains(rawLong))
            {
                // add line to pool
                pool.insert(currLine);
            
                // grab and log needed data
                String[] lineInformation = currLine.split("\\|");
                
                // log needed output
                log.writeBytes("------------------------------------------------------------------\n");
                log.writeBytes("Command " + commandNumber + "\twhat_is_at\t" + rawLat + "\t" + rawLong + "\n");
                
                // indice 1 is feature name
                // indice 5 is county name
                // indice 3 is state abbreviation
                log.writeBytes("\t" + currOffset + "\t" + lineInformation[1] + "\t"
                    + lineInformation[5] + "\t" + lineInformation[3] + "\n");
            }
            
            // update and iterate for accurate offset
            database.readLine();
        }
    }
    
    
    /**
     * Catch all show method that will write to log file depending on parameterized string
     * 
     * Handles quad, hash, pool
     * 
     * @param type
     * @throws IOException 
     */
    public static void show(String type) throws IOException
    {
        // show prQuadTree
        if (type.equals("quad"))
        {
            // header information
            log.writeBytes("------------------------------------------------------------------" + "\n");
            log.writeBytes("Command " + commandNumber + "\tshow\t" + type + "\n\n"); 
            log.writeBytes("Showing Quadtree" + "\n");
 
            // display actual tree
            log.writeBytes(quadtree.toString());
        }
            
        // show hashtable
        if (type.equals("hash"))
        {
            // header information
            log.writeBytes("------------------------------------------------------------------" + "\n");
            log.writeBytes("Command " + commandNumber + "\tshow\t" + type + "\n\n"); 
            log.writeBytes("Showing Hashtable" + "\n");
            
            // display actual hashtable
            log.writeBytes(table.display());
        }
            
        // show buffer pool
        if (type.equals("pool"))
        {
            // header information
            log.writeBytes("------------------------------------------------------------------" + "\n");
            log.writeBytes("Command " + commandNumber + "\tshow\t" + type + "\n\n"); 
            log.writeBytes("\tShowing buffer pool" + "\n");
            log.writeBytes("\tThe pool is currently size " + pool.size() + "\n\n");
            log.writeBytes("\tMRU\n");
            
            // iterate through buffer pool array and print contents
            for (int idx = 0; idx < pool.size(); idx++)
            {
                log.writeBytes("\t" + pool.get(idx) + "\n");
            }
            
            // more header information
            log.writeBytes("\tLRU\n");
        }
    }
    
    /**
     * Helper method that accepts a parametized current line and returns
     * an array depending on the validity of the line
     * 
     * The returned array will be of length 1 if invalid
     * 
     * @param currCommand : the current command string being considered
     * @return : array depending on validity of line
     * 
     * @throws IOException 
     */
    public static String[] validLine(String currCommand) throws IOException
    {                             
        // array to be returned
        String[] commEntry;
        
        if (currCommand.isBlank())
        {
            String[] skip = new String[1];  
            return skip;
        }
        
        // VALID LINE
        if (currCommand.charAt(0) != ';' && currCommand.charAt(0) != ' ')
        {
            // update command data array
            commEntry = currCommand.split("\t");
           
            // checks if calling quit
            if (commEntry.length == 1)
            {
                // return 1 length array that contains "quit"
                String[] quit = new String[1];
                quit[0] = "quit";
                
                log.writeBytes("------------------------------------------------------------------");
                log.writeBytes("\nCommand " + (commandNumber + 1) + "\t quit\n");
                log.writeBytes("\tFound quit command... ending processing...");
                
                System.exit(0);
                return quit;
            }
            // return command information array
            return commEntry;
        }
        // NOT VALID LINE
        else
        {
            // return 1 length array that contains "skip"
            String[] skip = new String[1];
            skip[0] = "skip";
            return skip;           
        }   
    }
    
    /**
     * helper method that accepts full raw latitude and returns the value
     * converted fully to seconds
     * 
     * @param rawCoord : raw latitude 
     * @return secondsSum : sum of conversion
     */
    private static long convertLat(String rawCoord)
    {        
        // initialize return var
        long secondsSum = 0;
        
        // appends degrees digits and unit    
        String degree = rawCoord.substring(0, 2);

        if (degree.charAt(0) == '0' && degree.charAt(1) == '0')
        {
            degree = degree.substring(2);
        }       
        else if (degree.charAt(0) == '0')
        {
            // cuts off beginning 0
            degree = degree.substring(1);
        }    
        
        // convert and add to sum
        secondsSum += (Long.parseLong(degree) * 3600);
        
        // appends minutes and unit
        String min = rawCoord.substring(2, 4);
        
        if (min.charAt(0) == '0')
        {
            // cuts off beggining 0
            min = min.substring(1);
        }
        
        // convert and add to sum
        secondsSum += (Long.parseLong(min) * 60);
        
        // appends seconds and unit
        String sec = rawCoord.substring(4, 6);
        if (sec.charAt(0) == '0')
        {
            // cuts off beginning 0
            sec = sec.substring(1);
        }
        
        // add to sum 
        secondsSum += Long.parseLong(sec);
        
        // makes our return value negative if indicating south
        if (rawCoord.charAt(6) == 'S')
        {
            secondsSum *= -1;
        }
        
        // return sum
        return secondsSum;      
    }
    
    /**
     * helper method that accepts full raw longitude and returns the value
     * converted fully to seconds
     * 
     * @param rawCoord : raw longitude 
     * @return secondsSum : sum of conversion
     */
    public static long convertLong(String rawCoord)
    {
        // initialize return var
        long secondsSum = 0;
        
        // appends degree digits and unit
        String degree = rawCoord.substring(0, 3);
        
        if (degree.charAt(0) == '0' && degree.charAt(1) == '0')
        {
            // cuts off first two 0's
            degree = degree.substring(2);
        }    
        else if (degree.charAt(0) == '0')
        {
            //cuts off beggining 0
            degree = degree.substring(1);
        }
        
        // convert and add to sum
        secondsSum += (Long.parseLong(degree) * 3600);
        
        // appends minutes and unit
        String min = rawCoord.substring(3, 5);
        if (min.charAt(0) == '0')
        {
            // cuts off beggining 0
            min = min.substring(1);
        }
        // convert and add to sum
        secondsSum += (Long.parseLong(min) * 60);
        
        
        // appends seconds and unit
        String sec = rawCoord.substring(5, 7);
        if (sec.charAt(0) == '0')
        {
            // cuts off beginning 0
            sec = sec.substring(1);
        }
        
        // add to sum 
        secondsSum += Long.parseLong(sec);
        
        // converts sum to negative if longitude indicates west
        if (rawCoord.charAt(7) == 'W')
        {
            secondsSum *= -1;
        }
        
        // return sum
        return secondsSum;
    }
    
    /**
     * helper method to reformat coordinates
     * 
     * @param rawCoord
     * @return retString
     */
    public static String formatLat(String rawCoord)
    {        
        // 321643N --> 32d 16m 43s North
        // 320210N --> 32d 2m 10s North
        
        // Stringbuilder object to aid with concatenation
        StringBuilder sb = new StringBuilder();
        
        // appends day digits and unit    
        String day = rawCoord.substring(0, 2);

        if (day.charAt(0) == '0' && day.charAt(1) == '0')
        {
            day = day.substring(2);
        }       
        else if (day.charAt(0) == '0')
        {
            // cuts off beginning 0
            day = day.substring(1);
        }    
        sb.append(day);
        sb.append("d ");
        
        // appends minutes and unit
        String min = rawCoord.substring(2, 4);
        
        if (min.charAt(0) == '0')
        {
            // cuts off beggining 0
            min = min.substring(1);
        }
        sb.append(min);
        sb.append("m ");
        
        // appends seconds and unit
        String sec = rawCoord.substring(4, 6);
        if (sec.charAt(0) == '0')
        {
            // cuts off beginning 0
            sec = sec.substring(1);
        }
        sb.append(sec);
        sb.append("s");
        
        // appends N/S
        if (rawCoord.charAt(6) == 'N')
        {
            sb.append(" North");
        }
        else
        {
            sb.append(" South");
        }
        
        // return complete string
        return sb.toString();
        
    }
    
    /**
     * helper method to reformat coordinates
     * 
     * @param rawCoord
     * @return retString
     */
    public static String formatLong(String rawCoord)
    {
        // 1090224W --> 109d 2m 24s West
        // 1042410W --> 104d 24m 13s West
        
        // Stringbuilder object to aid with concatenation
        StringBuilder sb = new StringBuilder();
        
        // appends day digits and unit
        String day = rawCoord.substring(0, 3);
        
        if (day.charAt(0) == '0' && day.charAt(1) == '0')
        {
            // cuts off first two 0's
            day = day.substring(2);
        }    
        else if (day.charAt(0) == '0')
        {
            //cuts off beggining 0
            day = day.substring(1);
        }
        sb.append(day);
        sb.append("d ");
        
        // appends minutes and unit
        String min = rawCoord.substring(3, 5);
        if (min.charAt(0) == '0')
        {
            // cuts off beggining 0
            min = min.substring(1);
        }
        sb.append(min);
        sb.append("m ");
        
        // appends seconds and unit
        String sec = rawCoord.substring(5, 7);
        if (sec.charAt(0) == '0')
        {
            // cuts off beginning 0
            sec = sec.substring(1);
        }
        sb.append(sec);
        sb.append("s ");
        
        // appends W/E
        if (rawCoord.charAt(7) == 'W')
        {
            sb.append("West");
        }
        else
        {
            sb.append("East");
        }
        
        // return complete string
        return sb.toString();
    }
}
