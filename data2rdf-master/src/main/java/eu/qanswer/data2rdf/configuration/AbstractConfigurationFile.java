package eu.qanswer.data2rdf.configuration;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigurationFile {

    //the url used for all instances
    public String format;
    public String file;
    public static String baseUrl;
    public String key;
    public ArrayList<Mapping> mappings;
    public ArrayList<Triple> triples = new ArrayList<>();

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMappings(ArrayList<Mapping> mappings) {
        this.mappings = mappings;
    }

    public List<Mapping> getMappings(){
        return this.mappings;
    }

    public Map<String,ObjectUtils.Null> getMappedTags(){
        Map<String, ObjectUtils.Null> map = new HashMap<String,ObjectUtils.Null>();
        for (Mapping m : mappings){
            map.put(m.getTag(),null);
        }
        return map;
    }

    public List<Mapping> getMapping(String tag){
        List<Mapping> properties = new ArrayList<Mapping>();
        for (Mapping m : this.mappings){
            if (m.getTag().equals(tag)){
                properties.add(m);
            }
        }

        return properties;
    }

    public ArrayList<Triple> getTriples() {
        return triples;
    }

    public void setTriples(ArrayList<Triple> triples) {
        this.triples = triples;
    }
}
