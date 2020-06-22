package eu.qanswer.data2rdf.mappings.informa;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Refactor {

//    public static void main(String[] args) throws IOException {
//        try {
//            ////Adding to the trial file the links to wikidata deaseses
//            StreamRDF writer = StreamRDFWriter.getWriterStream(new FileOutputStream(new File("/Users/Dennis/IdeaProjects/semanticscholar/trials_links.ttl")), org.apache.jena.riot.RDFFormat.NTRIPLES);
//            //retrive from wikidata all mesh ID - wikidata url pairs
//            //key: desease, value: meshID
//            QueryEngineHTTP qExe = new QueryEngineHTTP("https://query.wikidata.org/sparql", "SELECT * WHERE {?value <http://www.wikidata.org/prop/direct/P486> ?key}");
//            qExe.addParam("format", "json");
//            ResultSet result = qExe.execSelect();
//
//            HashMap<String, String> hashMap = new HashMap<>();
//            int duplicates = 0;
//            int total = 0;
//            while (result.hasNext()) {
//                total++;
//                QuerySolution next = result.next();
//                String key = "", value = "";
//                if (next.get("key").toString() != null)
//                    key = next.get("key").toString();
//                if (next.get("value").toString() != null)
//                    value = next.get("value").toString();
//                if (hashMap.containsKey(key)) {
//                    duplicates = duplicates + 1;
//                }
//                hashMap.put(key, value);
//            }
//            System.out.println("duplicates " + duplicates);
//
//            //compress to hdt
//            HDT hdt = HDTManager.generateHDT("/Users/Dennis/IdeaProjects/semanticscholar/trials.ttl", "file://", RDFNotation.TURTLE, new HDTSpecification(), null);
//            hdt.saveToHDT("/Users/Dennis/IdeaProjects/semanticscholar/trials.hdt", null);
//
//
//            //generate triples pointing to wikidata deases urls
//            IteratorTripleString iteratorTripleString = hdt.search("", "http://www.wikidata.org/prop/direct/P486", "");
//            while (iteratorTripleString.hasNext()) {
//                TripleString next = iteratorTripleString.next();
//                Node subject = NodeFactory.createURI(next.getSubject().toString());
//                Node predicate = NodeFactory.createURI("http://www.wikidata.org/prop/direct/P1050");
//                if (hashMap.containsKey(next.getObject().toString().replace("\"", ""))) {
//                    Node object = NodeFactory.createURI(hashMap.get(next.getObject().toString().replace("\"", "")));
//                    Triple triple = new Triple(subject, predicate, object);
//                    writer.triple(triple);
//                } else {
//                    System.out.println("Not found " + next.getObject().toString());
//                }
//
//
//            }
//            writer.finish();
//
//
////            //cat together trials, trials links and organization
////            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("/Users/Dennis/IdeaProjects/semanticscholar/trials_organizations_links.ttl"));
////            RDFWriter writer3 = Rio.createWriter(RDFFormat.NTRIPLES, out);
////            writer3.startRDF();
//            RDFFormat format = RDFFormat.TURTLE;
////            GraphQueryResult res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/trials.ttl"), "", format);
////            while (res.hasNext()) {
////                Statement statement = res.next();
////                writer3.handleStatement(statement);
////            }
////            res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/organization.ttl"), "", format);
////            while (res.hasNext()) {
////                Statement statement = res.next();
////                writer3.handleStatement(statement);
////            }
////            res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/trial_links.ttl"), "", format);
////            while (res.hasNext()) {
////                Statement statement = res.next();
////                writer3.handleStatement(statement);
////            }
////            writer3.endRDF();
////
//            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("/Users/Dennis/IdeaProjects/semanticscholar/trials_refactor.ttl"));
//            RDFWriter writer4 = Rio.createWriter(RDFFormat.NTRIPLES, out);
//            writer4.startRDF();
//////
//////            //compress to hdt
//
//            Repository db = new HDTRepository(new File("/Users/Dennis/IdeaProjects/semanticscholar/trials.hdt"));
//            db.initialize();
//            RepositoryConnection conn = db.getConnection();
////
//            //take only open trials
//            String construct = "construct {" +
//                    " ?s ?p ?o" +
//                    " } where { " +
//                    " ?s ?p ?o . " +
//                    //" ?s <http://www.newclean.eu/status> <https://citeline.informa.com/trials/details/Open> . " + // taken only open trials
//                    "}";
//            GraphQuery graphQuery = conn.prepareGraphQuery(construct);
//            GraphQueryResult graphQueryResult = graphQuery.evaluate();
//
//            while (graphQueryResult.hasNext()) {
//                Statement statement = graphQueryResult.next();
//                writer4.handleStatement(statement);
//            }
//
//            GraphQueryResult res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/trials_links.ttl"), "", format);
//            while (res.hasNext()) {
//                Statement statement = res.next();
//                writer4.handleStatement(statement);
//            }
////
////            //labels of missing url_with_label: countries
////            System.out.println("labels of missing url_with_label: countries");
////            construct = "construct {" +
////                    "    ?o <http://www.w3.org/2000/01/rdf-schema#label> ?label" +
////                    " } where { " +
////                    "   ?trial <http://www.wikidata.org/prop/direct/P17> ?o . " +
////                    "   ?o <http://www.w3.org/2000/01/rdf-schema#label> ?label . " +
////                    "}";
////            graphQuery = conn.prepareGraphQuery(construct);
////            graphQueryResult = graphQuery.evaluate();
////            while (graphQueryResult.hasNext()) {
////                Statement statement = graphQueryResult.next();
////                writer4.handleStatement(statement);
////            }
////
////            //labels of missing url_with_label: status
////            System.out.println("labels of missing url_with_label: status");
////            construct = "construct {" +
////                    "    ?o <http://www.w3.org/2000/01/rdf-schema#label> ?label" +
////                    " } where { " +
////                    "   ?trial <http://www.wikidata.org/prop/direct/P17> ?o . " +
////                    "   ?o <http://www.w3.org/2000/01/rdf-schema#label> ?label . " +
////                    "}";
////            graphQuery = conn.prepareGraphQuery(construct);
////            graphQueryResult = graphQuery.evaluate();
////            while (graphQueryResult.hasNext()) {
////                Statement statement = graphQueryResult.next();
////                writer4.handleStatement(statement);
////            }
//
//            //concatenate ontology trials
//            format = RDFFormat.TURTLE;
//            res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/trials.ttl_ontology"), "", format);
//            while (res.hasNext()) {
//                Statement statement = res.next();
//                writer4.handleStatement(statement);
//            }
//
//            //get from wikidata all labels of diseases with meshID
//            BufferedWriter writer2 = new BufferedWriter(new FileWriter("/Users/Dennis/IdeaProjects/semanticscholar/desease.txt", false));
//            construct = "CONSTRUCT { ?s ?p ?o} where { " +
//                    "?s ?p ?o . " +
//                    "FILTER (?p = <http://www.w3.org/2004/02/skos/core#altLabel> || ?p = <http://www.w3.org/2000/01/rdf-schema#label>  ) ." +
//                    "?s ?p1 <http://www.wikidata.org/entity/Q12136> . " +
////                    "?s <http://www.wikidata.org/prop/direct/P486> ?o1 . " +
//                    "}";
//            SPARQLRepository repo = new SPARQLRepository("https://query.wikidata.org/sparql");
//            RepositoryConnection repositoryConnection = repo.getConnection();
//            GraphQuery query = repositoryConnection.prepareGraphQuery(construct);
//            GraphQueryResult rs = query.evaluate();
//            while (rs.hasNext()) {
//                Statement statement = rs.next();
//                writer4.handleStatement(statement);
//                writer2.append(statement.getObject().stringValue() + "\n");
//            }
//
//            //get all wikipedia links of deseases
//            construct = "CONSTRUCT { " +
//                    " ?wikipedia <http://schema.org/about> ?s ." +
//                    " ?wikipedia <http://schema.org/inLanguage> ?language ." +
//                    " ?wikipedia <http://schema.org/isPartOf> ?link . "+
//                    "} where { " +
//                    "   ?s ?p1 <http://www.wikidata.org/entity/Q12136> . " +
//                    "   OPTIONAL{ " +
//                    "      ?wikipedia <http://schema.org/about> ?s ; <http://schema.org/inLanguage> ?language ; <http://schema.org/isPartOf> ?link . " +
//                    "   } "+
//                    "}";
//            repo = new SPARQLRepository("https://query.wikidata.org/sparql");
//            repositoryConnection = repo.getConnection();
//            query = repositoryConnection.prepareGraphQuery(construct);
//            rs = query.evaluate();
//            while (rs.hasNext()) {
//                Statement statement = rs.next();
//                writer4.handleStatement(statement);
//            }
//
//            //get from wikidata all labels of subclass diseases with meshID
//            // this is to cover the case diabetes type II subclass of diabetes
//            construct = "CONSTRUCT { ?s1 <http://www.w3.org/2004/02/skos/core#altLabel> ?o} where { " +
//                    "?s ?p ?o . " +
//                    "FILTER (?p = <http://www.w3.org/2004/02/skos/core#altLabel> || ?p = <http://www.w3.org/2000/01/rdf-schema#label>  ) ." +
//                    "?s ?p1 <http://www.wikidata.org/entity/Q12136> . " +
//                    //diabetes type II subclass of diabetes
//                    "?s1 <http://www.wikidata.org/prop/direct/P279> ?s . " +
//                    "?s1 <http://www.wikidata.org/prop/direct/P486> ?o1 . " +
//                    "}";
//            System.out.println(construct);
//            repo = new SPARQLRepository("https://query.wikidata.org/sparql");
//            repositoryConnection = repo.getConnection();
//            query = repositoryConnection.prepareGraphQuery(construct);
//            rs = query.evaluate();
//            while (rs.hasNext()) {
//                Statement statement = rs.next();
//                writer4.handleStatement(statement);
//                writer2.append(statement.getObject().stringValue() + "\n");
//            }
//
//            //get from wikidata all labels of subclass diseases with meshID
//            // this is to cover the case breast cancer subclass of breast neoplasm
//            construct = "CONSTRUCT { ?s <http://www.w3.org/2004/02/skos/core#altLabel> ?o} " +
//                    "where { " +
//                    "?s1 ?p ?o . " +
//                    "FILTER (?p = <http://www.w3.org/2004/02/skos/core#altLabel> || ?p = <http://www.w3.org/2000/01/rdf-schema#label>  ) ." +
//                    "?s1 ?p1 <http://www.wikidata.org/entity/Q12136> . " +
//                    //breast cancer subclass of breast neoplasm
//                    "?s1 <http://www.wikidata.org/prop/direct/P279> ?s . " +
//                    " FILTER (NOT EXISTS {?s1 <http://www.wikidata.org/prop/direct/P486> ?o1 }) " +
//                    "}";
//            System.out.println(construct);
//            repo = new SPARQLRepository("https://query.wikidata.org/sparql");
//            repositoryConnection = repo.getConnection();
//            query = repositoryConnection.prepareGraphQuery(construct);
//            rs = query.evaluate();
//            while (rs.hasNext()) {
//                Statement statement = rs.next();
//                writer4.handleStatement(statement);
//                writer2.append(statement.getObject().stringValue() + "\n");
//            }
//            writer2.close();
//
//
////            //get from wikidata all wikipedia links of diseases with meshID
////            construct = "CONSTRUCT { " +
////                    "?link <http://schema.org/about> ?s . " +
////                    "?s <http://schema.org/inLanguage> ?language . " +
////                    "} where { " +
////                    "?link <http://schema.org/about> ?s . " +
////                    "?s <http://schema.org/inLanguage> ?language . " +
////                    "?s ?p1 <http://www.wikidata.org/entity/Q12136> . " +
////                    "?s <http://www.wikidata.org/prop/direct/P486> ?o1 . " +
////                    "}";
////            query = repositoryConnection.prepareGraphQuery(construct);
////            rs = query.evaluate();
////            while (rs.hasNext()) {
////                Statement statement = rs.next();
////                writer4.handleStatement(statement);
////            }
////
////
////            //attach the geocoordinate of the organization
////            construct = "construct {" +
////                    " ?url  <http://www.wikidata.org/prop/direct/P625> ?o" +
////                    " } where { " +
////                    " ?org  <http://www.wikidata.org/prop/direct/P625> ?o . " +
////                    " FILTER (?p != <http://purl.org/dc/terms/identifier> && ?p != <http://www.wikidata.org/prop/direct/P137>) ." +
////                    " ?org <http://purl.org/dc/terms/identifier> ?id_org . " +
////                    " ?org <http://www.wikidata.org/prop/direct/P137> ?trial . " +
////                    " ?trial <http://purl.org/dc/terms/identifier> ?id_trial . " +
////                    " ?trial <http://www.newclean.eu/status> <https://citeline.informa.com/trials/details/Open> . " + // taken only open trials
////                    " BIND ( IRI(CONCAT(\"http://newclin.com/trial-organization/\",str(?id_trial),\"-\",str(?id_org))) as ?url ) . " +
////                    "}";
////            graphQuery = conn.prepareGraphQuery(construct);
////            graphQueryResult = graphQuery.evaluate();
////            while (graphQueryResult.hasNext()) {
////                Statement statement = graphQueryResult.next();
////                writer4.handleStatement(statement);
////            }
//
//            //attach the organization
//            hdt = HDTManager.generateHDT("/Users/Dennis/IdeaProjects/semanticscholar/organizations.ttl","file://", RDFNotation.TURTLE,new HDTSpecification(),null);
//            hdt.saveToHDT("/Users/Dennis/IdeaProjects/semanticscholar/organizations.hdt",null);
//            db = new HDTRepository(new File("/Users/Dennis/IdeaProjects/semanticscholar/organizations.hdt"));
//            db.initialize();
//            conn = db.getConnection();
//            construct = "construct {" +
//                    " ?trial  <http://www.wikidata.org/prop/direct/P137> ?org" +
//                    " } where { " +
//                    " ?org <http://www.wikidata.org/prop/direct/P137> ?trial . " +
//
//
////                    " ?org <http://purl.org/dc/terms/identifier> ?id_org . " +
////                    " ?org <http://www.wikidata.org/prop/direct/P137> ?trial . " +
////                    " ?trial <http://purl.org/dc/terms/identifier> ?id_trial . " +
////                    " ?trial <http://www.newclean.eu/status> <https://citeline.informa.com/trials/details/Open> . " + // taken only open trials
////                    " BIND ( IRI(CONCAT(\"http://newclin.com/trial-organization/\",str(?id_trial),\"-\",str(?id_org))) as ?url ) . " +
//                    "}";
//            graphQuery = conn.prepareGraphQuery(construct);
//            graphQueryResult = graphQuery.evaluate();
//
//            while (graphQueryResult.hasNext()) {
//                Statement statement = graphQueryResult.next();
//                writer4.handleStatement(statement);
//            }
//
//            //put all information about the organizations
//            res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/organizations.ttl_ontology"), "", format);
//            while (res.hasNext()) {
//                Statement statement = res.next();
//                writer4.handleStatement(statement);
//            }
//            res = QueryResults.parseGraphBackground(new FileInputStream("/Users/Dennis/IdeaProjects/semanticscholar/organizations.ttl"), "", format);
//            while (res.hasNext()) {
//                Statement statement = res.next();
//                writer4.handleStatement(statement);
//            }
//            writer4.endRDF();
//        } catch (RDFHandlerException e) {
//            e.printStackTrace();
//        } catch (QueryEvaluationException e) {
//            e.printStackTrace();
//        } catch (ParserException e) {
//            e.printStackTrace();
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        }

//    }

}
