package eu.qanswer.data2rdf;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.opencsv.CSVReader;
import eu.qanswer.data2rdf.configuration.*;
import eu.qanswer.data2rdf.mappings.covid.PublicDataDate;
import eu.qanswer.data2rdf.utility.Utility;
import eu.qanswer.linking.Main2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
//    "java -jar target/eu.wdaqua.semanticscholar-1.0-SNAPSHOT-jar-with-dependencies.jar -f eu.qanswer.mapping.mappings.orcId.OrcId,eu.qanswer.mapping.mappings.orcId.OrgStudy,eu.qanswer.mapping.mappings.orcId.OrgWork,eu.qanswer.mapping.mappings.orcId.Publication -o orcid.ttl"


    //private static final String endpoint = "http://qanswer-core1.univ-st-etienne.fr/api/endpoint/wikidata/sparql";
    static final String endpoint = "https://query.wikidata.org/sparql";

    @Parameter(names = {"--mappingClass", "-f"})
    private String mappingClass = null;

    @Parameter(names = {"--file"})
    private String file = null;

    @Parameter(names = {"--outputFilePath", "-o"})
    private String outputFilePath = "test.out";

    @Parameter(names = "--help", help = true)
    private boolean help = false;


    public void run() throws Exception {
        if (help) {
            System.out.println("Tool to convert structured data (json, xml, csv) to RDF");
            System.out.println("1) Write a class defining a mapping like eu.qanswer.data2rdf.mappings.covid.PublicData");
            System.out.println("2) Compile the project");
            System.out.println("3) Run java -jar target/data2rdf-1.0-SNAPSHOT.jar -f name of the class -o output file ");
            System.out.println("    Example: java -jar target/data2rdf-1.0-SNAPSHOT.jar -f eu.qanswer.data2rdf.mappings.covid.PublicData -o covid.ttl");
            return;
        }
        if (mappingClass == null) {
            System.out.println("Specify the --filesArguments argument, or --help if you need more information");
            return;
        } else {
            StreamRDF writer = StreamRDFWriter.getWriterStream(new FileOutputStream(new File(outputFilePath)), RDFFormat.NTRIPLES);
            List<AbstractConfigurationFile> mappingsList = new ArrayList<>();
            String[] files = mappingClass.split(",");
            for (String filePath : files) {
                Class<?> aClass;
                try {
                    aClass = Class.forName(filePath.trim());
                    Constructor<?> ctor = null;
                    AbstractConfigurationFile object = null;
                    if (file != null) {
                        if (aClass == PublicDataDate.class) {
                            HashMap<String, String> placeName = new HashMap<>();
                            String[] datasets = this.file.split(",");
                            ctor = aClass.getConstructor(String.class, HashMap.class);
                            for (String dataset : datasets) {
                                object = (AbstractConfigurationFile) ctor.newInstance(dataset, placeName);
                                mappingsList.add(object);
                            }
                        } else {
                            ctor = aClass.getConstructor(String.class);
                            object = (AbstractConfigurationFile) ctor.newInstance(this.file);
                            mappingsList.add(object);
                        }
                    } else {
                        ctor = aClass.getConstructor();
                        object = (AbstractConfigurationFile) ctor.newInstance();
                        mappingsList.add(object);
                    }
                    //AbstractConfigurationFile object = new HR2();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //extracts the information using the mappings
            HashSet<String> wikidataUrls = new HashSet<>();
            for (AbstractConfigurationFile mappings : mappingsList) {
                //choose the right parser
                if (mappings instanceof JsonConfigurationFile) {
                    parseJson((JsonConfigurationFile) mappings, writer, wikidataUrls);
                } else if (mappings.getFormat().equals("xml")) {
                    parseXML((XmlConfigurationFile) mappings, writer, wikidataUrls);
                } else if (mappings instanceof CSVConfigurationFile) {
                    parseCSV((CSVConfigurationFile) mappings, writer, wikidataUrls);
                } else {
                    System.out.println("This format is not supported. Only json, xml or csv is supported.");
                }
            }
            //extract the wikidata urls from the mappings, download the RDF information and add them to the dump
            StreamRDF writer_ontology = StreamRDFWriter.getWriterStream(new FileOutputStream(new File(outputFilePath + "_ontology")), RDFFormat.NTRIPLES);
            //adding the triples
            for (AbstractConfigurationFile config : mappingsList) {
                for (Triple t : config.getTriples()) {
                    writer.triple(t);
                }
            }
            for (String filePath : files) {
                Class<?> aClass;
                try {
                    aClass = Class.forName(filePath.trim());
                    if (aClass == PublicDataDate.class || aClass == PublicDataDate.class) {
                        Main2.run(Main2.addPrefix("c", outputFilePath), outputFilePath);
                    } else {
                        for (String url : wikidataUrls) {
                            System.out.println("Url " + url);
                            if (url.contains("http://www.wikidata.org/prop/direct/")) {
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
                                    writer_ontology.triple(statement.asTriple());
                                }
                                qexec.close();
                                constructModel.close();
                            }

                            if (url.contains("http://www.wikidata.org/entity/")) {
                                String construct = "CONSTRUCT { <" + url + "> ?p ?o} where { " +
                                        "<" + url + "> ?p ?o " +
                                        "}";
                                Query query = QueryFactory.create(construct);
                                QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                                Model constructModel = qexec.execConstruct();

                                StmtIterator a = constructModel.listStatements();
                                while (a.hasNext()) {
                                    Statement statement = a.next();
                                    writer_ontology.triple(statement.asTriple());
                                }
                                qexec.close();
                                constructModel.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            writer_ontology.finish();
            writer.finish();

            //Main2.run(Main2.addPrefix("c", outputFilePath) , outputFilePath);
            //MainObjectPosition.run("c" + outputFilePath, outputFilePath);
        }
    }


    private void parseXML(XmlConfigurationFile mappings, StreamRDF writer, HashSet<String> wikidataUrls) {
        int nbOfTimes = 0;
        String iterator = mappings.getIterator();
        HashMap<String, String> article = new HashMap<>();
        ArrayList<String> path = new ArrayList<>();
        try {
            Stack<String> stack = new Stack<>();
            String lastElementOnStack = "";
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(mappings.getFile()));
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        stack.push(qName);
                        path.add(qName);
                        if (qName.equals(lastElementOnStack)) {
                            String arrayPath = path.get(path.size() - 2);
                            if (arrayPath.length() > 3) {
                                String number = StringUtils.substringBetween(arrayPath, "[", "]");
                                if (NumberUtils.isNumber(number)) {
                                    int newNbOfTimes = Integer.parseInt(number) + 1;
                                    String newArrayPath = (arrayPath.split("\\[(.*?)\\]"))[0];
                                    newArrayPath = newArrayPath + "[" + newNbOfTimes + "]";
                                    path.remove(path.size() - 2);
                                    path.add(path.size() - 1, newArrayPath);
                                } else {
                                    arrayPath = arrayPath + "[" + nbOfTimes + "]";
                                    path.remove(path.size() - 2);
                                    path.add(path.size() - 1, arrayPath);
                                    nbOfTimes++;
                                }
                            }
                        } else
                            nbOfTimes = 0;
                        String pathString;
                        Iterator attributes = event.asStartElement().getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = (Attribute) attributes.next();
                            path.add(attribute.getName().toString());
                            article.put(splitByPoints(path), attribute.getValue());
                            path.remove(path.size() - 1);
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        Characters characters = event.asCharacters();
                        String data = characters.getData();
                        if (!data.trim().equals("")) {
                            article.put(splitByPoints(path), data.trim());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        event.asEndElement();
                        if (stack.size() != 0)
                            lastElementOnStack = stack.pop();
                        path.remove(path.size() - 1);
                        pathString = splitByPoints(path);
                        if (pathString.replaceAll("\\[(.*?)]", "").replace("$.", "").equals(iterator)) {
                            processMap(article, writer, mappings, wikidataUrls);
                            article = new HashMap<>();
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseJson(JsonConfigurationFile mappings, StreamRDF writer, HashSet<String> wikidataUrls) throws
            Exception {
        String iterator = mappings.getIterator();
        int counterNew = 0;
        int counterOld = -1;
        HashMap<String, String> article = new HashMap<>();
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(mappings.file), "UTF-8"));
        String name = "";
        reader.setLenient(true);

        boolean continuing = true;
        while (continuing) {
            String s;
            JsonToken token = reader.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    reader.beginArray();
                    break;
                case END_ARRAY:
                    reader.endArray();
                    break;
                case BEGIN_OBJECT:
                    if (reader.getPath().replaceAll("\\[(.*?)]", "").replace("$.", "").equals(iterator)) {
                        String[] list = iterator.split("\\.");
                        String arrayString = list[list.length - 1];
                        Pattern pattern = Pattern.compile(arrayString + "\\[(.*?)]");
                        Matcher matcher = pattern.matcher(reader.getPath());
                        if (matcher.find()) {
                            counterNew = Integer.parseInt(matcher.group(1));
                            counterOld = -1;
                        }
                    }
                    reader.beginObject();
                    break;
                case END_OBJECT:
                    reader.endObject();
                    if (reader.getPath().replaceAll("\\[(.*?)]", "").replace("$.", "").equals(iterator) && (counterNew != counterOld || iterator.equals("$"))) {
                        counterOld = counterNew;

                        processMap(article, writer, mappings, wikidataUrls);
                        article = new HashMap<>();
//                        System.out.println("New hash map");
                    }
                    break;
                case NAME:
                    name = reader.nextName();
                    break;
                case STRING:
                case NUMBER:
                    s = reader.nextString();

                    if (!name.equals("") && !s.trim().equals(""))
//                        System.out.println("Hash line "+reader.getPath().replace("$.","")+"---"+s);
                        article.put(reader.getPath().replace("$.", ""), s);

                    break;
                case BOOLEAN:
                    reader.nextBoolean();
                    break;
                case NULL:
                    reader.nextNull();
                    break;
                case END_DOCUMENT:
                    continuing = false;
                    break;
            }
        }
    }

    private void parseCSV(CSVConfigurationFile mappings, StreamRDF writer, HashSet<String> wikidataUrls) throws
            Exception {
        CSVReader reader = new CSVReader(new FileReader(mappings.file), mappings.separator);
        String[] nextLine;
        System.out.println("Reading header of the CSV file ...");
        String[] header = reader.readNext();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i]);
        }
        HashMap<String, String> article = new HashMap<>();
        System.out.println("Reading the CSV file ...");
        while ((nextLine = reader.readNext()) != null) {
            for (int i = 0; i < nextLine.length; i++) {
                if (!nextLine[i].equals("")) {
                    article.put(header[i], nextLine[i]);
                }
            }
            processMap(article, writer, mappings, wikidataUrls);
        }
    }

    public static void main(String[] argv) throws Exception {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(argv);
        main.run();

    }

    private static void processMap(HashMap<String, String> article, StreamRDF
            writer, AbstractConfigurationFile mappings, HashSet<String> wikidataUrls) {
        for (Mapping mapping : mappings.mappings) {
            ArrayList<Triple> triples = getObjects(mapping, mappings, article);
            for (Triple triple : triples) {
                // System.out.println(triple.toString());
                if (triple.getPredicate().toString().contains("http://www.wikidata.org/prop/")) {
                    wikidataUrls.add(triple.getPredicate().toString());
                }
                if (triple.getSubject().toString().contains("http://www.wikidata.org/entity/")) {
                    wikidataUrls.add(triple.getSubject().toString());
                }
                if (triple.getObject().toString().contains("http://www.wikidata.org/entity/")) {
                    wikidataUrls.add(triple.getObject().toString());
                }
                writer.triple(triple);

            }
        }
    }

    private static String splitByPoints(List<String> array) {
        String result = "";
        if (array.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                sb.append(s).append(".");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return result;
    }

    public static Node getSubject(HashMap<String, String> article, AbstractConfigurationFile
            mappings, String key) {
        Utility utility = new Utility();
        String keyWithoutBrackets = key.replaceAll("\\[(.*?)]", "");
        List<String> arrayMappingsKey = new ArrayList<>(Arrays.asList(mappings.getKey().split("\\.")));
        List<String> arrayKeyArticle = new ArrayList<>(Arrays.asList(keyWithoutBrackets.split("\\.")));
        String[] keySplitted = key.replace("$.", "").split("\\.");
        ArrayList<String> finalSubject = new ArrayList<>();
        for (int i = 0; i < arrayMappingsKey.size(); i++) {
            if (i < arrayKeyArticle.size()) {
                if (arrayKeyArticle.get(i).equals(arrayMappingsKey.get(i))) {
                    finalSubject.add(keySplitted[i]);
                } else {
                    finalSubject.add(arrayMappingsKey.get(i));
                }
            } else {
                finalSubject.add(arrayMappingsKey.get(i));
            }
        }
        String uri = mappings.baseUrl + article.get(splitByPoints(finalSubject));
        String[] array = uri.split(" ");
        String newUri = String.join("_", array);
        return (Utility.createURI(newUri));
    }

    private static Node getPredicate(Mapping mapping) {
        Utility utility = new Utility();
        return utility.createURI(mapping.getPropertyUri());
    }

    private static ArrayList<Triple> getObjects(Mapping mapping, AbstractConfigurationFile
            mappings, HashMap<String, String> article) {
        ArrayList<Triple> triples = new ArrayList<>();
        HashMap<String, Pattern> fast = new HashMap<>();
        Node subject, predicate, object;
        Utility utility = new Utility();
        for (String key : article.keySet()) {
            Pattern p;
            if (mappings.format != null && mappings.format.equals("csv")) {
                p = Pattern.compile("^" + mapping.getTag() + "$");
                //p = fast.get("^"+mapping.getTag()+"$");
            } else if (fast.containsKey(mapping.getTag())) {
                p = fast.get(mapping.getTag());
            } else {
                p = Pattern.compile(mapping.getTag());
                fast.put(mapping.getTag(), p);
            }
            Matcher m = p.matcher(key);
            if (m.find()) {
                if (mapping.getType() == null) {
                    subject = getSubject(article, mappings, key);
                    predicate = getPredicate(mapping);
                    object = Utility.createURI(mapping.getObject());
                    triples.add(new Triple(subject, predicate, object));
                } else {
                    switch (mapping.getType()) {
                        case LITERAL:
                            if (mapping.getDatatype() == null) {
                                subject = getSubject(article, mappings, key);
                                predicate = getPredicate(mapping);
                                object = Utility.createLiteral(article.get(key));
                                triples.add(new Triple(subject, predicate, object));
                            } else {
                                subject = getSubject(article, mappings, key);
                                predicate = getPredicate(mapping);
                                object = Utility.createLiteral(article.get(key), mapping.getDatatype());
                                triples.add(new Triple(subject, predicate, object));
                            }
                            break;
                        case URI:
                            if (mapping.getBaseurl() != null) {
                                subject = getSubject(article, mappings, key);
                                predicate = getPredicate(mapping);
                                object = utility.createURI(mapping.getBaseurl() + article.get(key));
                                triples.add(new Triple(subject, predicate, object));
                            } else {
                                if (article.get(key).startsWith("http://")) {
                                    subject = getSubject(article, mappings, key);
                                    predicate = getPredicate(mapping);
                                    object = utility.createURI(article.get(key));
                                    triples.add(new Triple(subject, predicate, object));
                                } else {
                                    subject = getSubject(article, mappings, key);
                                    predicate = getPredicate(mapping);
                                    object = utility.createURI(mappings.getBaseUrl() + article.get(key));
                                    triples.add(new Triple(subject, predicate, object));
                                }
                            }
                            break;
                        case URI_WITH_LABEL:

                            if (mapping.getBaseurl() != null) {

                                subject = getSubject(article, mappings, key);
                                predicate = getPredicate(mapping);
                                object = utility.createURI(mapping.getBaseurl() + article.get(key));
                                triples.add(new Triple(subject, predicate, object));

                                subject = object;
                                predicate = utility.createURI("http://www.w3.org/2000/01/rdf-schema#label");
                                object = NodeFactory.createLiteral(article.get(key));
                                triples.add(new Triple(subject, predicate, object));
                            } else {
                                subject = getSubject(article, mappings, key);
                                predicate = getPredicate(mapping);
                                object = utility.createURI(mappings.getBaseUrl() + article.get(key));
                                triples.add(new Triple(subject, predicate, object));

                                subject = object;
                                predicate = utility.createURI("http://www.w3.org/2000/01/rdf-schema#label");
                                object = NodeFactory.createLiteral(article.get(key));
                                triples.add(new Triple(subject, predicate, object));
                            }
                            break;
                        case CLASS:
                            if (mapping.getObject() != null) {
                                subject = getSubject(article, mappings, key);
                                predicate = getPredicate(mapping);
                                object = utility.createURI(mapping.getObject());
                                triples.add(new Triple(subject, predicate, object));
                            }
                            break;
                        case CUSTOM:
                            if (mapping.getCustomMapping() == null) {
                                try {
                                    throw new Exception("This custom mapping ha no associated class " + mapping.getCustomMapping());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                triples.addAll(mapping.getCustomMapping().function(getSubject(article, mappings, key), article));
                            }
                            break;
                    }
                }
            }
        }
        return triples;
    }
}

