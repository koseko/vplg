/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */

package plcc;


import java.io.*;
import java.util.*;

/**
 * This is the static I/O class for the ProtGraph class. It allows you to read ProtGraphs from files and write them
 * to files.
 * 
 * @author spirit
 */
public class ProtGraphs {
    
    private static ObjectInputStream objectIn;

    /**
     * Reads a file that has to contain a serialized ProtGraph object in binary form (as written by ProtGraph.toFile()).
     *
     */
    public static ProtGraph fromSerializedBinaryFile(String file) {

        ProtGraph pg = null;

        try {
            objectIn = new ObjectInputStream(new FileInputStream(file));
            pg = (ProtGraph)objectIn.readObject();
        }
        catch(Exception e) {
            System.err.println("WARNING: Could not read ProtGraph object from file '" + file + "'.");
            pg = null;
        }
        finally {
            try {
                objectIn.close();
            }
            catch(Exception ex) {
                // Wayne interessiert's.
            }
        }

        return(pg);

    }
    
    
    /**
     * Reads a file that has to contain a graph in TGF (trivial graph format) and turns it into a protein graph.
     *
     */
    public static ProtGraph fromTrivialGraphFormat(String file) {

        ProtGraph pg = null;
        Boolean inVertices = true;

        ArrayList<String> lines = FileParser.slurpFile(file);
        ArrayList<SSE> sses = new ArrayList<SSE>();
        String l, vertexPart, vertexPart1, vertexPart2, labelPart, edgePart, vertexLabel, edgeLabel;
        l = vertexLabel = edgeLabel = vertexPart = labelPart = vertexPart1 = vertexPart2 = null;
        String [] words;
        Integer vertex, firstSpace, vertex1, vertex2;
        SSE sse = null;
        Residue r = null;

        Integer curLine = 0;
        for(Integer i = 0; i < lines.size(); i++) {

            curLine++;
            l = lines.get(i);
            // In the nodes part (before the '#')

            // skip empty lines
            if(l.length() < 1) {
                //System.err.println("WARNING: TGF_FORMAT: Skipping line " + curLine + " (empty).");
                continue;
            }

            if(inVertices) {

                if(l.startsWith("#")) {         // We hit the border between vertices and edges, create the graph from the vertices we .
                    inVertices = false;            //  found so far and skip this line.
                    pg = new ProtGraph(sses);
                    pg.setInfo("<NONE>", "<NONE>", "custom");
                    //System.out.println("    Found separator vertex set /edge set separator '#' in line " + curLine + ".");
                    continue;
                }
                else {
                    firstSpace = l.indexOf(" ");

                    if(firstSpace < 0) {
                        // No spaces where found (but the line is non-empty), so there is no vertex label, only the vertex number.
                        // System.out.println("      No spaces found in line " + curLine + ", assuming empty label.");
                        vertexPart = l.trim();
                        labelPart = "";
                    }
                    else {
                        // The line contains spaces, so the part before the 1st space is considered the vertex, the rest is the vertex label.
                        try {
                            vertexPart = l.substring(0, firstSpace);
                            labelPart = (l.substring(firstSpace, l.length())).trim();
                        } catch(Exception e) {
                            // We'll just skip this line for now
                            System.err.println("WARNING: TGF_FORMAT: Skipping line " + curLine + " (could not parse as vertex).");
                            continue;
                        }                                                                        
                    }

                    //System.out.println("    At vertex, line " + curLine + ", vertexPart = '" + vertexPart + "', labelPart = '" + labelPart + "'.");
                    // The parts have been defined, parse them
                    try {
                        vertex = Integer.valueOf(vertexPart);
                        vertexLabel = vertexPart.trim();
                    } catch(Exception e) {
                        System.err.println("WARNING: TGF_FORMAT: Skipping line " + curLine + " (could not extract vertex data).");
                        continue;
                        //System.err.println("ERROR: Parsing vertex line in trivial graph format file '" + file + "' failed. File broken or in wrong format?");
                        //System.exit(-1);
                    }

                    // We can now create the vertex object (a fake SSE)
                    sse = new SSE("H");
                    r = new Residue(vertex, vertex);        // make sure it has a start/end residue
                    sse.addResidue(r);
                    sse.setSeqSseChainNum(vertex);  // this is not true and makes no sense with a non-PG, of course
                    sses.add(sse);
                }
            }
            else {                  // We are in the edge part
                words = l.split("\\s", 3);
                if(words.length < 2) {
                    System.err.println("WARNING: TGF_FORMAT: Skipping line " + curLine + " (less than 2 fields can't encode an edge).");
                    continue;
                }
                else if(words.length == 2) {
                    vertexPart1 = words[0];
                    vertexPart2 = words[1];
                    labelPart = "";
                }
                else {
                    vertexPart1 = words[0];
                    vertexPart2 = words[1];
                    labelPart = words[2];
                }

                // The parts have been defined, parse them
                //System.out.println("    At edge, line " + curLine + ", vertexPart1 = '" + vertexPart1 + "', vertexPart2 = '" + vertexPart2 + "', labelPart = '" + labelPart + "'.");
                try {
                    vertex1 = Integer.valueOf(vertexPart1);
                    vertex2 = Integer.valueOf(vertexPart2);
                    edgeLabel = labelPart.trim();
                } catch(Exception e) {
                    System.err.println("WARNING: TGF_FORMAT: Skipping line " + curLine + " (could not parse as edge).");
                    continue;
                    //System.err.println("ERROR: Parsing vertex line in trivial graph format file '" + file + "' failed. File broken or in wrong format?");
                    //System.exit(-1);
                }

                // We can now add this edge
                if(pg == null) {
                    System.err.println("ERROR: TGF_FORMAT: Graph in file '" + file + "' has no '#' line separating vertices from edges. File broken (skipping edge).");
                }
                else {
                    pg.addContact(vertex1 - 1, vertex2 - 1, 1);
                }


            }
        }

        if(pg == null) {
            System.err.println("WARNING: TGF_FORMAT: Graph in trivial graph format file '" + file + "' is empty (has no edges).");
            return(new ProtGraph(new ArrayList<SSE>()));
        }

        return(pg);

    }
    
    
    /**
     * Parses the plcc format graph file 'file' for meta data and returns it in a HashMap. Everyline in the file that is in the correct format ("> key > value") is parsed and the resulting (key, value) pair
     * is added to the returned HashMap. This function guarantees that the HashMap contains at least one key: format_version. If it is not found in the file,
     * it is set to '1' because version 1 is the only version which did NOT have this field.
     * 
     * @param file the path of the plcc graph file to scan
     * @return the meta data in a HashMap.
     */
    public static HashMap<String, String> getMetaData(String file) {
        
        HashMap md = new HashMap<String, String>();
        
        md.put("format_version", "1");      // will be overwritten later if it occurs in the file. If it does NOT occur, this is the correct value.
        
        ArrayList<String> lines = FileParser.slurpFile(file);
        
        // other vars
        String l = null;            // a line!
        String empty, key, value;
        empty = key = value = null;
        String [] words;

        Integer curLine = 0;
        Boolean error = false;
        
        // Get all met data entries
        for(Integer i = 0; i < lines.size(); i++) {

            curLine++;            
            
            // remove all whitespace from the line, it is not needed and will make splitting way easier later
            l = lines.get(i).replaceAll("\\s*","");
                                    
            if(l.startsWith(">")) {
                
                //System.out.println("[SSE] * Handling line #" + curLine + " of the " + lines.size() + " lines.");
                //System.out.println("[SSE]   Line: '" + l + "'");
                
                try {
                    words = l.split("\\>");
                    
                    if(words.length != 3) {
                        System.err.println("ERROR: PLCC_FORMAT: Hit meta data line containing " + words.length + " fields at line #" + curLine + " (expected 3).");
                        error = true;
                    }
                    
                    empty = words[0];           // the empty string, leftmost field
                    key = words[1];
                    value = words[2];                    
                } catch(Exception e) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken meta data line encountered at line #" + curLine + ". Ignoring.");
                    error = false;              // may have been set before exception!
                    continue;
                }
                
                if(error) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken meta data line encountered at line #" + curLine + ", wrong number of fields. Ignoring.");
                    error = false;
                    continue;
                }
                
                md.put(key, value);
            }
        }
        
        
        return(md);
    }
    
    
    /**
     * Debug function, prints the meta data contained in the plcc file 'file'.
     * @param file the plcc input graph file
     */ 
    public static void printPlccMetaData(String file) {
        
        HashMap<String, String> md = getMetaData(file);
        
        System.out.println("Meta data for plcc graph file '" + file + "' follows:");
        
        for(String key : md.keySet()) {
            System.out.println(" " + key + " = " + (String)md.get(key) );
        }
        
        System.out.println("Meta data output complete.");
    }
    
    
    /**
     * Determines the file format version of the plcc format graph file 'file'. This information can be used to call the proper parsing function for the
     * file format version.
     * @param file the plcc format file to parse
     * @return the 'format_version' meta data value found in the file as an Integer
     */
    public static Integer getPlccFileVersion(String file) {
        
        Integer v = 1;
        HashMap<String, String> md = getMetaData(file);
                
        try {
            v = Integer.parseInt(md.get("format_version"));            
        } catch(Exception e) {
            System.err.println("ERROR: getPlccFileVersion(): format_version is not an Integer in plcc graph file '" + file + "', file broken.");
            System.exit(1);
        }
        
        return(v);               
    }
    
    
    public static ProtGraph fromPlccGraphFormat(String file) {
        Integer v = getPlccFileVersion(file);
        
        if(v == 1) {
            System.err.println("WARNING: Graph file '" + file + "' in deprecated version '" + v + "': not all operations are supported.");
            return(fromPlccGraphFormatv1(file));
        }
        else if(v == 2) {
            return(fromPlccGraphFormatv2(file));
        }
        else {
            System.err.println("ERROR: Could not determine file format version of graph file '" + file + "'.");
            return null;
        }
    }
    
    
    /**
     * Reads a file that has to contain a graph in PLCC graph format version 1 and turns it into a protein graph. Note that this can be used for drawing a protein graph
     * from a file, but it does NOT restore the complete graph because information on the atoms and residues is not contained in the graph file.
     * 
     * This function and the associated file version is deprecated because it does not store the sequential position of an SSE in the primary sequence.
     *
     * @param file the plcc format graph file to parse
     */
    @Deprecated public static ProtGraph fromPlccGraphFormatv1(String file) {

        ProtGraph pg = null;
        Boolean inVertices = true;

        ArrayList<String> lines = FileParser.slurpFile(file);
        ArrayList<SSE> sses = new ArrayList<SSE>();
        
        // tmp vars for vertex lines
        String pdbid, chain, graphType, sseType, pdbStartRes, pdbEndRes, sequence;
        pdbid = chain = graphType = sseType = pdbStartRes = pdbEndRes = sequence = null;
        Integer sseID, dsspStartRes, dsspEndRes, seqSSENum;
        sseID = dsspStartRes = dsspEndRes = seqSSENum = 0;
        
        // tmp vars for edge lines
        String spatRel = null;
        Integer sseID1, sseID2; 
        sseID1 = sseID2 = 0;
        
        // other vars
        String l = null;            // a line!
        String empty = null;
        String [] words;
        SSE sse = null;
        Residue r = null;
        Boolean error = false;

        Integer curLine = 0;
        Integer numContactsAdded = 0;
        
        // Get all vertices so we can create the graph.
        for(Integer i = 0; i < lines.size(); i++) {

            curLine++;            
            
            // remove all whitespace from the line, it is not needed and will make splitting way easier later
            l = lines.get(i).replaceAll("\\s*","");
                                    
            if(l.startsWith("|")) {
                
                //System.out.println("[SSE] * Handling line #" + curLine + " of the " + lines.size() + " lines.");
                //System.out.println("[SSE]   Line: '" + l + "'");
                
                try {
                    words = l.split("\\|");
                    
                    Integer numExpected = 11;
                    
                    if(words.length != numExpected) {
                        System.err.println("ERROR: PLCC_FORMAT: Hit vertex line containing " + words.length + " fields at line #" + curLine + " (expected " + numExpected + ").");
                        error = true;
                    }
                    
                    empty = words[0];           // the empty string, leftmost field
                    pdbid = words[1];
                    chain = words[2];
                    graphType = words[3];
                    sseID = Integer.valueOf(words[4]);
                    sseType = words[5];
                    dsspStartRes = Integer.valueOf(words[6]);
                    dsspEndRes = Integer.valueOf(words[7]);
                    pdbStartRes = words[8];
                    pdbEndRes = words[9];
                    sequence = words[10];
                    
                } catch(Exception e) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken vertex line encountered at line #" + curLine + ". Ignoring.");
                    error = false;              // may have been set before exception!
                    continue;
                }
                
                if(error) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken vertex line encountered at line #" + curLine + ", wrong number of fields. Ignoring.");
                    error = false;
                    continue;
                }
                
                // We can now create the vertex object (a fake SSE)
                sse = new SSE(sseType);
                
                // add the residues
                Integer numRes = dsspEndRes - dsspStartRes + 1;
                
                // Note that we cannot restore the correct AA sequences because the DSSP residue numbers include chain brake extra residues, thus the
                //  length of the sequence string does NOT equal the number of residues.
                //if(sequence.length() != numRes) {
                //    System.err.println("ERROR: PLCC_FORMAT: Broken vertex line encountered at line #" + curLine + ". Sequence length should be " + numRes + " but is " + sequence.length() + ". Ignoring.");
                //    continue;
                //}
                
                
                //Integer sequenceStringIndex = 0;      // unused, see comment on AA sequence above
                for(Integer j = dsspStartRes; j <= dsspEndRes; j++) {
                    r = new Residue(j, j);        // Make sure the SSE has a start/end residue, just put fake values for the PDB numbers, there is no way to determine them unless you have a complete list because they are not necessarily sequential.
                    //r.setAAName1(sequence.substring(sequenceStringIndex, sequenceStringIndex + 1));       // unused, see comment on AA sequence above
                    r.setAAName1("?");                                                                      // see comment on AA sequence above
                    r.setChainID(chain);
                    r.setiCode(" ");
                    
                    sse.addResidue(r);
                    //sequenceStringIndex++;        // unused, see comment on AA sequence above
                }                               
                
                // set other SSE info
                sse.setSeqSseChainNum(sseID);   // This is not correct, but we have no choice because we dont know it in this format version! :| Acutally, this is the reason why this format version is now deprecated.
                sses.add(sse);                
            }
        }
        
        // All vertices have been parsed, create the graph.
        
        if(sses.size() <= 0) {
            System.err.println("ERROR: PLCC_FORMAT: Graph file did not contain any valid SSE lines, vertex set empty. Exiting.");
            System.exit(1);
        }
        else {
            System.out.println("  Parsed " + sses.size() + " SSEs from input file in plcc graph format.");
        }
        
        pg = new ProtGraph(sses);
        pg.setInfo(pdbid, chain, graphType);
        
        // Now create all edges
        curLine = 0;
        for(Integer i = 0; i < lines.size(); i++) {

            curLine++;                        
            
            // remove all whitespace from the line, it is not needed and will make splitting way easier later
            l = lines.get(i).replaceAll("\\s*","");
            
            if(l.startsWith("=")) {
                
                //System.out.println("[Contact] * Handling line #" + curLine + " of the " + lines.size() + " lines.");
                //System.out.println("[Contact]   Line: '" + l + "'");
                
                try {
                    words = l.split("=");
                    
                    if(words.length != 4) {
                        System.err.println("ERROR: PLCC_FORMAT: Hit edge line containing " + words.length + " fields at line #" + curLine + " (expected 4). Ignoring.");
                        error = true;
                    }
                    
                    empty = words[0];
                    sseID1 = Integer.valueOf(words[1]);
                    spatRel = words[2];
                    sseID2 = Integer.valueOf(words[3]);
                            
                    
                } catch(Exception e) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken edge line encountered at line #" + curLine + ". Ignoring.");
                    error = false;                  // may have been set before exception!
                    continue;
                }
                
                if(error) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken edge line encountered at line #" + curLine + ", wrong number of fields. Ignoring.");
                    error = false;
                    continue;
                }
                
                // everything seems fine, add the contact
                pg.addContact(sseID1 - 1, sseID2 - 1, SpatRel.stringToInt(spatRel));
                numContactsAdded++;
                
            }
        }
                                
        // Done.  
        System.out.println("  Parsed " + numContactsAdded + " contacts from input file in plcc graph format.");
            
        if(pg == null) {
            System.err.println("ERROR: Parsing graph from plcc format graph file '" + file + "' failed, returning empty graph.");
            return(new ProtGraph(new ArrayList<SSE>()));
        }
        
        pg.setMetaData(getMetaData(file));

        return(pg);

    }
    
    
    
    
    /**
     * Reads a file that has to contain a graph in PLCC graph format version 2 and turns it into a protein graph. Note that this can be used for drawing a protein graph
     * from a file, but it does NOT restore the complete graph because information on the atoms and residues is not contained in the graph file.
     *
     * @param file the plcc format graph file to parse
     */
    public static ProtGraph fromPlccGraphFormatv2(String file) {

        ProtGraph pg = null;

        ArrayList<String> lines = FileParser.slurpFile(file);
        ArrayList<SSE> sses = new ArrayList<SSE>();
        
        // tmp vars for vertex lines
        String pdbid, chain, graphType, sseType, pdbStartRes, pdbEndRes, sequence;
        pdbid = chain = graphType = sseType = pdbStartRes = pdbEndRes = sequence = null;
        Integer sseID, dsspStartRes, dsspEndRes, seqSSENum;
        sseID = dsspStartRes = dsspEndRes = seqSSENum = 0;
        
        // tmp vars for edge lines
        String spatRel = null;
        Integer sseID1, sseID2; 
        sseID1 = sseID2 = 0;
        
        // other vars
        String l = null;            // a line!
        String empty = null;
        String [] words;
        SSE sse = null;
        Residue r = null;
        Boolean error = false;

        Integer curLine = 0;
        Integer numContactsAdded = 0;
        
        // Get all vertices so we can create the graph.
        for(Integer i = 0; i < lines.size(); i++) {

            curLine++;            
            
            // remove all whitespace from the line, it is not needed and will make splitting way easier later
            l = lines.get(i).replaceAll("\\s*","");
                                    
            if(l.startsWith("|")) {
                
                //System.out.println("[SSE] * Handling line #" + curLine + " of the " + lines.size() + " lines.");
                //System.out.println("[SSE]   Line: '" + l + "'");
                
                try {
                    words = l.split("\\|");
                    
                    Integer numExpected = 12;
                    
                    if(words.length != numExpected) {
                        System.err.println("ERROR: PLCC_FORMAT: Hit vertex line containing " + words.length + " fields at line #" + curLine + " (expected " + numExpected + ").");
                        error = true;
                    }
                    
                    empty = words[0];           // the empty string, leftmost field
                    pdbid = words[1];
                    chain = words[2];
                    graphType = words[3];
                    seqSSENum = Integer.valueOf(words[4]);
                    sseID = (Integer.valueOf(words[5]) - 1);    /** the -1 is because we add 1 when we print it so the list doesnt start at zero and matches the one in the image */
                    sseType = words[6];
                    dsspStartRes = Integer.valueOf(words[7]);
                    dsspEndRes = Integer.valueOf(words[8]);
                    pdbStartRes = words[9];
                    pdbEndRes = words[10];
                    sequence = words[11];
                    
                } catch(Exception e) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken vertex line encountered at line #" + curLine + ". Ignoring.");
                    error = false;              // may have been set before exception!
                    continue;
                }
                
                if(error) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken vertex line encountered at line #" + curLine + ", wrong number of fields. Ignoring.");
                    error = false;
                    continue;
                }
                
                // We can now create the vertex object (a fake SSE)
                sse = new SSE(sseType);
                
                // add the residues
                Integer numRes = dsspEndRes - dsspStartRes + 1;
                
                // Note that we cannot restore the correct AA sequences because the DSSP residue numbers include chain brake extra residues, thus the
                //  length of the sequence string does NOT equal the number of residues.
                //if(sequence.length() != numRes) {
                //    System.err.println("ERROR: PLCC_FORMAT: Broken vertex line encountered at line #" + curLine + ". Sequence length should be " + numRes + " but is " + sequence.length() + ". Ignoring.");
                //    continue;
                //}
                
                
                //Integer sequenceStringIndex = 0;      // unused, see comment on AA sequence above
                for(Integer j = dsspStartRes; j <= dsspEndRes; j++) {
                    r = new Residue(j, j);        // Make sure the SSE has a start/end residue, just put fake values for the PDB numbers, there is no way to determine them unless you have a complete list because they are not necessarily sequential.
                    //r.setAAName1(sequence.substring(sequenceStringIndex, sequenceStringIndex + 1));       // unused, see comment on AA sequence above
                    r.setAAName1("?");                                                                      // see comment on AA sequence above
                    r.setChainID(chain);
                    r.setiCode(" ");
                    
                    sse.addResidue(r);
                    //sequenceStringIndex++;        // unused, see comment on AA sequence above
                }                               
                
                // set other SSE info
                sse.setSeqSseChainNum(seqSSENum);   // This is the correct value, parsed from the input file
                sses.add(sse);                
            }
        }
        
        // All vertices have been parsed, create the graph.
        
        if(sses.size() <= 0) {
            System.err.println("ERROR: PLCC_FORMAT: Graph file did not contain any valid SSE lines, vertex set empty. Exiting.");
            System.exit(1);
        }
        else {
            System.out.println("  Parsed " + sses.size() + " SSEs from input file in plcc graph format.");
        }
        
        pg = new ProtGraph(sses);
        pg.setInfo(pdbid, chain, graphType);
        
        // Now create all edges
        curLine = 0;
        for(Integer i = 0; i < lines.size(); i++) {

            curLine++;                        
            
            // remove all whitespace from the line, it is not needed and will make splitting way easier later
            l = lines.get(i).replaceAll("\\s*","");
            
            if(l.startsWith("=")) {
                
                //System.out.println("[Contact] * Handling line #" + curLine + " of the " + lines.size() + " lines.");
                //System.out.println("[Contact]   Line: '" + l + "'");
                
                try {
                    words = l.split("=");
                    
                    if(words.length != 4) {
                        System.err.println("ERROR: PLCC_FORMAT: Hit edge line containing " + words.length + " fields at line #" + curLine + " (expected 4). Ignoring.");
                        error = true;
                    }
                    
                    empty = words[0];
                    sseID1 = Integer.valueOf(words[1]);
                    spatRel = words[2];
                    sseID2 = Integer.valueOf(words[3]);
                            
                    
                } catch(Exception e) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken edge line encountered at line #" + curLine + ". Ignoring.");
                    error = false;                  // may have been set before exception!
                    continue;
                }
                
                if(error) {
                    System.err.println("ERROR: PLCC_FORMAT: Broken edge line encountered at line #" + curLine + ", wrong number of fields. Ignoring.");
                    error = false;
                    continue;
                }
                
                // everything seems fine, add the contact
                pg.addContact(sseID1 - 1, sseID2 - 1, SpatRel.stringToInt(spatRel));
                numContactsAdded++;
                
            }
        }
                                
        // Done.  
        System.out.println("  Parsed " + numContactsAdded + " contacts from input file in plcc graph format.");
            
        if(pg == null) {
            System.err.println("ERROR: Parsing graph from plcc format graph file '" + file + "' failed, returning empty graph.");
            return(new ProtGraph(new ArrayList<SSE>()));
        }

        
        pg.setMetaData(getMetaData(file));
        return(pg);

    }



}
