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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PublicDataDate extends CSVConfigurationFile {

    private String date;
    private HashMap<String, String> placeName;

    public PublicDataDate() throws ParseException {
        this("/home/diazork/FAC/M1/stage/stage/data2rdf/src/main/resources/04-25-2020.csv", null);
    }


    public PublicDataDate(String filePath, HashMap<String, String> placeName) throws ParseException {
        this(filePath, placeName, getFileDate(filePath));
    }

    private PublicDataDate(String filePath, HashMap<String, String> placeName, String fileDate) {
        format = "csv";
        separator = ',';
        file = filePath;
        date = fileDate;
        this.placeName = placeName;

        baseUrl = "http://qanswer.eu/data/datasets/kovid19/";
        key = "Combined_Key";
        mappings = new ArrayList<>(Arrays.asList(
                new Mapping("Confirmed", "http://www.wikidata.org/prop/direct/P1603", new LinkingNumber(), Type.CUSTOM),    // http://www.wikidata.org/prop/direct/P1674
                new Mapping("Deaths", "http://www.wikidata.org/prop/direct/P1120", new LinkingNumber(), Type.CUSTOM), //http://www.wikidata.org/prop/direct/P1120
                new Mapping("Recovered", "http://www.wikidata.org/prop/direct/P8010", new LinkingNumber(), Type.CUSTOM),
                new Mapping("Combined_Key", "http://www.w3.org/2002/07/owl#sameAs", new LinkingLocation(), Type.CUSTOM) // "http://www.wikidata.org/prop/direct/P276"
        ));
        triples = new ArrayList<>();
    }

    private class LinkingNumber extends CustomMapping {

        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            Node object = null;
            for (String a : article.keySet()) {
                if (a.contains(getMapping().getTag())) {
                    object = utility.createLiteral(article.get(a), XSDDatatype.XSDinteger);
                }
            }
            String[] predicateAddressSplit = getMapping().getPropertyUri().split("/");
            String predicateName = predicateAddressSplit[predicateAddressSplit.length - 1];
            String source = date +
                    subject.toString() +
                    getMapping().getPropertyUri() +
                    object;
            UUID uuid = null;
            try {
                byte[] bytes = source.getBytes("UTF-8");
                uuid = UUID.nameUUIDFromBytes(bytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Node blank = utility.createURI(baseUrl + "statement/" + uuid);
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
            triples.add(
                    new Triple(
                            blank,
                            utility.createURI("http://www.wikidata.org/prop/qualifier/P585"),
                            utility.createLiteral(date, XSDDatatype.XSDdate)
                    )
            );
            triples.add(
                    new Triple(
                            blank,
                            utility.createURI("http://www.wikidata.org/prop/qualifier/P828"),
                            utility.createURI("http://www.wikidata.org/entity/Q81068910")
                    )
            );

            // triples.add(new Triple(subject, predicate, object));

            return triples;
        }
    }

    private class LinkingLocation extends CustomMapping {


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

                    object = utility.createURI(PublicDataDate.baseUrl + article.get(a));
                }
            }
            Node predicate = utility.createURI(getMapping().getPropertyUri());

            //location
            String wikidataURI = "";
            if (placeName.containsKey(location)) {
                if (placeName.get(location) != null) {
                    wikidataURI = placeName.get(location);
                }
            } else {
                OkHttpClient client = new OkHttpClient();

                // Get OpenStreetMap ID
                Request request = null;

                request = new Request.Builder()
                        .url(nominatimEndpoint + "/search?format=json&q=" + location)
                        .get()
                        .build();

                Response response = null;
                boolean hasFail;
                int nTry1 = 0;
                int nTry2 = 0;

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
                    nTry1++;
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
                        nTry2++;
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
                                wikidataURI = "http://www.wikidata.org/entity/" + obj.get("extratags").getAsJsonObject().get("wikidata").getAsString();
                            }
                        }
                    } catch (IllegalStateException e) {
                        System.err.println(obj);
                        e.printStackTrace();
                    }
                }
            }
            placeName.put(location, wikidataURI);
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


    private static String getFileDate(String filePath) throws ParseException {
        String[] filePathName = filePath.split("/");
        String fileName = filePathName[filePathName.length - 1];
        System.out.println(fileName);
        String date = fileName.split("\\.")[0];

        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new SimpleDateFormat("MM-dd-yyyy").parse(date));
    }
}
