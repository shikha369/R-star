
import java.io.*;
import java.util.List;
import java.util.Scanner;
import rstar.RStarTree;
import rstar.spatial.SpatialPoint;
import util.Utils;


public class driver {

    private void KnnQuery(int k) throws FileNotFoundException, IOException {
        /*read query file*/
        float[] point;
        long start, end;
        
        BufferedReader input = new BufferedReader(new FileReader("query.csv"));
        String line;
        String[] lineSplit;
        
        line = input.readLine();
        
        lineSplit = line.split(",");
        point = extractPoint(lineSplit, 0);
        
        
                        SpatialPoint center = new SpatialPoint(point);

                        start = System.currentTimeMillis();
                        List<SpatialPoint> result = tree.knnSearch(center, (int)k);
                        end = System.currentTimeMillis();

                        System.out.println(Utils.SpatialPointListToString(result));
                        System.out.println(end - start);
                     
					
			input.close();
            tree.save();
    }

    private RStarTree tree;
    private int dimension;
    private String inputFile;
   private String resultFile;
    //private List<Long> insertRunTime;
   // private List<Long> searchRunTime;
    //private List<Long> rangeRuntime;
    private List<Long> knnRuntime;
    

    public static void main(String[] arg) throws FileNotFoundException, IOException {
        Scanner sc = new Scanner(System.in);
        String[] args = new String[3];
        System.out.println("Path to Data file-----");
        args[0] = sc.nextLine();
        System.out.println("No of Dimensions-----");
        args[1] = sc.nextLine();
        System.out.println("Path to Result file-----");
        args[2] = sc.nextLine();
        driver controller = new driver(args);

        System.out.println("Reading input file ...");
        controller.BuildIndex();
        System.out.println("Finished Building Index...");
        
        String continueQuery = "y";
        while (continueQuery.equals("Y") || continueQuery.equals("y")) {
        
        System.out.println("Enter K value ...");
        int k=sc.nextInt();
        controller.KnnQuery(k);
        System.out.println("________Continue ???? ____________");
        continueQuery = sc.next();
        }
        //controller.writeRuntimeToFile(controller.insertRunTime, "Insertion_runtime.txt");
        //controller.writeRuntimeToFile(controller.searchRunTime, "Search_runtime.txt");
        //controller.writeRuntimeToFile(controller.rangeRuntime, "RangeSearch_runtime.txt");
        //controller.writeRuntimeToFile(controller.knnRuntime, "KNNSearch_runtime.txt");

       // controller.printResults();
    }

    public driver(String[] args) {
        if (args.length >= 2) {
            this.inputFile = args[0];
            this.dimension = Integer.parseInt(args[1]);

            if (args.length >= 3) {
                this.resultFile = args[2];
            } else {
                this.resultFile = this.getClass().getSimpleName() + "_Results.txt";
            }

        } 
        tree = new RStarTree(dimension);
        //this.insertRunTime = new ArrayList<Long>();
        //this.searchRunTime = new ArrayList<Long>();
        //this.rangeRuntime = new ArrayList<Long>();
        //this.knnRuntime = new ArrayList<Long>();
        //logger = Trace.getLogger(this.getClass().getSimpleName());
    }

    private float[] extractPoint(String[] points, int startPos) throws NumberFormatException {
        float[] tmp = new float[this.dimension];
        for (int i = startPos, lineSplitLength = points.length;
                ((i < lineSplitLength) && (i < (startPos + this.dimension))); i++) {
            tmp[i - startPos] = Float.parseFloat(points[i]);
        }
        return tmp;
    }

   
    private void BuildIndex() throws FileNotFoundException, IOException {
        //float opType,k;
        
        float[] point;
        //long start, end;
        //int lineNum = 0;

        BufferedReader input = new BufferedReader(new FileReader(this.inputFile));
        String line;
        String[] lineSplit;
        int oid=-1;
        /*Testing with 200 points*/
        while ((line = input.readLine()) != null) {
            oid++;
            lineSplit = line.split(",");
                point = extractPoint(lineSplit, 0);
                tree.insert(new SpatialPoint(point, oid));
        /*writing 100th point as query point*/
                FileWriter queryF ;
                if(oid==100)
                {
                queryF = new FileWriter("query.csv");
                for(int i=0;i<lineSplit.length;i++)
                {
                queryF.append(lineSplit[i]);
                
                if(i<lineSplit.length-1)
                queryF.append(",");
                
                }
                queryF.flush();
                queryF.close();
                }
        }
    }
}