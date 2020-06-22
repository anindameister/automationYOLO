package eu.qanswer.data2rdf.configuration;

import org.apache.jena.datatypes.RDFDatatype;

public class Mapping {
    private String tag;
    private String object;
    private String propertyUri;
    private String baseurl;
    private Type type;
    private RDFDatatype datatype;
    private CustomMapping customMapping;

    public Mapping(String tag, String propertyUri, String objectUri){
        this.tag = tag;
        this.propertyUri = propertyUri;
        this.object = objectUri;
        this.type=Type.CLASS;
    }

    public Mapping(String tag, String propertyUri, CustomMapping customMapping, Type type){
        this.tag = tag;
        this.propertyUri = propertyUri;
        this.type = type;
        this.customMapping=customMapping;
        customMapping.setMapping(this);
    }

    public Mapping(String tag, String propertyUri,Type type){
        this.tag = tag;
        this.propertyUri = propertyUri;
        this.type = type;
    }

    public Mapping(String tag, String propertyUri, Type type, RDFDatatype datatype){
        this.tag = tag;
        this.propertyUri = propertyUri;
        this.type = type;
        this.datatype = datatype;
    }
    public Mapping(String tag, String propertyUri, String baseurl,Type type){
        this.tag = tag;
        this.propertyUri = propertyUri;
        this.type = type;
        this.baseurl = baseurl;
    }

    public String getBaseurl() {
        return baseurl;
    }


    public String getTag() {
        return tag;
    }

    public String getPropertyUri() {
        return propertyUri;
    }

    public Type getType() {
        return type;
    }

    public RDFDatatype getDatatype() {
        return datatype;
    }

    public void setDatatype(RDFDatatype datatype) {
        this.datatype = datatype;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public CustomMapping getCustomMapping()
    {
        return customMapping;
    }
}
