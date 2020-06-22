package eu.qanswer.data2rdf.mappings.eu;

import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;
import eu.qanswer.data2rdf.configuration.CustomMapping;
import eu.qanswer.data2rdf.configuration.datatypes.WktLiteral;
import eu.qanswer.data2rdf.utility.Utility;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


//@todo: add city and post code

public class Buildings extends JsonConfigurationFile {

    Buildings buildings = this;

    public Buildings() {
        file = "/Users/Dennis/IdeaProjects/semanticscholar/src/main/resources/buildings.json";
        baseUrl = "http://europa.eu/building/";
        key = "buildingList.buildings.code";
        iterator = "buildingList.buildings";
        mappings = new ArrayList<>(Arrays.asList(
                new Mapping("type", "http://www.wikidata.org/prop/direct/P31", "http://www.wikidata.org/entity/Q41176"),
                new Mapping("buildingList.buildings\\[[0-9]+\\].code", "http://www.w3.org/2004/02/skos/core#altLabel", Type.LITERAL),
                new Mapping("buildingList.buildings\\[[0-9]+\\].code", "http://www.w3.org/2004/02/skos/core#altLabel", new Label(), Type.CUSTOM),
                new Mapping("buildingList.buildings\\[[0-9]+\\].name", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),
                new Mapping("occupants", "http://www.wikidata.org/prop/direct/P466", new Occupant(), Type.CUSTOM),
//                new Mapping("Name", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),
                new Mapping("buildingAddress.streetAddress", "http://www.wikidata.org/prop/direct/P6375", Type.LITERAL),
                new Mapping("postalCode", "http://www.wikidata.org/prop/direct/P281", Type.LITERAL),
                new Mapping("gpsCoordinates", "http://www.wikidata.org/prop/direct/P625", new Coordinates(), Type.CUSTOM),
                new Mapping("photoLink", "http://www.wikidata.org/prop/direct/P18", new Image(), Type.CUSTOM),
                new Mapping("buildingOpeningHoursList", "http://europa.eu/building/opening", new Opening(), Type.CUSTOM),
                new Mapping("contacts", "http://europa.eu/building/contact", new Contact(), Type.CUSTOM)
        ));
        triples = new ArrayList<>(Arrays.asList(
                new Triple(NodeFactory.createURI("http://www.wikidata.org/prop/direct/P625"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("where","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/opening"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("open","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/opening"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("closed","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("contact","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("phone","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("phone number","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("mail","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("e-mail","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("reception","en"))
        ));
    }



    private class Label extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            subject = null;
            for (String a : article.keySet()) {
                if (a.contains("code")) {
                    subject = NodeFactory.createURI(Buildings.baseUrl + article.get(a));
                }
            }
            Node predicate = utility.createURI(getMapping().getPropertyUri());


            for (String a : article.keySet()) {
                if (a.contains("code")) {
                    //Case for buildings called like B-18
                    String name = article.get(a);
                    if (name.matches("[A-Za-z]+-[0-9]+")) {
                        Node object = NodeFactory.createLiteral(name.replace("-", " "), XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                        object = NodeFactory.createLiteral(name.replace("-", ""), XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                    //Case for buildings called like B--18
                    if (name.matches("[A-Za-z]+--[0-9]+")) {
                        Node object = NodeFactory.createLiteral(name.replace("--", " "), XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                        object = NodeFactory.createLiteral(name.replace("--", ""), XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                    //Case for buildings called like B18
                    if (name.matches("[A-Za-z]+[0-9]+")) {
                        Node object = NodeFactory.createLiteral(name.split("[0-9]+")[0]+"-"+name.split("[A-Za-z]+")[1], XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                        object = NodeFactory.createLiteral(name.split("[0-9]+")[0]+" "+name.split("[A-Za-z]+")[1], XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                }
            }

            return triples;
        }
    }

    private class Occupant extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            String occupants[] = {};
            for (String a : article.keySet()) {
                if (a.contains(getMapping().getTag())) {
                    //Case for buildings called like B-18
                    String name = article.get(a);
                    occupants = name.split(", ");

                }
            }

            subject = null;
            for (String a : article.keySet()) {
                if (a.contains("code")) {
                    subject = NodeFactory.createURI(Buildings.baseUrl + article.get(a));
                }
            }
            Node predicate = utility.createURI(getMapping().getPropertyUri());

            for (String o : occupants){
                Node object = utility.createURI(Buildings.baseUrl + o);
                triples.add(new Triple(subject, predicate, object));

                subject = object;
                predicate = utility.createURI("http://www.w3.org/2000/01/rdf-schema#label");
                object = NodeFactory.createLiteral(o);
                triples.add(new Triple(subject, predicate, object));
            }

            return triples;
        }
    }

    private class Coordinates extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            subject = null;
            for (String a : article.keySet()) {
                if (a.contains("code")) {
                    subject = NodeFactory.createURI(Buildings.baseUrl + article.get(a));
                }
            }
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

            return triples;
        }


    }



    private class Image extends CustomMapping {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Utility utility = new Utility();
            ArrayList<Triple> triples = new ArrayList<>();

            subject = null;
            for (String a : article.keySet()) {
                if (a.contains("code")) {
                    subject = NodeFactory.createURI(Buildings.baseUrl + article.get(a));
                    Node predicate = utility.createURI(getMapping().getPropertyUri());
                    Node object = NodeFactory.createURI("https://webgate.ec.europa.eu/where2go/api/buildings/" + article.get(a) + "/photo");
                    triples.add(new Triple(subject, predicate, object));
                }
            }


            return triples;
        }
    }


    static class Opening extends CustomMapping {
        //"openingHours":[{"daysOfWeek":[1,2,3,4,5,8,9,10,11,12],"startTime":"07:00","endTime":"21:00"
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            ArrayList<Triple> triples = new ArrayList<>();
            String startTime = "";
            for (String a : article.keySet()) {
                if (a.contains("startTime")) {
                    startTime = article.get(a);
                }
            }
            String endTime = "";
            for (String a : article.keySet()) {
                if (a.contains("endTime")) {
                    endTime = article.get(a);
                }
            }
            String opening = "";
            for (int i = 0; i < 14; i++) {
            for (String a : article.keySet()) {
                if (a.contains(getMapping().getTag())) {

                        if (a.contains("daysOfWeek[" + i + "]")) {
                            switch (i) {
                                case 1:
                                    opening += "Monday " + startTime + "-" + endTime + "\n";
                                    break;
                                case 3:
                                    opening += "Tuesday " + startTime + "-" + endTime + "\n";
                                    break;
                                case 5:
                                    opening += "Wednesday " + startTime + "-" + endTime + "\n";
                                    break;
                                case 7:
                                    opening += "Thursday " + startTime + "-" + endTime + "\n";
                                    break;
                                case 9:
                                    opening += "Friday " + startTime + "-" + endTime + "\n";
                                    break;
                                case 11:
                                    opening += "Saturday " + startTime + "-" + endTime + "\n";
                                    break;
                                case 13:
                                    opening += "Sunday " + startTime + "-" + endTime + "\n";
                                    break;
                            }
                        }
                    }
                }

            }

//            article.get(hr2.getKey())
            if (!opening.equals("")) {

                Utility utility = new Utility();
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Buildings.baseUrl + article.get(a));
                        Node predicate = utility.createURI(getMapping().getPropertyUri());
                        Node object = NodeFactory.createLiteral(opening, XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                }
            }
//
//            Node object = null;
//            for (String a : article.keySet()){
//                if (a.contains("Occupants")){
//                    article.get(a).split(', ');
//                    System.out.println(article.get(a));
//                    object =  NodeFactory.createLiteral(article.get(a));
//                }
//            }
//
//            if (object!=null) {
//                triples.add(new Triple(subject, predicate, object));
//            }

            return triples;
        }

    }

    private class Contact extends CustomMapping {
        //"openingHours":[{"daysOfWeek":[1,2,3,4,5,8,9,10,11,12],"startTime":"07:00","endTime":"21:00"
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            ArrayList<Triple> triples = new ArrayList<>();
            String contact = "";
            for (int i=0; i<5; i++) {
                for (String a : article.keySet()) {
                    if (a.contains(getMapping().getTag()+"["+i+"]")) {
                        if (a.contains("name")) {
                            contact += article.get(a) + "\n";
                        }
                    }
                }
                for (String a : article.keySet()) {
                    if (a.contains(getMapping().getTag() + "[" + i + "]")) {
                        if (a.contains("email")) {
                            contact += "  E-mail: " + article.get(a) + "\n";
                        }
                    }
                }
                for (String a : article.keySet()) {
                    if (a.contains(getMapping().getTag() + "[" + i + "]")) {
                        if (a.contains("phone")) {
                            contact += "  Phone: " + article.get(a) + "\n\n";
                        }
                    }
                }
            }
            if (!contact.equals("")) {

                Utility utility = new Utility();
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Buildings.baseUrl + article.get(a));
                        Node predicate = utility.createURI(getMapping().getPropertyUri());
                        Node object = NodeFactory.createLiteral(contact, XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                }
            }
            return triples;
        }

    }


}



