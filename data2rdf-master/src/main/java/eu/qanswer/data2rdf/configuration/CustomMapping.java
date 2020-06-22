package eu.qanswer.data2rdf.configuration;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class CustomMapping {
    Mapping mapping;
    public abstract ArrayList<Triple> function(Node node, HashMap<String,String> article);
    public void setMapping(Mapping mapping)
    {
        this.mapping=mapping;
    }
    public Mapping getMapping()
    {
        return mapping;
    }
}
