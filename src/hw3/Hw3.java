/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hw3;

import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTree;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeFactory;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeIndex;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import de.lmu.ifi.dbs.elki.math.linearalgebra.*;
import de.lmu.ifi.dbs.elki.persistent.AbstractPageFileFactory;
import de.lmu.ifi.dbs.elki.persistent.PageFileFactory;

/**
 *
 * @author cool
 */
public class Hw3 {

    static ArrayList<de.lmu.ifi.dbs.elki.math.linearalgebra.Vector> data;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        /*
         * get data file
         */
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter path to dataset (.csv______options : 8D,4D,2D)");
        String pathToDataSetFile = sc.nextLine();
        LoadData(pathToDataSetFile);
        /*
         * options 2D,4D,8D
         */
         
        System.out.println("___________Data Loaded______Options:Enter R(R tree index) or V(VA file Index)");
        String indexType = sc.next();
        /*
         * Index data [options]
         */
        switch (indexType) {
            case "R":
            case "r":
                //RStarTreeIndex<Vector> rindex=new RStarTreeIndex(null, null, null);
                
                //RStarTreeFactory f=new 
                
                
                break;
            case "V":
            case "v":
                break;
            default:
                System.out.println("Invalid");
                break;
        }
        /*
         * generate a random query point from data itself
         */
        String continueQuery = "y";
        while (continueQuery.equals("Y") || continueQuery.equals("y")) {
            System.out.println("Enter the value ok K : ");

            Integer k = sc.nextInt();
            /*
             * start time
             */
            /*
             * Process
             */
            /*
             * end time
             */
            /*
             * display reslts and time
             */
            System.out.println("________Continue ???? ____________");
            continueQuery = sc.next();
        }
    }

    public static void LoadData(String pathToDataSetFile) throws FileNotFoundException, IOException {
        BufferedReader fileReader;
        fileReader = new BufferedReader(new FileReader(pathToDataSetFile));
        String line;
        data = new ArrayList<de.lmu.ifi.dbs.elki.math.linearalgebra.Vector>();


        while ((line = fileReader.readLine()) != null) {
            Vector v = new Vector(2);
            
            String[] tokens = line.split(",");
            for(int l = 0;l<tokens.length;l++)
            {
                v.set(l, Double.parseDouble(tokens[l]));
            }    
            data.add(v);
        
    }
    }
    public static void Display() {
        //System.out.println(ListData.size());
        for (int i = 0; i < 1; i++) {
            //  System.out.println(ListData.get(i).getObjectName() + " " + ListData.get(i).getAttribute1() + " " + ListData.get(i).getAttribute2());
        }

    }
}
