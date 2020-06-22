package eu.qanswer.linking.utils;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class LinkKeys {

    public static ArrayList<Pair<String, String>> linkKeys(String endpoint1, String query1, String endpoint2, String query2) {
        HashMap<String, String> dataset1 = execQuery(endpoint1, query1);
        HashMap<String, String> dataset2 = execQuery(endpoint2, query2);
        System.out.println(dataset1.size());
        System.out.println(dataset2.size());


        ArrayList<Pair<String, String>> owlSameAs = new ArrayList<Pair<String, String>>();
        HashMap<String, String> smallerSize;
        HashMap<String, String> biggerSize;

        if (dataset1.size() < dataset2.size()) {
            smallerSize = dataset1;
            biggerSize = dataset2;
        } else {
            smallerSize = dataset2;
            biggerSize = dataset1;
        }
        for (String key1 : smallerSize.keySet()) {
            if (biggerSize.containsKey(key1.toLowerCase()) || biggerSize.containsKey(key1.toUpperCase())) {
                owlSameAs.add(new Pair<String, String>(smallerSize.get(key1), biggerSize.get(key1)));
            }
        }
        System.out.println("Intersection: " + owlSameAs.size());
        System.out.println("Found in Data1 but not in Data2: " + (dataset1.size() - owlSameAs.size()));
        System.out.println("Found in Data2 but not in Data1: " + (dataset2.size() - owlSameAs.size()));
        return owlSameAs;
    }


    private static HashMap<String, String> execQuery(String endpoint1, String sparql1) {
        Query query1 = QueryFactory.create(sparql1);
        QueryEngineHTTP qExe = new QueryEngineHTTP(endpoint1, query1);
        qExe.addParam("format", "json");
        ResultSet result = qExe.execSelect();

        HashMap<String, String> dataset = new HashMap<String, String>();
        int duplicates = 0;
        int total = 0;
        while (result.hasNext()) {
            total++;
            QuerySolution next = result.next();
            String key = "", value = "";
            if (next.get("key").toString() != null)
                key = next.get("key").toString();
            if (next.get("value").toString() != null)
                value = next.get("value").toString();
            if (dataset.containsKey(key)) {
                duplicates = duplicates + 1;
            }
            dataset.put(key, value);
        }
        System.out.println("Total: " + total);
        System.out.println("Without dups: " + dataset.size());
        System.out.println("Dups: " + duplicates);
        return dataset;
    }

}
