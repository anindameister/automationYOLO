package eu.qanswer.data2rdf.configuration;

public abstract class XmlConfigurationFile extends AbstractConfigurationFile {

    public String iterator;

    public String getIterator() {
        return iterator;
    }

    public void setIterator(String iterator) {
        this.iterator = iterator;
    }
}
