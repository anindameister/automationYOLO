package eu.qanswer.linking.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class SubstituteOwlSameAs {

    public void substitute(String owlFile, String datasetFile, String outputFile, String typeOfDataset) {
        int numberOfLinks = 0;
        try {
            if (owlFile != null && datasetFile != null && outputFile != null) {
                HDT owlHdt = HDTManager.generateHDT(owlFile, "http://qanswer.eu/", RDFNotation.TURTLE, new HDTSpecification(), null);
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                if (typeOfDataset.equals("ntriples")) {
                    PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
                    while (iteratorDataset.hasNext()) {
                        Triple tripleDataset = iteratorDataset.next();
                        Node subjectDataset = tripleDataset.getSubject();
                        Node objectDataset = tripleDataset.getObject();
                        Node predicateDataset = tripleDataset.getPredicate();
                        try {
                            IteratorTripleString iteratorOwl = owlHdt.search("", "", subjectDataset.toString());
                            System.out.println(subjectDataset);
                            if (iteratorOwl.hasNext()) {
                                numberOfLinks++;
                                TripleString triple = iteratorOwl.next();
                                inOwl(writer, predicateDataset, objectDataset, triple.getSubject().toString());
                            }
                            else {
                                notInOWl(writer, subjectDataset, predicateDataset, objectDataset);
                            }
                        } catch (NotFoundException e) {
                            notInOWl(writer, subjectDataset, predicateDataset, objectDataset);
                        }
                    }
                } else if (typeOfDataset.equals("hdt")) {
                    System.out.println("HDT type");
                    HDT hdt = HDTManager.mapIndexedHDT(datasetFile, null);
                    NodeDictionary nodeDictionary = new NodeDictionary(hdt.getDictionary());
                    IteratorTripleID iter = hdt.getTriples().search(new TripleID(0, 0, 0));
                    while (iter.hasNext()) {
                        TripleID tripleId = iter.next();
                        long subjectId = tripleId.getSubject();
                        long predicateId = tripleId.getPredicate();
                        long objectId = tripleId.getObject();

                        Node subjectDataset = nodeDictionary.getNode(subjectId, TripleComponentRole.SUBJECT);
                        Node predicateDataset = nodeDictionary.getNode(predicateId, TripleComponentRole.PREDICATE);
                        Node objectDataset = nodeDictionary.getNode(objectId, TripleComponentRole.OBJECT);
                        try {
                            IteratorTripleString iteratorOwl = owlHdt.search("", "", subjectDataset.toString());
                            if (iteratorOwl.hasNext()) {
                                numberOfLinks++;
                                TripleString triple = iteratorOwl.next();
                                inOwl(writer, predicateDataset, objectDataset, triple.getSubject().toString());
                            }
                        } catch (NotFoundException e) {
                            notInOWl(writer, subjectDataset, predicateDataset, objectDataset);
                        }

                    }
                }
                System.out.println("Finished");
                System.out.println("Number of substituted links: " + numberOfLinks);
                writer.close();
                // System.exit(0);
            }
        } catch (IOException | ParserException e) {
            e.printStackTrace();
        }

    }

    private static void inOwl(BufferedWriter writer, Node predicateDataset, Node objectDataset, String newSubject)
    {
        try {

            if (objectDataset.isURI()) {
                writer.write("<" + newSubject + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
            } else if (objectDataset.isLiteral()) {
                String string = objectDataset.getLiteral().getValue().toString();
                Node nodeString = SubstituteSpecialCharacters.createLiteral(string);
                String language = objectDataset.getLiteralLanguage();
                String dataType = objectDataset.getLiteralDatatypeURI();

                if (!language.trim().equals("")) {
                    writer.write("<" + newSubject + "> <" + predicateDataset + "> " + nodeString + "@" + language + ".\n");
                } else if (!dataType.trim().equals("")) {
                    writer.write("<" + newSubject + "> <" + predicateDataset + "> " + nodeString + "^^<" + dataType + "> .\n");
                } else {
                    writer.write("<" + newSubject + "> <" + predicateDataset + "> " + nodeString + " .\n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private static void notInOWl(BufferedWriter writer, Node subjectDataset, Node predicateDataset, Node objectDataset)
    {
        try
        {
            if (objectDataset.isURI())
            {
                writer.write("<" + subjectDataset + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
            }
            else if (objectDataset.isLiteral()) {
                String string = objectDataset.getLiteral().toString();
                Node nodeString = SubstituteSpecialCharacters.createLiteral(string);
                String language = objectDataset.getLiteralLanguage();
                String dataType = objectDataset.getLiteralDatatypeURI();

                if (!language.trim().equals("")) {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + "@" + language + ".\n");
                } else if (!dataType.trim().equals("")) {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + "^^<" + dataType + "> .\n");
                } else {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + " .\n");
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}