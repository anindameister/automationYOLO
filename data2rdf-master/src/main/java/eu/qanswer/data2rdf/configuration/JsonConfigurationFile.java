package eu.qanswer.data2rdf.configuration;

//For json lines set iterator to $

public abstract class JsonConfigurationFile extends AbstractConfigurationFile {

    public String iterator;

    public String getIterator() {
        return iterator;
    }

    public void setIterator(String iterator) {
        this.iterator = iterator;
    }
}
