/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cool
 */



import de.lmu.ifi.dbs.elki.application.greedyensemble.GreedyEnsembleExperiment.Distance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.distance.DoubleDistanceDBIDPairList;
import de.lmu.ifi.dbs.elki.database.ids.distance.DoubleDistanceKNNHeap;
import de.lmu.ifi.dbs.elki.database.ids.distance.DoubleDistanceKNNList;
import de.lmu.ifi.dbs.elki.database.query.DatabaseQuery;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.query.range.RangeQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.database.relation.RelationUtil;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.LPNormDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.index.AbstractRefiningIndex;
import de.lmu.ifi.dbs.elki.index.IndexFactory;
import de.lmu.ifi.dbs.elki.index.KNNIndex;
import de.lmu.ifi.dbs.elki.index.RangeIndex;
import de.lmu.ifi.dbs.elki.index.vafile.VectorApproximation;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.persistent.AbstractPageFileFactory;
import de.lmu.ifi.dbs.elki.utilities.datastructures.heap.DoubleMaxHeap;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;
import de.lmu.ifi.dbs.elki.utilities.pairs.DoubleObjPair;


public class vaf<V extends NumberVector<?>> extends AbstractRefiningIndex<V> implements KNNIndex<V> {

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
  /**
   * Constructor.
   * 
   * @param pageSize Page size of simulated index
   * @param relation Relation to index
   * @param partitions Number of partitions for each dimension.
   */
  public vaf(int pageSize, ArrayList<Vector> data, int partitions) {
    //this.relation=relation;
    this.relation=data;
    this.partitions = partitions;
    this.pageSize = pageSize;
    this.scans = 0;
    this.vectorApprox = new ArrayList<>();
  }

  @Override
  public void initialize() {
    setPartitions(this.relation);
    for(int iditer = 0; iditer<relation.size(); iditer++) {
      
      vectorApprox.add(calculateApproximation(iditer, relation.get(iditer)));
    }
  }

  /**
   * Initialize the data set grid by computing quantiles.
   * 
   * @param relation Data relation
   * @throws IllegalArgumentException
   */
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

  /**
   * Get the number of scanned bytes.
   * 
   * @return Number of scanned bytes.
   */
  public long getScannedPages() {
    int vacapacity = pageSize / VectorApproximation.byteOnDisk(splitPositions.length, partitions);
    int vasize = (int) Math.ceil((vectorApprox.size()) / (1.0 * vacapacity));
    return vasize * scans;
  }

  
 

  @Override
  public String getLongName() {
    return "VA-file index";
  }

