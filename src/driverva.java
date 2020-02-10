
import de.lmu.ifi.dbs.elki.index.vafile.VAFile;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cool
 */
public class driverva {
 
     private List<myVectorApproximation> vectorApprox;

  /**
   * Number of partitions.
   */
  private int partitions;

  /**
   * Quantile grid we use.
   */
  private double[][] splitPositions;

  /**
   * Page size, for estimating the VA file size.
   */
  int pageSize;

  /**
   * Number of scans we performed.
   */
  int scans;
  private ArrayList<Vector> relation;
    public driverva(ArrayList<Vector> data, int partitions) {
    //this.relation=relation;
    this.relation=data;
    this.partitions = partitions;
    this.vectorApprox = new ArrayList<>();
  }
    
    public void initialize() {
    setPartitions(this.relation);
    for(int iditer = 0; iditer<relation.size(); iditer++) {
      
      vectorApprox.add(calculateApproximation(iditer, relation.get(iditer)));
    }
  }
     public void setPartitions(ArrayList<Vector> relation) throws IllegalArgumentException {
    
    final int dimensions = relation.get(0).getDimensionality(); /*Size of one data*/
    final int size = relation.size();
    splitPositions = new double[dimensions][partitions + 1];

    for(int d = 0; d < dimensions; d++) {
      double[] tempdata = new double[size];
      int j = 0;
      for(int iditer = 0; iditer<relation.size(); iditer++) {
        tempdata[j] = relation.get(iditer).doubleValue(d);
        j += 1;
      }
      Arrays.sort(tempdata);
   //check methods ----   Vector v=new Vector();
   
      for(int b = 0; b < partitions; b++) {
        int start = (int) (b * size / (double) partitions);
        splitPositions[d][b] = tempdata[start];
      }
      // make sure that last object will be included
      splitPositions[d][partitions] = tempdata[size - 1] + 0.000001;
    }
  }

  /**
   * Calculate the VA file position given the existing borders.
   * 
   * @param id Object ID
   * @param dv Data vector
   * @return Vector approximation
   */
  public myVectorApproximation calculateApproximation(int iditer, Vector dv) {
    int approximation[] = new int[dv.getDimensionality()];
    for(int d = 0; d < splitPositions.length; d++) {
      final double val = dv.doubleValue(d);
      final int lastBorderIndex = splitPositions[d].length - 1;

      // Value is below data grid
      
      
        // Search grid position
        int pos = Arrays.binarySearch(splitPositions[d], val);
        pos = (pos >= 0) ? pos : ((-pos) - 2);
        approximation[d] = pos;
      
    }
    return new myVectorApproximation(iditer, approximation);
  }
    
}
