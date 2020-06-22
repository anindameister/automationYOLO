package eu.qanswer.data2rdf.mappings.informa;

import eu.qanswer.data2rdf.configuration.CustomMapping;
import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;
import eu.qanswer.data2rdf.configuration.datatypes.WktLiteral;
import eu.qanswer.data2rdf.utility.Utility;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Organization extends JsonConfigurationFile {

    public Organization(){
        file = "/Users/Dennis/PycharmProjects/TrialTrove/crawl_organizations";
        baseUrl = "https://citeline.informa.com/organizations/details/";
        key = "organizationId";
        iterator = "$";
        mappings = new ArrayList<Mapping>(Arrays.asList(
                new Mapping("organizationName", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),
                new Mapping("organizationId", "http://purl.org/dc/terms/identifier", Type.LITERAL),
                new Mapping("organizationId", "http://www.w3.org/2000/01/rdf-schema#type", "http://www.wikidata.org/entity/Q43229"),
//                new Mapping("organizationType", "http://www.w3.org/2000/01/rdf-schema#type", Type.URI_WITH_LABEL),
                new Mapping("organizationPhoneNumbers", "http://www.wikidata.org/prop/direct/P1329", Type.LITERAL),
//                new Mapping("organizationFaxNumbers", "http://www.wikidata.org/prop/direct/P2900", Type.LITERAL),
                new Mapping("organizationGeoLocation.latitude", "http://www.wikidata.org/prop/direct/P625", new Coordinates(), Type.CUSTOM),
                new Mapping("organizationTrials.*id", "http://www.wikidata.org/prop/direct/P137", "https://citeline.informa.com/trials/details/", Type.URI),
//                new Mapping("organizationSupportingUrls","http://www.newclean.eu/source", Type.LITERAL),
                new Mapping("state","http://www.newclean.eu/state","http://www.newclean.eu/state/", Type.URI_WITH_LABEL),
                new Mapping("city","http://www.newclean.eu/city","http://www.newclean.eu/city/", Type.URI_WITH_LABEL),
                new Mapping("country","http://www.newclean.eu/country","http://www.newclean.eu/country/", Type.URI_WITH_LABEL),
                new Mapping("country","http://www.wikidata.org/prop/direct/P6375", new Address(), Type.CUSTOM)
    ));
    }


    private class Coordinates extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            Node predicate = utility.createURI(getMapping().getPropertyUri());

            String latitude = null;
            String longitude = null;
            for (String a : article.keySet()) {
                if (a.contains("latitude")) {
                    latitude = article.get(a);
                }
                if (a.contains("longitude")) {
                    longitude = article.get(a);
                }
            }
            if (latitude != null && longitude != null) {
                TypeMapper.getInstance().registerDatatype(WktLiteral.wktLiteralType);
                Node object = NodeFactory.createLiteral("Point(" + longitude + " " + latitude + ")", WktLiteral.wktLiteralType);
                triples.add(new Triple(subject, predicate, object));
            }

            return triples;
        }
    }

    private class Address extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            Node predicate = utility.createURI(getMapping().getPropertyUri());

//            "organizationLocation":{
//                "streetAddress":"Exohi",
//                        "city":"Thessaloniki",
//                        "state":"Thessaloniki",
//                        "postCode":"570 10",
//                        "country":"Greece"
//            },
            String address = "";
            for (String a : article.keySet()) {
                if (a.contains("streetAddress")) {
                    address += article.get(a) + "\n";
                }
            }
            for (String a : article.keySet()) {
                if (a.contains("postCode")) {
                    address += article.get(a)+" ";
                }
            }
            for (String a : article.keySet()) {
                if (a.contains("city")) {
                    address += article.get(a) + "\n";
                }
            }
            for (String a : article.keySet()) {
                if (a.contains("state")) {
                    address += article.get(a) + " ";
                }
            }
            for (String a : article.keySet()) {
                if (a.contains("country")) {
                    address += article.get(a) + "\n";
                }
            }
            Node object = NodeFactory.createLiteral(address);
            triples.add(new Triple(subject, predicate, object));
            return triples;
        }
    }


}
