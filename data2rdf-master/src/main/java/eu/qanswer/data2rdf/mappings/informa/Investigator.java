package eu.qanswer.data2rdf.mappings.informa;

import eu.qanswer.data2rdf.configuration.JsonConfigurationFile;
import eu.qanswer.data2rdf.configuration.Mapping;

import java.util.ArrayList;
import java.util.Arrays;

public class Investigator extends JsonConfigurationFile {

    public Investigator(){
        file = "/Users/Dennis/PycharmProjects/TrialTrove/crawl_investigators";
        baseUrl = "https://citeline.informa.com/investigators/details/";
        key = "investigatorId";
        mappings = new ArrayList<Mapping>(Arrays.asList(
                new Mapping("investigatorId", "http://www.w3.org/2000/01/rdf-schema#type", "http://www.wikidata.org/entity/Q30093123")
//                new Mapping("investigatorEmails", "http://www.wikidata.org/prop/direct/P968", Type.LITERAL),
//                new Mapping("investigatorPrimaryOrganization", "http://www.wikidata.org/prop/direct/P108", "https://citeline.eu.qanswer.mapping.informa.com/organizations/details/", Type.URI),
//                new Mapping("investigatorLastName", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),
//                new Mapping("investigatorFirstName", "http://www.w3.org/2000/01/rdf-schema#label", Type.CUSTOM), //// check
//                new Mapping("investigatorNPI", "http://www.newclean.eu/npi", Type.LITERAL),
//                new Mapping("investigatorPhoneNumbers", "http://www.wikidata.org/prop/direct/P1329", Type.LITERAL),  //phone number
//                new Mapping("investigatorGeoLocation.lat", "http://www.wikidata.org/prop/direct/P625", Type.CUSTOM),
//                new Mapping("investigatorDegrees", "http://www.wikidata.org/prop/direct/P512", Type.LITERAL),
//                new Mapping("copyrightNotice", "http://www.newclean.eu/copyright", Type.LITERAL),
//                new Mapping("investigatorId", "http://purl.org/dc/terms/identifier", Type.LITERAL),
//                new Mapping("trialTitle", "http://www.w3.org/2000/01/rdf-schema#label", Type.LITERAL),
//                new Mapping("trialStatus","http://www.newclean.eu/status", Type.URI),
//                new Mapping("state","http://www.newclean.eu/state","http://www.newclean.eu/state/", Type.URI_WITH_LABEL),
//                new Mapping("city","http://www.newclean.eu/city","http://www.newclean.eu/city/", Type.URI_WITH_LABEL),
//                new Mapping("country","http://www.newclean.eu/country","http://www.newclean.eu/country/", Type.URI_WITH_LABEL)
    ));
    }
}
