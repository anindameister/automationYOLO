package eu.qanswer.linking.utils;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class SubstituteSpecialCharacters {

    public static Node createURI(String s){
        s = s.replace(" ","_");
        s = s.replace("\"","");
        s = removeSpecialCharacteres(s);
        return NodeFactory.createURI(s);
    }

    public static Node createLiteral(String s){
        s = removeSpecialCharacteres2(s);
        return NodeFactory.createLiteral(s);
    }

    public static Node createLiteral(String s, RDFDatatype datatype){
        s = removeSpecialCharacteres2(s);
        return NodeFactory.createLiteral(s, datatype);
    }

    public static String removeSpecialCharacteres(String s){
        if (s.contains(">")){
            //System.out.println("This URI "+s+"contains illigal caracter >");
            s = s.replace(">","");
        }
        if (s.contains("<")){
            //System.out.println("This URI "+s+"contains illigal caracter <");
            s = s.replace("<","");
        }
        if (s.contains("\\")){
            //System.out.println("This URI "+s+"contains illigal caracter \\");
            s = s.replace("\\","");
        }
        if (s.contains("}")){
            //System.out.println("This URI "+s+"contains illigal caracter }");
            s = s.replace("}","");
        }
        if (s.contains("{")){
            //System.out.println("This URI "+s+"contains illigal caracter {");
            s = s.replace("{","");
        }
        if (s.contains("\"")){
            //System.out.println("This URI "+s+"contains illigal caracter \"");
            s = s.replace("\"","");
        }
        if (s.contains("'")){
            //System.out.println("This URI "+s+"contains illigal caracter \"");
            s = s.replace("'","");
        }
        if (s.contains("`")){
            //System.out.println("This URI "+s+"contains illigal caracter \"");
            s = s.replace("`","");
        }
        if (s.contains("|")){
            //System.out.println("This URI "+s+"contains illigal caracter |");
            s = s.replace("|","");
        }
        if (s.contains("\\|")){
            System.out.println("This URI "+s+"contains illigal caracter |");
            s = s.replace("\\|","");
        }
        if (s.contains("|")){
            //System.out.println("This URI "+s+"contains illigal caracter |");
            s = s.replace("|","");
        }
        if (s.contains(" ")){
            //System.out.println("This URI "+s+"contains illigal caracter |");
            s = s.replace(" ","");
        }
        if (s.contains("&")){
            //System.out.println("This URI "+s+"contains illigal caracter |");
            s = s.replace("&","");
        }
        if (s.contains("^")){
            //System.out.println("This URI "+s+"contains illigal caracter |");
            s = s.replace("^","");
        }

        s = s.replaceAll("\\s","");


        return s;
    }

    public static String removeSpecialCharacteres2(String s){
        if (s.contains("'")){
            //System.out.println("This URI "+s+"contains illigal caracter \"");
            s = s.replace("'","");
        }
        if(s.contains("\\"))
        {
            s=s.replace("\\","\\\\");
        }
        return s;
    }
}

