package eu.qanswer.data2rdf.mappings.eu;

import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;
import eu.qanswer.data2rdf.configuration.CustomMapping;
import eu.qanswer.data2rdf.utility.Utility;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parking extends JsonConfigurationFile {

    Parking parking = this;

    public Parking() {
        file = "/Users/Dennis/IdeaProjects/semanticscholar/src/main/resources/buildings.json";
        baseUrl = "http://europa.eu/parking/";
        key = "buildingList.buildings.code";
        iterator = "buildingList.buildings";
        mappings = new ArrayList<>(Arrays.asList(
                //cafeteria
                new Mapping("parkingFacility", "http://www.wikidata.org/prop/direct/P31", "http://www.wikidata.org/entity/Q6501349"),
                //cafe
                new Mapping("buildingList.buildings\\[[0-9]+\\].code", "http://www.wikidata.org/prop/direct/P276", new Building(), Type.CUSTOM),
                new Mapping("buildingList.buildings\\[[0-9]+\\].name", "http://www.w3.org/2000/01/rdf-schema#label", new Label(),Type.CUSTOM),
                new Mapping("parkingAddress.gpsCoordinates", "http://www.wikidata.org/prop/direct/P625", new Coordinates(), Type.CUSTOM),
                new Mapping("parkingOpeningHoursList", "http://europa.eu/building/opening", new Buildings.Opening(), Type.CUSTOM),
                new Mapping("parkingFacility.parkingContact", "http://europa.eu/building/contact", Type.LITERAL),
                new Mapping("parkingAddress.streetAddress", "http://www.wikidata.org/prop/direct/P6375", Type.LITERAL)
        ));
    }



    private class Label extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();
            boolean found = false;
            //the building must have a cafeteria
            for (String key : article.keySet()){
                if (key.contains("parkingFacility")){
                    found = true;
                }
            }
            if (found == true) {

                subject = null;
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Parking.baseUrl + article.get(a));
                    }
                }
                Node predicate = utility.createURI(getMapping().getPropertyUri());


                String validPattern = "buildingList.buildings\\[[0-9]+\\].name";
                Pattern pattern = Pattern.compile(validPattern);

                for (String a : article.keySet()) {
                    Matcher matcher = pattern.matcher(a);
                    if (matcher.find()) {
                        //Case for buildings called like B-18
                        String name = article.get(a);
                        Node object = NodeFactory.createLiteral(name.replace("-", " ") + " (Parking)", XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                }


            }

            return triples;
        }
    }

    private class Building extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();
            boolean found = false;
            //the building must have a cafeteria
            for (String key : article.keySet()){
                if (key.contains("parkingFacility")){
                    found = true;
                }
            }
            if (found == true) {

                subject = null;
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Parking.baseUrl + article.get(a));
                    }
                }
                Node predicate = utility.createURI(getMapping().getPropertyUri());


                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        //Case for buildings called like B-18
                        String name = article.get(a);
                        Node object = NodeFactory.createURI("http://europa.eu/building/" + name);
                        triples.add(new Triple(subject, predicate, object));
                    }
                }
            }

            return triples;
        }
    }

    private class Coordinates extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();
            boolean found = false;
            //the building must have a cafeteria
            for (String key : article.keySet()){
                if (key.contains("parkingFacility")){
                    found = true;
                }
            }
            if (found == true) {
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
                    Node object = NodeFactory.createLiteral("Point(" + latitude + " " + longitude + ")", WktLiteral.wktLiteralType);
                    triples.add(new Triple(subject, predicate, object));
                }
            }
            return triples;
        }
    }

    public static class WktLiteral extends BaseDatatype {
        public static final String TypeURI = "http://www.opengis.net/ont/geosparql#wktLiteral";

        public static final String CRS84 = "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>";

        public static final RDFDatatype wktLiteralType = new WktLiteral();

        private WktLiteral() {
            super(WktLiteral.TypeURI);
        }

        /**
         * Convert a value of this datatype out to lexical form.
         */
        public String unparse(Object value) {
            return value.toString();
        }

        /**
         * Parse a lexical form of this datatype to a value
         */
        public Object parse(String lexicalForm) {
            return new TypedValue(String.format("%s %s", WktLiteral.CRS84, lexicalForm), this.getURI());
        }

        /**
         * Compares two instances of values of the given datatype. This does not allow rationals
         * to be compared to other number formats, Lang tag is not significant.
         *
         * @param value1 First value to compare
         * @param value2 Second value to compare
         * @return Value to determine whether both are equal.
         */
        public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
            return value1.getDatatype() == value2.getDatatype()
                    && value1.getValue().equals(value2.getValue());
        }
    }
}



