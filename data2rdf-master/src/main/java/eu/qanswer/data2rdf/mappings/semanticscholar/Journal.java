package eu.qanswer.data2rdf.mappings.semanticscholar;

import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;
import eu.qanswer.data2rdf.configuration.Type;

import java.util.ArrayList;
import java.util.Arrays;

public class Journal extends JsonConfigurationFile {
    public Journal() {
        file = "sample-S2-records";
        baseUrl = "http://www.semanticscholar.org/journal/";
        ;
        key = "publications.journalName";
        iterator = "publications";
        mappings = new ArrayList<Mapping>(Arrays.asList(
                new Mapping("journalName", "http://www.wikidata.org/prop/direct/P31", "http://www.wikidata.org/entity/Q737498"),

                new Mapping("journalName", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL)));

    }
}
