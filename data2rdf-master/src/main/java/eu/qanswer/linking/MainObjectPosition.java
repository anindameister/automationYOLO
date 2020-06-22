package eu.qanswer.linking;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import eu.qanswer.linking.utils.Parser;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import java.io.File;
import java.io.FileOutputStream;

public class MainObjectPosition {


    @Parameter(names = {"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names = {"--outputFile", "-o"})
    private String outputFile;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    public static void main(String[] args) throws Exception {
        MainObjectPosition main = new MainObjectPosition();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        main.run();
    }

    public void run() throws Exception {
        if (help) {
            System.out.println("Help Yourself");
            System.out.println(
                    "-o : outputfile\n" +
                            "-d : datasetfile"
            );
        } else {
            run(outputFile, datasetFile);
        }
    }

    public static void run(String outputFile, String datasetFile) throws Exception {
        if (datasetFile != null && outputFile != null) {
            // first create files that separate the URI  we want to replace
            PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
            StreamRDF writer = StreamRDFWriter.getWriterStream(
                    new FileOutputStream(new File(outputFile)), RDFFormat.NTRIPLES);

            while (iteratorDataset.hasNext()) {
                Triple tripleDataset = iteratorDataset.next();
                Node subjectDataset = tripleDataset.getSubject();
                Node objectDataset = tripleDataset.getObject();
                Node predicateDataset = tripleDataset.getPredicate();
                writer.triple(tripleDataset);
            }
            iteratorDataset.close();
            PipedRDFIterator<Triple> iteratorDatasetOntology = Parser.parse(datasetFile + "_ontology");
            while (iteratorDatasetOntology.hasNext()) {
                Triple tripleDataset = iteratorDatasetOntology.next();
                Node subjectDataset = tripleDataset.getSubject();
                Node objectDataset = tripleDataset.getObject();
                Node predicateDataset = tripleDataset.getPredicate();
                writer.triple(tripleDataset);
            }
            iteratorDatasetOntology.close();
            writer.finish();

        }
    }
}