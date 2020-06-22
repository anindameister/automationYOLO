package eu.qanswer.data2rdf.mappings.imageannotation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.qanswer.data2rdf.configuration.CSVConfigurationFile;
import eu.qanswer.data2rdf.configuration.CustomMapping;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;
import eu.qanswer.data2rdf.utility.Utility;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.io.IOException;
import java.util.*;

public class ObjectPosition extends CSVConfigurationFile {
    private String qanswerEndPoint = "https://qanswer-core1.univ-st-etienne.fr/";
    private HashMap<String, String> labelQuery = new HashMap<>();
    private HashSet<String> labelAndWdImgAdded = new HashSet<>();

    public ObjectPosition() {
        format = "csv";
        separator = ',';
        //file = "/home/diazork/FAC/M1/stage/stage/data2rdf/src/main/resources/imageHasOnLeftRightCenterTopBottom.csv";
        file="F:\\data2rdf-master\\src\\main\\resources\\imageHasOnLeftRightCenterTopBottom.csv";
        baseUrl = "http://qanswer.eu/data/datasets/objectPosition/";
        //key = "Image name";
        key = "url";
        mappings = new ArrayList<>(Arrays.asList(
                new Mapping("has on the left", baseUrl + "has_on_the_left", new Linking(), Type.CUSTOM),
                new Mapping("has on the right", baseUrl + "has_on_the_right", new Linking(), Type.CUSTOM),
                new Mapping("has on the top", baseUrl + "has_on_the_top", new Linking(), Type.CUSTOM),
                new Mapping("has on the bottom", baseUrl + "has_on_the_bottom", new Linking(), Type.CUSTOM),
                new Mapping("has in the center", baseUrl + "has_in_the_center", new Linking(), Type.CUSTOM)
        ));
        triples = new ArrayList<>(Arrays.asList(
                // English
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_left"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("has on the left", "en")),
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_right"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("has on the right", "en")),
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_top"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("has on the top", "en")),
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_bottom"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("has on the bottom", "en")),
                new Triple(NodeFactory.createURI(baseUrl + "has_in_the_center"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("has in the center", "en")),
                new Triple(NodeFactory.createURI(baseUrl + "has_in_the_center"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("has in the middle", "en")),
                // French
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_left"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("a sur la gauche", "fr")),
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_right"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("a sur la droite", "fr")),
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_top"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("a sur le dessus", "fr")),
                new Triple(NodeFactory.createURI(baseUrl + "has_on_the_bottom"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("a sur le dessous", "fr")),
                new Triple(NodeFactory.createURI(baseUrl + "has_in_the_center"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("a au centre", "fr")),
                new Triple(NodeFactory.createURI(baseUrl + "has_in_the_center"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("a au milieu", "fr"))
        ));
    }

    private class Linking extends CustomMapping {

        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            Node object = null;
            for (String a : article.keySet()) {
                //if (a.contains("Image name")) {
                if (a.contains("URLs")) {
                    subject = utility.createURI(article.get(a));
                    if (!labelAndWdImgAdded.contains(subject.toString())) {
                        labelAndWdImgAdded.add(subject.toString());
                        triples.add(
                                new Triple(
                                        subject,
                                        utility.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                                        utility.createLiteral(article.get(a))
                                )
                        );
                        triples.add(
                                new Triple(
                                        subject,
                                        utility.createURI("http://www.wikidata.org/prop/direct/P31"),
                                        utility.createURI("http://www.wikidata.org/entity/Q478798")
                                )
                        );
                    }
                } else if (a.contains(getMapping().getTag())) {
                    if (!(article.get(a).equals("na") || article.get(a).equals(""))) {
                        if (labelQuery.containsKey(article.get(a))) {
                            object = utility.createURI(labelQuery.get(article.get(a)));
                        } else {
                            object = utility.createURI(article.get(a));
                            // Set the correct URI of the object

                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(qanswerEndPoint + "api/link?text=" + article.get(a)
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
                                    object = utility.createURI(obj.get(0).getAsString());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            labelQuery.put(article.get(a), object.toString());
                        }

                        Node predicate = utility.createURI(getMapping().getPropertyUri());
                        triples.add(new Triple(subject, predicate, object));
                    }
                }
            }

            return triples;
        }
    }
}
