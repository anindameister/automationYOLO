package eu.qanswer.linking;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import eu.qanswer.linking.utils.Hungarian;
import eu.qanswer.linking.utils.LinkKeys;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Main {

    @Parameter(names={"--query1", "-q1"})
    private String query1;

    @Parameter(names={"--query2", "-q2"})
    private String query2;

    @Parameter(names={"--service1", "-s1"})
    private String service1;

    @Parameter(names={"--service2", "-s2"})
    private String service2;

    @Parameter(names = {"--label","-l"})
    private String label;

    @Parameter(names = {"--output","-o"})
    private String outputFilePath;

    @Parameter(names={"--owlFile", "-owl"})
    private String owlFile;

    @Parameter(names={"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names={"--outputFile", "-o"})
    private String outputFile;

    @Parameter(names={"--type", "-t"})
    private String typeOfDataset;


    @Parameter(names = "--help", help = true)
    private boolean help = false;

    public static void main(String[] argv){
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(argv);
        main.run();
    }

    public void run() {
        if (help) {
            System.out.println("Help Yourself");
        }
        else {
            String endpoint1 = service1;
            String endpoint2 = service2;
            String sparql1 = query1;
            String sparql2 = query2;
            ArrayList<Pair<String, String>> linkedPairs=new ArrayList<Pair<String, String>>();

            if(label.toLowerCase().equals("true")) {
                //Find levenstein distance between all strings
                Query query1 = QueryFactory.create(sparql1);
                Query query2 = QueryFactory.create(sparql2);

                QueryEngineHTTP qExe1 = new QueryEngineHTTP(endpoint1, query1);
                qExe1.addParam("format", "json");
                ResultSet result1 = qExe1.execSelect();
                ArrayList<Pair<String, String>> arrayList1 = fromResultSetToArrayList(result1);

                QueryEngineHTTP qExe2 = new QueryEngineHTTP(endpoint2, query2);
                qExe2.addParam("format", "json");
                ResultSet result2 = qExe2.execSelect();
                ArrayList<Pair<String, String>> arrayList2 = fromResultSetToArrayList(result2);

                System.out.println(arrayList1.size());
                System.out.println(arrayList2.size());
                int max = Math.max(arrayList1.size(), arrayList2.size());
                int[][] matrix = new int[max][max];

                for (int i = 0; i < arrayList1.size(); i++) {
                    for (int j = 0; j < arrayList2.size(); j++) {
                        matrix[i][j] = StringUtils.getLevenshteinDistance(arrayList1.get(i).getRight(), arrayList2.get(j).getRight());
                    }
                }

                //Execute Hungarian algorithm
                Hungarian hungarian = new Hungarian(matrix);
                int[] result = hungarian.getResult();
                // Display result on screen
                for (int i = 0; i < result.length; i++) {
                    if (i < arrayList1.size() && i < arrayList2.size())
                    {
                        System.out.println(arrayList1.get(i).getLeft()+" ===> "+arrayList2.get(result[i]).getLeft());
                        linkedPairs.add(new Pair<String, String>(arrayList1.get(i).getLeft(),arrayList2.get(result[i]).getLeft()));
                    }
                }
                writeInFile(linkedPairs);
            } else if (label.toLowerCase().equals("false")){
                System.out.println("false");
                linkedPairs = LinkKeys.linkKeys(endpoint1, query1, endpoint2, query2);
                writeInFile(linkedPairs);
            }

        }
    }


    private void writeInFile(ArrayList<Pair<String, String>> correspondence) {
        try {
            StreamRDF writer = StreamRDFWriter.getWriterStream(new FileOutputStream(new File(outputFilePath)), RDFFormat.NTRIPLES);
            for (int i = 0; i < correspondence.size(); i++) {
                String key = correspondence.get(i).getLeft();
                Node subject = NodeFactory.createURI(key);
                Node predicate = NodeFactory.createURI("http://www.w3.org/2002/07/owl#sameAs");
                Node object = NodeFactory.createURI(correspondence.get(i).getRight());
                Triple triple = new Triple(subject, predicate, object);
                writer.triple(triple);
            }
            writer.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static ArrayList<Pair<String, String>> fromResultSetToArrayList(ResultSet resultSet)
    {
        ArrayList<Pair<String, String>> arrayList=new ArrayList<Pair<String, String>>();
        while (resultSet.hasNext())
        {
            QuerySolution next = resultSet.next();
            if(next.get("s1")!=null && next.get("label")!=null)
                arrayList.add(new Pair<String, String>(next.get("s1").toString().trim(),next.get("label").toString().trim()));
        }
        return arrayList;
    }
}
