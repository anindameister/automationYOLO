package eu.qanswer.data2rdf.mappings.semanticscholar;

import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;

import eu.qanswer.data2rdf.configuration.CustomMapping;
import eu.qanswer.data2rdf.utility.Utility;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Article extends JsonConfigurationFile {
    Article art=this;
    public Article()
    {
        file="/Users/Dennis/IdeaProjects/semanticscholar/src/main/resources/sample-S2-records";
        baseUrl = "http://semanticscholar.org/paper/";;
        key = "publications.id";
        iterator = "publications";
        mappings = new ArrayList<Mapping>(Arrays.asList(
                new Mapping("fake","fake","http://www.wikidata.org/entity/Q591041"),
                new Mapping("fake","fake","http://www.wikidata.org/entity/Q732577"),
                new Mapping("publications(.*).id","http://www.wikidata.org/prop/direct/P31","http://www.wikidata.org/entity/Q13442814"),
                new Mapping("publications(.*).id", "http://www.wikidata.org/prop/direct/P4011", Type.LITERAL),

                new Mapping("title", "http://www.wikidata.org/prop/direct/P1476", Type.LITERAL),
                new Mapping("title", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),

                new Mapping("paperAbstract", "http://purl.org/dc/terms/abstract", Type.LITERAL),

                new Mapping("\bpublications\b","http://www.w3.org/2000/01/rdf-schema#label",new ClassForAbstract(),Type.CUSTOM),

                new Mapping("entities", "http://www.wikidata.org/prop/direct/P921","http://semanticscholar.org/entity/", Type.URI_WITH_LABEL),

                new Mapping("s2Url",    "http://www.wikidata.org/prop/direct/P854","", Type.URI),

                new Mapping("pdfUrls", "http://www.wikidata.org/prop/direct/P4945", Type.URI),

                new Mapping("authors(.*)name", "http://www.wikidata.org/prop/direct/P50",new ClassForAuthor(), Type.CUSTOM),

                new Mapping("inCitations", "http://www.wikidata.org/prop/direct/P2860", Type.URI),

                new Mapping("outCitations", "http://www.wikidata.org/prop/direct/P2860", Type.URI),


                new Mapping("year", "http://www.wikidata.org/prop/direct/P577", Type.LITERAL),

                new Mapping("venue", "http://www.wikidata.org/prop/direct/P1433", new ClassForVenue(),Type.CUSTOM),

                new Mapping("journalVolume", "http://www.wikidata.org/prop/direct/P478", Type.LITERAL),

                new Mapping("journalPages", "http://www.wikidata.org/prop/direct/P304", Type.LITERAL),

//the snippet has medline as a source for all, so a triple is ?p mainSubject medline, so ,mainSubject??
//                new Mapping("sources", "http://purl.org/dc/terms/source", Type.LITERAL),

                new Mapping("doiUrl", "http://www.wikidata.org/prop/direct/P356", Type.LITERAL),

                new Mapping("pmid", "http://www.wikidata.org/prop/direct/P698", Type.LITERAL)

        ));
    }
    private class ClassForAuthor extends CustomMapping {


        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Node object;
            Utility utility = new Utility();
            ArrayList<Triple> triples=new ArrayList<>();
            Node predicate = utility.createURI(getMapping().getPropertyUri());
            if(getMapping().getTag().contains("authors(.*)name"))
            {
                String name=article.get(key);
                String id = article.get(key.replace("name", "ids[1]"));
                System.out.println(key.replace("name", "ids[1]"));
                System.out.println(article);
                String url="http://www.semanticscholar.org/author/"+id;
                object = NodeFactory.createURI(url);
                triples.add(new Triple(subject, predicate, object));
            }
            return triples;
        }
    }
    private class ClassForVenue extends CustomMapping
    {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Node object;
            Utility utility = new Utility();
            ArrayList<Triple> triples=new ArrayList<>();
            Node predicate = utility.createURI(getMapping().getPropertyUri());
            if(getMapping().getTag().contains("venue"))
            {
                String name=article.get(key);
                String url="http://www.semanticscholar.org/journal/"+name;
                object = Utility.createURI(url);
                triples.add(new Triple(subject, predicate, object));
            }
            return triples;
        }
    }
    private class ClassForAbstract extends CustomMapping
    {
        @Override
        public ArrayList<Triple> function(Node subject, HashMap<String, String> article) {
            Node object;
            Utility utility = new Utility();
            ArrayList<Triple> triples=new ArrayList<>();
            Node predicate = utility.createURI(getMapping().getPropertyUri());
            subject = Utility.createURI("http://purl.org/dc/terms/abstract");
            if(getMapping().getTag().contains("pubication"))
            {
                object = Utility.createLiteral("Description");
                triples.add(new Triple(subject, predicate, object));
            }
            return triples;
        }
    }
}