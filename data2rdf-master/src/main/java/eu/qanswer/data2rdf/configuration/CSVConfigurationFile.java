package eu.qanswer.data2rdf.configuration;

public abstract class CSVConfigurationFile extends AbstractConfigurationFile {

    public char separator;

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }
}
