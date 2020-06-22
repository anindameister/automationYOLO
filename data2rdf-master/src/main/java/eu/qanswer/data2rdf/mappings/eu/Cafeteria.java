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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cafeteria extends JsonConfigurationFile {

    Cafeteria cafeteria = this;

    public Cafeteria() {
        file = "/Users/Dennis/IdeaProjects/semanticscholar/src/main/resources/buildings.json";
        baseUrl = "http://europa.eu/cafeteria/";
        key = "buildingList.buildings.code";
        iterator = "buildingList.buildings";
        mappings = new ArrayList<>(Arrays.asList(
                //cafeteria
                new Mapping("cafeteriaFacility", "http://www.wikidata.org/prop/direct/P31", "http://www.wikidata.org/entity/Q8463304"),
                //cafe
                new Mapping("cafeteriaFacility", "http://www.wikidata.org/prop/direct/P31", "http://www.wikidata.org/entity/Q30022"),
                new Mapping("cafeteriaFacility", "http://www.wikidata.org/prop/direct/P31", "http://www.wikidata.org/entity/Q54957790"),
                new Mapping("buildingList.buildings\\[[0-9]+\\].code", "http://www.wikidata.org/prop/direct/P276", new Building(), Type.CUSTOM),
                new Mapping("buildingList.buildings\\[[0-9]+\\].name", "http://www.w3.org/2000/01/rdf-schema#label", new Label(),Type.CUSTOM),
                new Mapping("cafeteriaFacility.contractor", "http://europa.eu/building/contractor", Type.LITERAL),
                new Mapping("gpsCoordinates", "http://www.wikidata.org/prop/direct/P625", new Coordinates(), Type.CUSTOM),
                new Mapping("cafeteriaOpeningHoursList", "http://europa.eu/building/opening", new Buildings.Opening(), Type.CUSTOM),
                new Mapping("canteenMenu", "http://europa.eu/building/menu", new Menu(), Type.CUSTOM),
                new Mapping("soup$", "http://europa.eu/building/soup", Type.LITERAL),
                new Mapping("vegetarian$", "http://europa.eu/building/vegetarian", Type.LITERAL),
                new Mapping("starter$", "http://europa.eu/building/starter", Type.LITERAL),
                new Mapping("meal1$", "http://europa.eu/building/mainDish", Type.LITERAL),
                new Mapping("meal2$", "http://europa.eu/building/mainDish", Type.LITERAL)
        ));
        triples = new ArrayList<>(Arrays.asList(
                new Triple(NodeFactory.createURI("http://www.wikidata.org/entity/Q8463304"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#altLabel"),NodeFactory.createLiteral("cafet","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/opening"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("open","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/opening"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("closed","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("contact","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("phone","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("phone number","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("mail","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("e-mail","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/contact"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("reception","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/menu"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("menu","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/menu"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("eat","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/menu"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("food","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/soup"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("soup","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/vegetarian"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("vegetarian","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/vegetarian"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("veggy","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/starter"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("starter","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/mainDish"),NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),NodeFactory.createLiteral("main dish","en")),
                new Triple(NodeFactory.createURI("http://europa.eu/building/mainDish"),NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),NodeFactory.createLiteral("main course","en"))
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
                if (key.contains("cafeteriaFacility")){
                    found = true;
                }
            }
            if (found == true) {

                subject = null;
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Cafeteria.baseUrl + article.get(a));
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
                        Node object = NodeFactory.createLiteral(name.replace("-", " ") + " (Cafeteria)", XSDDatatype.XSDstring);
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
                if (key.contains("cafeteriaFacility")){
                    found = true;
                }
            }
            if (found == true) {

                subject = null;
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Cafeteria.baseUrl + article.get(a));
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
                if (key.contains("cafeteriaFacility")){
                    found = true;
                }
            }
            if (found == true) {
                subject = null;
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Cafeteria.baseUrl + article.get(a));
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
            }
            return triples;
        }
    }

    private class Menu extends CustomMapping {
        //"canteenMenu":{
        //"soup":"Swet peppers soup",
        //"starter":"Canneloni with salmon gratinated",
        //"meal1":"(Fit@work) Chicken fillet, peppers sauce, green cabbage, grenaille potatoes",
        //"meal1Kcal":420,
        //"meal2":"Dab fillet, pumpkin crème sauce, leeks,\n sweet potatoes",
        //"meal2Kcal":520,
        //"vegetarian":"Green wheat indian style, mushrooms, tempeh",
        //"vegetarianKcal":440
        //}
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {

            ArrayList<Triple> triples = new ArrayList<>();
            String menu = "Menu\n\n";
            for (String a : article.keySet()) {
                    if (a.contains("soup")) {
                        menu += "  Soup: "+article.get(a) + "\n";
                    }
            }
            for (String a : article.keySet()) {
                if (a.endsWith("starter")) {
                    menu += "  Starter: "+article.get(a) + "\n";
                }
            }
            for (String a : article.keySet()) {
                if (a.endsWith("meal1")) {
                    menu += "  Meal 1: "+article.get(a) + "\n";
                }
            }
            for (String a : article.keySet()) {
                if (a.endsWith("meal2")) {
                    menu += "  Meal 2: "+article.get(a) + "\n";
                }
            }
            for (String a : article.keySet()) {
                if (a.endsWith("vegetarian")) {
                    menu += "  Vegetarian: "+article.get(a) + "\n";
                }
            }
            if (!menu.equals("")) {

                Utility utility = new Utility();
                for (String a : article.keySet()) {
                    if (a.contains("code")) {
                        subject = NodeFactory.createURI(Cafeteria.baseUrl + article.get(a));
                        Node predicate = utility.createURI(getMapping().getPropertyUri());
                        Node object = NodeFactory.createLiteral(menu, XSDDatatype.XSDstring);
                        triples.add(new Triple(subject, predicate, object));
                    }
                }
            }
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
                        subject = NodeFactory.createURI(Cafeteria.baseUrl + article.get(a));
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



