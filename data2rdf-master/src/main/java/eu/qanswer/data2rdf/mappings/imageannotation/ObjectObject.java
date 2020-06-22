package eu.qanswer.data2rdf.mappings.imageannotation;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import eu.qanswer.data2rdf.configuration.CSVConfigurationFile;
import eu.qanswer.data2rdf.configuration.CustomMapping;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;
import eu.qanswer.data2rdf.utility.Utility;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ObjectObject extends CSVConfigurationFile {
    private String qanswerEndPoint = "https://qanswer-core1.univ-st-etienne.fr/";
    private HashMap<String, String> labelQuery = new HashMap<>();
    private HashSet<String> labelAndWdImgAdded = new HashSet<>();

    public ObjectObject() {
        format = "csv";
        separator = ',';
        file = "/home/diazork/FAC/M1/stage/stage/data2rdf/src/main/resources/isCloseTo.csv";
        baseUrl = "http://qanswer.eu/data/datasets/objectPosition/";
        key = "object 1";
        mappings = new ArrayList<>(Arrays.asList(
                new Mapping("is close to", baseUrl + "is_close_to", new Linking(), Type.CUSTOM)
        ));
        triples = new ArrayList<>(Arrays.asList(
                // English
                new Triple(NodeFactory.createURI(baseUrl + "is_close_to"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("is close to", "en")),
                // French
                new Triple(NodeFactory.createURI(baseUrl + "is_close_to"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("est proche de", "fr"))
        ));
    }

    private class Linking extends CustomMapping {

        private Node resolveLabelToURI(String label, Utility utility) {
            if (!(label.equals("na") || label.equals(""))) {
                if (labelQuery.containsKey(label)) {
                    return utility.createURI(labelQuery.get(label));
                }
                OkHttpClient client = new OkHttpClient();
                Node result = null;
                Request request = new Request.Builder()
                        .url(qanswerEndPoint + "api/link?text=" + label
                                + "&language=en"
                                + "&user=open"
                                + "&knowledgebase=wikidata")
                        .get()
                        .build();
                Response response = null;
                boolean hasFail;
                do {
                    hasFail = false;
                    try {
                        response = client.newCall(request).execute();

                    } catch (IOException e) {
                        hasFail = true;
                    }
                } while (hasFail);

                JsonArray obj;
                try {
                    obj = new JsonParser().parse(response.body().string()).getAsJsonArray();
                    if (obj.size() > 0) {
                        result = utility.createURI(obj.get(0).getAsString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                labelQuery.put(label, result.toString());
                return result;
            }
            return null;
        }

        private Node newBlankNode(String source, Utility utility) {
            UUID uuid = null;
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            uuid = UUID.nameUUIDFromBytes(bytes);
            return utility.createURI(baseUrl + "statement/" + uuid);
        }

        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            Node object = null;
            Node predicate = null;
            for (String a : article.keySet()) {
                // adding Reference to image
                if (a.contains("image Name")) {
                    if (!labelAndWdImgAdded.contains(article.get(a))) {
                        labelAndWdImgAdded.add(article.get(a));
                        triples.add(
                                new Triple(
                                        utility.createURI(ObjectObject.baseUrl + article.get(a)),
                                        utility.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                                        utility.createLiteral(article.get(a))
                                )
                        );
                        triples.add(
                                new Triple(
                                        utility.createURI(ObjectObject.baseUrl + article.get(a)),
                                        utility.createURI("http://www.wikidata.org/prop/direct/P31"),
                                        utility.createURI("http://www.wikidata.org/entity/Q478798")
                                )
                        );
                    }
                }
                // adding triple
                if (a.contains("object 1")) {
                    subject = this.resolveLabelToURI(article.get(a), utility);
                    if (article.containsKey(getMapping().getTag())) {
                        if (!(article.get(getMapping().getTag()).equals("na") ||
                                article.get(getMapping().getTag()).equals(""))) {
                            object = this.resolveLabelToURI(article.get(a), utility);
                            predicate = utility.createURI(getMapping().getPropertyUri());
                            triples.add(new Triple(subject, predicate, object));
                            
                            // System.out.println(String.format("<%s> <%s> <%s> .", subject, predicate, object));
                            String[] predicateAddressSplit = getMapping().getPropertyUri().split("/");
                            String predicateName = predicateAddressSplit[predicateAddressSplit.length - 1];
                            String source = subject.toString() + predicate.toString() + object.toString();
                            Node blank = this.newBlankNode(source, utility);
                            triples.add(
                                    new Triple(
                                            subject,
                                            utility.createURI("http://www.wikidata.org/prop/" + predicateName),
                                            blank
                                    )
                            );
                            triples.add(
                                    new Triple(
                                            blank,
                                            utility.createURI("http://www.wikidata.org/prop/statement/" + predicateName),
                                            object
                                    )
                            );
                            // context
                            triples.add(
                                    new Triple(
                                            blank,
                                            utility.createURI("http://www.wikidata.org/prop/qualifier/P361"),
                                            utility.createURI(ObjectObject.baseUrl + article.get("image Name"))
                                    )
                            );
                        }
                    }
                }
            }
            return triples;
        }
    }
}
