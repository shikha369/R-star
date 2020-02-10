/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cool
 */
import java.util.Arrays;

import de.lmu.ifi.dbs.elki.database.ids.DBID;
//import de.lmu.ifi.dbs.elki.persistent.ByteArrayUtil;
import de.lmu.ifi.dbs.elki.utilities.io.ByteArrayUtil;

/**
 * Object in a VA approximation.
 * 
 * @author Thomas Bernecker
 * @author Erich Schubert
 */
public class myVectorApproximation {
  /**
   * approximation (va cell ids)
   */
  int[] approximation;

  /**
   * Object represented by this approximation
   */
  protected int id;

  /**
   * Constructor.
   * 
   * @param id Object represented (may be <code>null</code> for query objects)
   * @param approximation Approximation
   */
  public myVectorApproximation(int id, int[] approximation) {
    super();
    this.id = id;
    this.approximation = approximation;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Get the dimensionality
   * 
   * @return Dimensionality
   */
  public int getDimensionality() {
    return approximation.length;
  }

  /**
   * Get the VA approximation
   * 
   * @param dim Dimension
   * @return Bin number
   */
  public int getApproximation(int dim) {
    return approximation[dim];
  }

  @Override
  public String toString() {
    return id + " (" + Arrays.toString(approximation) + ")";
  }

  /**
   * Computes IO costs (in bytes) needed for reading the candidates. For one
   * object, log2(numberOfPartitions) bits have to be read per dimension.
   * 
   * @param numberOfDimensions the number of relevant dimensions
   * @param numberOfPartitions the number of relevant partitions
   * @return the cost values (in bytes)
   */
  // nicht gleich in bytes umwandeln, sonst rundungsfehler erst nachdem *anzahl
  // objekte
  public static int byteOnDisk(int numberOfDimensions, int numberOfPartitions) {
    // (partition*dimension+id) alles in Bit 32bit für 4 byte id
    return (int) (Math.ceil(numberOfDimensions * ((Math.log(numberOfPartitions) / Math.log(2))) + 32) / ByteArrayUtil.SIZE_DOUBLE);
  }
}