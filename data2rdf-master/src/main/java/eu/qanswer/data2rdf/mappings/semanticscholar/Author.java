package eu.qanswer.data2rdf.mappings.semanticscholar;

import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;

import java.util.ArrayList;
import java.util.Arrays;

public class Author extends JsonConfigurationFile {
    public Author()
    {
        file="sample-S2-records";
        baseUrl = "http://www.semanticscholar.org/author/";
        key = "publications.authors.ids[1]";
        iterator = "publications.authors";
        mappings = new ArrayList<Mapping>(Arrays.asList(
                new Mapping("authors(.*)name","http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),
                new Mapping("authors(.*)name","http://www.wikidata.org/prop/direct/P2561", Type.LITERAL)));

    }
}
