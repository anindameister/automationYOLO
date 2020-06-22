package eu.qanswer.data2rdf.mappings.covid;

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
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class PublicData extends CSVConfigurationFile {

    public PublicData(String filepath) {
        format = "csv";
        separator = ',';
        file = filepath;

        baseUrl = "http://qanswer.eu/kovid19/";
        key = "Combined_Key";
        mappings = new ArrayList<>(Arrays.asList(
                new Mapping("Confirmed", "http://www.wikidata.org/prop/direct/P1603", Type.LITERAL, XSDDatatype.XSDinteger),    // http://www.wikidata.org/prop/direct/P1674
                new Mapping("Deaths", "http://www.wikidata.org/prop/direct/P1120", Type.LITERAL, XSDDatatype.XSDinteger), //P1590
                new Mapping("Recovered", "http://www.wikidata.org/prop/direct/P8010", Type.LITERAL, XSDDatatype.XSDinteger),
                new Mapping("Combined_Key", "http://www.w3.org/2002/07/owl#sameAs", new Linking(), Type.CUSTOM)
        ));
        triples = new ArrayList<>(/*Arrays.asList(
                new Triple(NodeFactory.createURI("http://www.wikidata.org/prop/direct/P1603"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("confirmed case", "en")),
                new Triple(NodeFactory.createURI("http://www.wikidata.org/prop/direct/P1120"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("death", "en")),
                new Triple(NodeFactory.createURI("http://www.wikidata.org/prop/direct/P8010"),
                        NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                        NodeFactory.createLiteral("recovered", "en"))
                )//*/);
    }

    public PublicData() {
        this("/home/diazork/FAC/M1/stage/stage/data2rdf/src/main/resources/04-25-2020.csv");
    }

    private class Linking extends CustomMapping {

        static final String nominatimEndpoint = "http://localhost:5000/https://nominatim.openstreetmap.org";

        private JsonObject maxJsonObject(JsonArray jsonArray) {
            if (jsonArray.size() > 0) {
                JsonObject maxObj = jsonArray.get(0).getAsJsonObject();
                for (Iterator ite = jsonArray.iterator(); ite.hasNext(); ) {
                    JsonObject obj = ((JsonObject) ite.next()).getAsJsonObject();
                    if (obj.get("importance").getAsDouble() > maxObj.get("importance").getAsDouble()) {
                        maxObj = obj;
                    }
                }
                return maxObj;
            }
            return null;
        }

        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            Node object = null;
            String location = null;
            for (String a : article.keySet()) {
                if (a.contains("Combined_Key")) {
                    location = article.get(a);
                    object = utility.createURI(PublicData.baseUrl + article.get(a));
                }
            }
            Node predicate = utility.createURI(getMapping().getPropertyUri());

            //location
            String wikidataURI = "";

            if (location.contains("Unassigned, ")) {
                location = location.replace("Unassigned, ", "");
            }
            // Get OpenStreetMap ID
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(nominatimEndpoint + "/search?format=json&limit=1&q=" + location)
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
                /*
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //*/
            } while (hasFail);
            JsonArray jsonArray = null;
            try {
                jsonArray = new JsonParser().parse(response.body().string()).getAsJsonArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonObject locationObject = maxJsonObject(jsonArray);
            if (locationObject != null) {
                // Get Wikidata URI
                //*
                request = new Request.Builder()
                        .url(nominatimEndpoint + "/details?format=json&place_id="
                                + (locationObject.get("place_id").getAsString())
                        )
                        .get()
                        .build();
                do {
                    hasFail = false;
                    try {
                        response = client.newCall(request).execute();

                    } catch (IOException e) {
                        hasFail = true;
                    }
                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //*/
                } while (hasFail);

                JsonObject obj = null;
                try {
                    obj = new JsonParser().parse(response.body().string()).getAsJsonObject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (obj.get("extratags").isJsonObject()) {
                        if (obj.get("extratags").getAsJsonObject().has("wikidata")) {
                            wikidataURI = "http://www.wikidata.org/entity/" +
                                    obj.get("extratags").getAsJsonObject().get("wikidata").getAsString();
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            subject = utility.createURI(wikidataURI);
            if (!wikidataURI.equals("")) {
                triples.add(new Triple(subject, predicate, object));
            } else {
                System.out.println("\"" + location + "\", "
                        + article.get("Confirmed")
                        + ", " + article.get("Deaths")
                        + ", " + article.get("Recovered"));
            }
            return triples;
        }
    }
}