  @Override
  public String getShortName() {
    return "va-file";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <D extends Distance<D>> KNNQuery<Vector, D> getKNNQuery(DistanceQuery<Vector, D> distanceQuery, Object... hints) {
    for(Object hint : hints) {
      if(hint == DatabaseQuery.HINT_BULK) {
        // FIXME: support bulk?
        return null;
      }
    }
    DistanceFunction<? super V, ?> df = distanceQuery.getDistanceFunction();
    if(df instanceof LPNormDistanceFunction) {
      double p = ((LPNormDistanceFunction) df).getP();
      DistanceQuery<V, ?> ddq = (DistanceQuery<V, ?>) distanceQuery;
      KNNQuery<V, ?> dq = new VAFileKNNQuery((DistanceQuery<V, DoubleDistance>) ddq, p);
      return (KNNQuery<V, D>) dq;
    }
    // Not supported.
    return null;
  }




  /**
   * KNN query for this index.
   * 
   * @author Erich Schubert
   */
  public class VAFileKNNQuery extends AbstractRefiningIndex<V>.AbstractKNNQuery<DoubleDistance> {
    /**
     * LP Norm p parameter.
     */
    final double p;

    /**
     * Constructor.
     * 
     * @param distanceQuery Distance query object
     * @param p LP norm p
     */
    public VAFileKNNQuery(DistanceQuery<V, DoubleDistance> distanceQuery, double p) {
      super(distanceQuery);
      this.p = p;
    }

    @Override
    public DoubleDistanceKNNList getKNNForObject(V query, int k) {
      // generate query approximation and lookup table
      VectorApproximation queryApprox = calculateApproximation( iditer, relation.get(iditer));

      // Approximative distance function
      VALPNormDistance vadist = new VALPNormDistance(p, splitPositions, query, queryApprox);

      // Heap for the kth smallest maximum distance (yes, we need a max heap!)
      DoubleMaxHeap minMaxHeap = new DoubleMaxHeap(k + 1);
      double minMaxDist = Double.POSITIVE_INFINITY;
      // Candidates with minDist <= kth maxDist
      ArrayList<DoubleObjPair<DBID>> candidates = new ArrayList<>(vectorApprox.size());

      // Count a VA file scan
      scans += 1;

      // Approximation step
      for(int i = 0; i < vectorApprox.size(); i++) {
        myVectorApproximation va = vectorApprox.get(i);
        double minDist = vadist.getMinDist(va);
        double maxDist = vadist.getMaxDist(va);

        // Skip excess candidate generation:
        if(minDist > minMaxDist) {
          continue;
        }
        candidates.add(new DoubleObjPair<>(minDist, va.id));

        // Update candidate pruning heap
        minMaxHeap.add(maxDist, k);
        if(minMaxHeap.size() >= k) {
          minMaxDist = minMaxHeap.peek();
        }
      }
      // sort candidates by lower bound (minDist)
      Collections.sort(candidates);

      // refinement step
      DoubleDistanceKNNHeap result = DBIDUtil.newDoubleDistanceHeap(k);

      // log.fine("candidates size " + candidates.size());
      // retrieve accurate distances
      for(DoubleObjPair<DBID> va : candidates) {
        // Stop when we are sure to have all elements
        if(result.size() >= k) {
          double kDist = result.doubleKNNDistance();
          if(va.first > kDist) {
            break;
          }
        }

        // refine the next element
        final double dist = refine(va.second, query).doubleValue();
        result.insert(dist, va.second);
      }
      if(LOG.isDebuggingFinest()) {
        LOG.finest("query = (" + query + ")");
        LOG.finest("database: " + vectorApprox.size() + ", candidates: " + candidates.size() + ", results: " + result.size());
      }

      return result.toKNNList();
    }
  }

  /**
   * Index factory class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.stereotype factory
   * @apiviz.has VAFile
   * 
   * @param <V> Vector type
   */
  public static class Factory<V extends NumberVector<?>> implements IndexFactory<V, VAFile<V>> {
    /**
     * Number of partitions to use in each dimension.
     * 
     * <pre>
     * -vafile.partitions 8
     * </pre>
     */
    public static final OptionID PARTITIONS_ID = new OptionID("vafile.partitions", "Number of partitions to use in each dimension.");

    /**
     * Page size.
     */
    int pagesize = 1;

    /**
     * Number of partitions.
     */
    int numpart = 2;

    /**
     * Constructor.
     * 
     * @param pagesize Page size
     * @param numpart Number of partitions
     */
    public Factory(int pagesize, int numpart) {
      super();
      this.pagesize = pagesize;
      this.numpart = numpart;
    }

    @Override
    public VAFile<V> instantiate(Relation<V> relation) {
      return new VAFile<>(pagesize, relation, numpart);
    }

    @Override
    public TypeInformation getInputTypeRestriction() {
      return TypeUtil.NUMBER_VECTOR_FIELD;
    }

    /**
     * Parameterization class.
     * 
     * @author Erich Schubert
     * 
     * @apiviz.exclude
     */
    public static class Parameterizer extends AbstractParameterizer {
      /**
       * Page size.
       */
      int pagesize = 1;

      /**
       * Number of partitions.
       */
      int numpart = 2;

      @Override
      protected void makeOptions(Parameterization config) {
        super.makeOptions(config);
        IntParameter pagesizeP = new IntParameter(AbstractPageFileFactory.Parameterizer.PAGE_SIZE_ID, 1024);
        pagesizeP.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
        if(config.grab(pagesizeP)) {
          pagesize = pagesizeP.getValue();
        }
        IntParameter partitionsP = new IntParameter(Factory.PARTITIONS_ID);
        partitionsP.addConstraint(CommonConstraints.GREATER_THAN_ONE_INT);
        if(config.grab(partitionsP)) {
          numpart = partitionsP.getValue();
        }
      }

      @Override
      protected Factory<?> makeInstance() {
        return new Factory<>(pagesize, numpart);
      }
    }
  }
}
