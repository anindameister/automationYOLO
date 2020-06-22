package eu.qanswer.linking;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import eu.qanswer.linking.utils.Parser;
import eu.qanswer.linking.utils.SubstituteOwlSameAs;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;

public class Main2 {

    static final String endpoint = "https://query.wikidata.org/sparql";

    @Parameter(names = {"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names = {"--outputFile", "-o"})
    private String outputFile;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    public static void main(String[] args) throws Exception {
        Main2 main = new Main2();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        main.run();
    }

    private void run() throws Exception {
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

    public static String addPrefix(String prefix, String word) {
        String[] words = word.split("/");
        words[words.length - 1] = prefix + words[words.length - 1];
        return String.join("/", words);
    }

    public static void run(String outputFile, String datasetFile) throws Exception {
        if (datasetFile != null && outputFile != null) {
            // first create files that separate the URI  we want to replace
            StreamRDF writer = StreamRDFWriter.getWriterStream(
                    new FileOutputStream(new File(outputFile)), RDFFormat.NTRIPLES);
            StreamRDF writer_owl = StreamRDFWriter.getWriterStream(
                    new FileOutputStream(new File(addPrefix( "owl_", outputFile))), RDFFormat.NTRIPLES);
            PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
            while (iteratorDataset.hasNext()) {
                Triple tripleDataset = iteratorDataset.next();
                Node subjectDataset = tripleDataset.getSubject();
                Node objectDataset = tripleDataset.getObject();
                Node predicateDataset = tripleDataset.getPredicate();
                if (predicateDataset.toString().equals("http://www.w3.org/2002/07/owl#sameAs")) {
                    // add to owl
                    writer_owl.triple(tripleDataset);
                } else {
                    // add to other
                    writer.triple(tripleDataset);
                }
            }
            writer.finish();
            writer_owl.finish();
            //*
            // then substitute
            new SubstituteOwlSameAs().substitute(
                    addPrefix("owl_", outputFile),
                    outputFile,
                    addPrefix("clean_", outputFile),
                    "ntriples"
            );
            //*/
            // then get all data from the Wikidata URI

            PipedRDFIterator<Triple> triplesData = Parser.parse(addPrefix("clean_", outputFile));
            StreamRDF writer_complete = StreamRDFWriter.getWriterStream(
                    new FileOutputStream(new File(addPrefix("complete_", outputFile))), RDFFormat.NTRIPLES);


            HashSet<String> wikidataUrls = new HashSet<>();
            while (triplesData.hasNext()) {
                Triple triple = triplesData.next();
                Node predicate = triple.getPredicate();
                Node subject = triple.getSubject();
                Node object = triple.getObject();
                writer_complete.triple(triple);

                if (predicate.toString().contains("http://www.wikidata.org/prop/")) {
                    wikidataUrls.add(predicate.toString());
                }
                if (subject.toString().contains("http://www.wikidata.org/entity/")) {
                    wikidataUrls.add(subject.toString());
                }
                if (object.toString().contains("http://www.wikidata.org/entity/")) {
                    wikidataUrls.add(object.toString());
                }
            }
            String constructCovid19 = "CONSTRUCT { <http://www.wikidata.org/entity/Q84263196> ?p ?o } where { " +
                    "<http://www.wikidata.org/entity/Q84263196> ?p ?o . " +
                    "}";
            Query queryCovid19 = QueryFactory.create(constructCovid19);
            QueryExecution qexecCovid19 = QueryExecutionFactory.sparqlService(endpoint, queryCovid19);
            Model constructModelCovid19 = qexecCovid19.execConstruct();
            StmtIterator aCovid19 = constructModelCovid19.listStatements();
            while (aCovid19.hasNext()) {
                Statement statement = aCovid19.next();
                Property predicate = statement.getPredicate();
                Resource subject = statement.getSubject();
                RDFNode object = statement.getObject();
                writer_complete.triple(statement.asTriple());

                if (predicate.toString().contains("http://www.wikidata.org/prop/")) {
                    String[] predicateAddressSplit = predicate.toString().split("/");
                    String predicateName = predicateAddressSplit[predicateAddressSplit.length - 1];
                    wikidataUrls.add("http://www.wikidata.org/prop/direct/" + predicateName);
                }
                if (subject.toString().contains("http://www.wikidata.org/entity/")) {
                    wikidataUrls.add(subject.toString());
                }
                if (object.toString().contains("http://www.wikidata.org/entity/")) {
                    wikidataUrls.add(object.toString());
                }
            }

            HashSet<String> alreadyQuery = new HashSet<>();

            alreadyQuery.add("http://www.wikidata.org/entity/Q84263196");
            qexecCovid19.close();
            constructModelCovid19.close();
            wikidataUrls.add("http://www.wikidata.org/prop/direct/P131");

            HashSet<Statement> allStatements = new HashSet<>();
            HashSet<String> addedP131 = new HashSet<>();

            for (String url : wikidataUrls) {
                System.out.println("Url " + url);
                if (!alreadyQuery.contains(url)) {
                    if (url.contains("http://www.wikidata.org/prop/")) {
                        alreadyQuery.add(url);
                        String construct = "CONSTRUCT {?s <http://wikiba.se/ontology#directClaim> <" + url + "> . ?s ?p ?o} where { " +
                                "?s <http://wikiba.se/ontology#directClaim> <" + url + "> . " +
                                "?s ?p ?o " +
                                "}";
                        Query query = QueryFactory.create(construct);
                        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                        Model constructModel = qexec.execConstruct();

                        StmtIterator a = constructModel.listStatements();
                        while (a.hasNext()) {
                            Statement statement = a.next();

                            writer_complete.triple(statement.asTriple());
                        }
                        qexec.close();
                        constructModel.close();
                    }
                    if (url.contains("http://www.wikidata.org/entity/")) {
                        alreadyQuery.add(url);
                        String construct = "CONSTRUCT { <" + url + "> ?p ?o} where { " +
                                "<" + url + "> ?p ?o " +
                                "}";
                        Query query = QueryFactory.create(construct);
                        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                        Model constructModel = qexec.execConstruct();

                        StmtIterator a = constructModel.listStatements();
                        while (a.hasNext()) {
                            Statement statement = a.next();
                            String urlP = statement.getPredicate().toString();
                            if (!urlP.equals("http://www.wikidata.org/prop/direct/P1120")) {
                                writer_complete.triple(statement.asTriple());

                                if (!alreadyQuery.contains(urlP) && urlP.contains("http://www.wikidata.org/prop/")) {
                                    System.out.println("Url " + urlP);
                                    alreadyQuery.add(urlP);
                                    String constructP = "CONSTRUCT {?s <http://wikiba.se/ontology#directClaim> <" + urlP + "> . ?s ?p ?o} where { " +
                                            "?s <http://wikiba.se/ontology#directClaim> <" + urlP + "> . " +
                                            "?s ?p ?o " +
                                            "}";
                                    Query queryP = QueryFactory.create(constructP);
                                    QueryExecution qexecP = QueryExecutionFactory.sparqlService(endpoint, queryP);
                                    Model constructModelP = qexecP.execConstruct();

                                    StmtIterator aP = constructModelP.listStatements();
                                    while (aP.hasNext()) {
                                        Statement statementP = aP.next();
                                        writer_complete.triple(statementP.asTriple());
                                    }
                                    qexecP.close();
                                    constructModelP.close();
                                }
                            }
                        }
                        qexec.close();
                        constructModel.close();

                        String constructP131 = "CONSTRUCT { " +
                                " <" + url + "> <http://www.wikidata.org/prop/direct/P131> ?o " +
                                "} where {" +
                                " <" + url + "> <http://www.wikidata.org/prop/direct/P131>+ ?o " +
                                "}";
                        Query queryP131 = QueryFactory.create(constructP131);
                        QueryExecution qexecP131 = QueryExecutionFactory.sparqlService(endpoint, queryP131);
                        Model constructModelP131 = qexecP131.execConstruct();

                        StmtIterator aP131 = constructModelP131.listStatements();
                        while (aP131.hasNext()) {
                            Statement statement = aP131.next();
                            if (!addedP131.contains(statement.getObject().toString())) {
                                addedP131.add(statement.getObject().toString());
                                writer_complete.triple(new Triple(NodeFactory.createURI(statement.getObject().toString()),
                                        NodeFactory.createURI("http://www.wikidata.org/prop/direct/P131"),
                                        NodeFactory.createURI(statement.getObject().toString())
                                ));
                            }
                            writer_complete.triple(statement.asTriple());
                        }

                        String constructObjP131 = "CONSTRUCT { ?o ?p2 ?o2 } where { " +
                                " <" + url + "> <http://www.wikidata.org/prop/direct/P131>+ ?o . " +
                                "?o ?p2 ?o2 . " +
                                "FILTER (?p2 = <http://www.w3.org/2000/01/rdf-schema#label> || " +
                                "?p2 = <http://www.w3.org/2004/02/skos/core#altLabel> || " +
                                "?p2 = <http://www.w3.org/2004/02/skos/core#prefLabel>) " +
                                "}";
                        Query queryObjP131 = QueryFactory.create(constructObjP131);
                        QueryExecution qexecObjP131 = QueryExecutionFactory.sparqlService(endpoint, queryObjP131);
                        Model constructModelObjP131 = qexecObjP131.execConstruct();

                        StmtIterator aObjP131 = constructModelObjP131.listStatements();
                        while (aObjP131.hasNext()) {
                            Statement statement = aObjP131.next();
                            if (!alreadyQuery.contains(statement.getSubject().toString())) {
                                alreadyQuery.add(statement.getSubject().toString());
                                writer_complete.triple(statement.asTriple());
                            }
                        }

                        String construct2 = "CONSTRUCT { ?o ?p2 ?o2 } where { " +
                                "  <" + url + "> ?p ?o ." +
                                "  ?o ?p2 ?o2 ." +
                                "  FILTER (?p2 = <http://www.w3.org/2000/01/rdf-schema#label> || " +
                                "?p2 = <http://www.w3.org/2004/02/skos/core#altLabel> || " +
                                "?p2 = <http://www.w3.org/2004/02/skos/core#prefLabel>) " +
                                "}";
                        Query query2 = QueryFactory.create(construct2);
                        QueryExecution qexec2 = QueryExecutionFactory.sparqlService(endpoint, query2);
                        Model constructModel2 = qexec2.execConstruct();
                        
                        StmtIterator a2 = constructModel2.listStatements();
                        while (a2.hasNext()) {
                            Statement statement = a2.next();
                            allStatements.add(statement);
                            //writer_complete.triple(statement.asTriple());
                        }
                        qexec2.close();
                        constructModel2.close();
                    }
                }
            }
            //*
            for (Statement statement : allStatements) {
                writer_complete.triple(statement.asTriple());
            }
            //*/

            writer_complete.finish();
            System.out.println("Finished");
        }
    }

}
