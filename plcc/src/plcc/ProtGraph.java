/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */


package plcc;


import java.util.*;
import java.io.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.swing.*;
//import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Collections;


import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;



// Apache Batik SVG library
//import org.apache.batik.svggen.SVGGraphics2D;
//import org.apache.batik.dom.GenericDOMImplementation;
//import org.w3c.dom.Document;
//import org.w3c.dom.DOMImplementation;


public class ProtGraph implements java.io.Serializable {


    private Integer size = null;
    private Integer[ ][ ] matrix;                       // contacts and spatial relations between pairs of SSEs
    private Integer[ ][ ] distMatrix;           // distances of the vertices within this graph
    /** the list of all SSEs of this graph */
    private ArrayList<SSE> sseList;
    /** the list of all SSEs of this graph which should be drawn */
    private ArrayList<DrawSSE> sseDrawList;
    private Integer foldingGraphNumber = null;
    private ArrayList<ArrayList<Integer>> adjLists;
    private ArrayList<Set<Integer>> cliques;    // for Bron-Kerbosch algorithm
    private String pdbid;                               // the PDB ID this graph represents, e.g. "3kmf"
    private String chainid;                             // the chain ID in the PDB file, e.g. "A"
    private String graphType;                           // the graph type, e.g. "albe"
    private Boolean isProteinGraph;                     // true if this is a protein graph, false if this is a folding graph (a connected component of the protein graph)
    private HashMap<String, String> metadata;
    
    private ProtGraph parent;
    private Boolean connectedComponentsComputed;
    private Boolean distancesCalculated;
    private ArrayList<ProtGraph> connectedComponents;

    public String getPdbid() { return(this.pdbid); }
    public String getChainid() { return(this.chainid); }
    public String getGraphType() { return(this.graphType); }
    public Integer getFoldingGraphNumber() { return(this.foldingGraphNumber); }
    public void setFoldingGraphNumber(Integer i) { this.foldingGraphNumber = i; }
    public void setPdbid(String s) { this.pdbid = s; this.metadata.put("pdbid", s); }
    public void setChainid(String s) { this.chainid = s; this.metadata.put("chainid", s);}
    public void setGraphType(String s) { this.graphType = s; this.metadata.put("graphtype", s);}
    public Boolean isProteinGraph() { return(this.isProteinGraph); }
    public Boolean isFoldingGraph() { return( ! this.isProteinGraph); }
    public void declareProteinGraph() { this.isProteinGraph = true; this.foldingGraphNumber = -1; this.parent = null; }
    
    public void declareFoldingGraphOf(ProtGraph p) { 
        // checking for cennectivity calculates the connected components and this is used in the function, so checking for
        //  connectivity here would produce an andless loop. in short, we don't check here atm.
        //if(this.isConnected()) {
            this.isProteinGraph = false;
            this.parent = p; 
        //}
        //else {
        //    System.err.println("ERROR: declareFoldingGraphOf(): Tried to declare a non-connected graph a folding graph, which contradicts the FG definition.");
        //    System.exit(1);            
        //}        
    }

    
    /**
     * Sets the info fields of this graph, defining the PDB ID as 'pdbid', the chain id as 'chainid' and the graph type as 'graphType'. Also sets the meta data.
     * @param pdbid the PDB identifier, e.g., "8icd"
     * @param chainid the PDB chain ID, e.g., "A"
     * @param graphType the graph type, e.g., "albe"
     */
    public void setInfo(String pdbid, String chainid, String graphType) {
        this.pdbid = pdbid;
        this.chainid = chainid;
        this.graphType = graphType;
        
        this.metadata.put("pdbid", pdbid);
        this.metadata.put("chainid", chainid);
        this.metadata.put("graphtype", graphType);
    }
    

    /**
     * Constructor. Requires a list of SSEs that will be represented by the vertices of the graph.
     * @param sses a list of SSEs which make up this folding graph. The contacts have to be added later (or there will be none).
     */
    ProtGraph(ArrayList<SSE> sses) {
        this.sseList = sses;
        this.sseDrawList = new ArrayList<DrawSSE>();
        this.size = sseList.size();
        this.matrix = new Integer[size][size];
        this.distMatrix = new Integer[size][size];      // distances in graph
        this.isProteinGraph = true;
        this.foldingGraphNumber = -1;
        this.parent = null;
        this.connectedComponents = new ArrayList<ProtGraph>();
        
        this.connectedComponentsComputed = false;
        this.distancesCalculated = false;

        adjLists = new ArrayList<ArrayList<Integer>>();
        // add one ArrayList for each SSE
        for (Integer i = 0; i < sseList.size(); i++) {
            adjLists.add(new ArrayList<Integer>());
            sseList.get(i).setSeqIndexInGraph(i);
        }

        this.metadata = new HashMap<String, String>();
        this.init();
    }

    
    /**
     * Determines the maximum vertex degree in this graph.
     * @return the maximum vertex degree
     */
    public Integer maxVertexDegree() {

        Integer max = 0;

        for(Integer i = 0; i < this.size; i++) {
            if(this.degreeOfVertex(i) > max) {
                max = this.degreeOfVertex(i);
            }
        }

        return(max);
    }

    /**
     * Determines the minimum vertex degree in this graph.
     * @return the minimum vertex degree
     */
    public Integer minVertexDegree() {

        Integer min = Integer.MAX_VALUE;

        for(Integer i = 0; i < this.size; i++) {
            if(this.degreeOfVertex(i) < min) {
                min = this.degreeOfVertex(i);
            }
        }

        if(this.size <= 1) {
            return(0);
        }
        return(min);
    }

    
    /**
     * Determines whether this graph contains a vertex with degree n.
     * @return true if it does, false otherwise
     */
    public Boolean hasVertexWithDegree(Integer n) {

        Boolean hasVert = false;

        for(Integer i = 0; i < this.size; i++) {
            if(this.degreeOfVertex(i).equals(n)) {
                hasVert = true;
            }
        }

        return(hasVert);
    }

    /**
     * Determines whether this graph is bifurcated (has a vertex with more than 2 neighbors).
     * @return true if it is, false otherwise 
     */
    public Boolean isBifurcated() {
        return(this.maxVertexDegree() > 2);
    }


    /**
     * Determines the SSE/vertex which is closest to the N-terminus (has the lowest DSSP start residue number).
     * @return the index of the vertex in the SSE list
     */
    public Integer closestToNTerminus() {
        Integer vertIndex = -1;
        Integer minDsspNum = Integer.MAX_VALUE;

        for(Integer i = 0; i < this.size; i++) {
            if(sseList.get(i).getStartDsspNum() < minDsspNum) {
                minDsspNum = sseList.get(i).getStartDsspNum();
                vertIndex = i;
            }
        }

        if(vertIndex < 0) {
            System.err.println("WARNING: closestToNTerminus(): No SSE found, returning '" + vertIndex + "'.");
            if(this.size > 0) {
                System.err.println("ERROR: closestToNTerminus(): Graph has " + this.size + " vertices, so not finding anything is a bug.");
                System.exit(1);
            }
        }
        return(vertIndex);
    }


    /**
     * Determines which of the vertices in the List someVertices is closest to the N-terminus (in sequential order/ AA sequence).
     * @return the index of the vertex or -1 if no such vertex exists in this graph (i.e., this graph is empty)
     */
    public Integer closestToNTerminusOf(java.util.List<Integer> someVertices) {
        Integer vertex = -1;
        Integer minDsspNum = Integer.MAX_VALUE;

        for(Integer i = 0; i < someVertices.size(); i++) {
            if(sseList.get(someVertices.get(i)).getStartDsspNum() < minDsspNum) {
                minDsspNum = sseList.get(someVertices.get(i)).getStartDsspNum();
                vertex = someVertices.get(i);
            }
        }

        if(vertex < 0) {
            System.err.println("WARNING: closestToNTerminusOf(): No SSE found in list, returning '" + vertex + "'.");
            if(someVertices.size() > 0) {
                System.err.println("ERROR: closestToNTerminusOf(): List has " + someVertices.size() + " vertices, so not finding anything is a bug.");
                System.exit(-1);
            }
        }
        return(vertex);
    }


    /**
     * Determines the SSE/vertex which is closest to the N-terminus (has the lowest DSSP start residue number) and has a degree of 1.
     * @return the index of the vertex or -1 if no such vertex exists in this graph
     */
    public Integer closestToNTerminusDegree1() {
        Integer vertIndex = -1;
        Integer minDsspNum = Integer.MAX_VALUE;

        for(Integer i = 0; i < this.size; i++) {
            if(sseList.get(i).getStartDsspNum() < minDsspNum && this.degreeOfVertex(i) == 1) {
                minDsspNum = sseList.get(i).getStartDsspNum();
                vertIndex = i;
            }
        }

        if(vertIndex < 0) {
            System.err.println("WARNING: closestToNTerminusDegree1(): No SSE found, returning '" + vertIndex + "'.");
            if(this.size > 0) {
                System.err.println("ERROR: closestToNTerminusDegree1(): Graph has " + this.size + " vertices, so not finding anything is a bug.");
                System.exit(-1);
            }
        }
        return(vertIndex);
    }

    /**
     * Returns a String which represents this graph in 'Trivial Graph Format'. See http://docs.yworks.com/yfiles/doc/developers-guide/tgf.html.
     * @return the (multi-line) TGF string
     */
    public String toTrivialGraphFormat() {

        String tgf = "";

        // add vertices
        for(Integer i = 0; i < this.sseList.size(); i++) {
            tgf += (i + 1) + " " + (i + 1) + "-" + sseList.get(i).getSseType() + "\n";
        }

        // separator to indicate that edges follow
        tgf += "#\n";

        // edges
        for(Integer i = 0; i < this.sseList.size(); i++) {
            for(Integer j = i + 1; j < this.sseList.size(); j++) {
                if(this.containsEdge(i, j)) {
                    tgf += (i + 1) + " " + (j + 1) + SpatRel.getString(this.getContactType(i, j)) + "\n";
                }
            }
        }

        return(tgf);
    }

    /**
     * Determines the SSE/vertex which is closest to the C-terminus (has the highest DSSP end residue number).
     */
    public Integer closestToCTerminus() {
        Integer vertIndex = -1;
        Integer maxDsspNum = Integer.MIN_VALUE;

        for(Integer i = 0; i < this.size; i++) {
            if(sseList.get(i).getEndDsspNum() > maxDsspNum) {
                maxDsspNum = sseList.get(i).getEndDsspNum();
                vertIndex = i;
            }
        }

        if(vertIndex < 0) {
            System.err.println("WARNING: closestToCTerminus(): No SSE found, returning '" + vertIndex + "'.");
            if(this.size > 0) {
                System.err.println("ERROR: closestToCTerminus(): Graph has " + this.size + " vertices, so not finding anything is a bug.");
                System.exit(-1);
            }
        }
        return(vertIndex);
    }

    /**
     * Orders the vertices of this graph by their spatial distance (in the graph) to the N-terminus.
     * @return An array containing the vertex indices, ordered by their graph distance (length of the shortest path to) to the residue that is closest to the N-terminus.
     */
    public Integer[] vertexOrderSpatial() {
        Integer ntIndex = this.closestToNTerminus();
        Integer [] distN = this.pathDistanceAllVerts(ntIndex);

        System.out.print("      Printing distances of all vertices to vertex " + ntIndex + ", which is clostest to the N-terminus:\n      ");
        for(Integer i = 0; i < this.size; i++) {
            System.out.print(" " + distN[i]);
        }
        System.out.print("\n");

        Integer [] spatialOrder = new Integer[this.size];
        // Init order with sequential order
        for(Integer i = 0; i < this.size; i++) {
            spatialOrder[i] = i;
        }

        // Now sort the spatialOrder array by the distances in the distN array
        Boolean switched = true;
        Integer tmp;
        while(switched) {
            switched = false;
            for(Integer i = 1; i < this.size; i++) {
                if(distN[i] < distN[i-1]) {
                    // This pair is in wrong order, switch it.
                    tmp = spatialOrder[i-1];
                    spatialOrder[i-1] = spatialOrder[i];
                    spatialOrder[i] = tmp;

                    // Also switch the value in distN, of course
                    tmp = distN[i-1];
                    distN[i-1] = distN[i];
                    distN[i] = tmp;

                    switched = true;
                }
            }
        }

      
        return(spatialOrder);
    }

    /**
     * Calculates the distance of the shortest path to vertex x from all vertices in this graph.
     * @return An array with the distance to x for all vertices (defined by their vertex indices). If a vertex is not reachable from x (it is not in the same connected component), its distance is set to -1.
     */
    public Integer[] pathDistanceAllVerts(Integer x) {

        if(x < 0 || x >= this.size) {
            System.err.println("ERROR: pathDistanceAllVerts(): Vertex index '" + x + "' invalid.\n");
            System.exit(-1);
        }

        System.out.println("      Calculating distance of all " + this.size + " vertices to vertex " + x + ".");

        // Now perform breadth-first search with source vertex x and fill in the computed distances. Vertices not in the
        //  connected component of x will not be found and their distance will remain at -1.
        Integer [] color = new Integer [this.size];
        Integer [] dist = new Integer [this.size];
        Integer [] predec = new Integer [this.size];
        Integer v = null;
        LinkedList<Integer> queue;


        // Init stuff
        for(Integer i = 0; i < sseList.size(); i++) {
            color [i] = 1;  // 1 = white, 2 = gray, 3 = black. White vertices have not been handled yet, gray ones
                            //  are currently being handled and black ones have already been handled.
            dist[i] = -1;    // distance of vertex i to the root of the search (x)
            predec[i] = -1;       // the predecessor of vertex i
        }

        queue = new LinkedList<Integer>();

        // Start breadth-first search in vertex x
        color[x] = 2;   // x is currently being handled
        dist[x] = 0;    // the distance of x ito itself is ... zero! ;)
        queue.addFirst(x);

        while( ! queue.isEmpty()) {
            v = queue.peekFirst();      // v is the 1st element of the FIFO

            // For all neighbors w of v
            for(Integer w : (adjLists.get(v))) {
                // If w has not been treated yet
                if(color[w].equals(1)) {
                    color[w] = 2;               // ...now it is being treated
                    dist[w] = dist[v] + 1;      // w is a successor of v (a neighbor of v that is treated after v)
                    predec[w] = v;              // so v is a predecessor of w
                    queue.addLast(w);           // add the neighbors of v to the queue so all their neighbors are checked, too.
                                                //   (Note that adding them to the end makes this find all vertices in distance n before finding
                                                //    any vertex in distance n + 1 to i.)
                }
            }
            queue.removeFirst();    // This vertex has just been handled,
            color[v] = 3;           //  so mark it as handled.
        }

        // The queue is empty, so all vertices reachable from x have been checked.
        return(dist);
    }

    /**
     * Determines the distance from vertex #x to vertex #y in the graph.
     * @return The length of the shortest path between the vertices at indices x and y.
     */
    public Integer distPath(Integer x, Integer y) {
        Integer [] distAllToX = pathDistanceAllVerts(x);
        return(distAllToX[y]);
    }


    
    /**
     * Returns whether an edge exists between vertices x and y.
     */
    public Boolean containsEdge(Integer x, Integer y) {
        return(sseContactExistsPos(x, y));
    }

    /**
     * Returns whether an edge between vertices x and y exists.
     */
    public Boolean sseContactExistsPos(Integer x, Integer y) {
        if(matrix[x][y] > 0) {
            return(true);
        }
        else {
            return(false);
        }
    }

    /**
     * Returns the number of vertices of this graph.
     */
    public Integer numVertices() {
        return(this.size);
    }


    /**
     * Serializes this graph and writes it to a (binary) file that can be read by the ProtGraphs.fromFile() method to restore the ProtGraph object.
     */
    public Boolean toFileSerialized(String filePath) {

        //System.out.println("    Writing ProtGraph of type " + gt + " with " + pg.numVertices() + " vertices and " + pg.numEdges() + " edges to file " + file + ".");

        FileOutputStream fos = null;
        ObjectOutputStream outStream = null;
        Boolean res = false;

        try {
            fos = new FileOutputStream( filePath );

            outStream = new ObjectOutputStream( fos );

            outStream.writeObject( this );

            outStream.flush();          // Always flush, this ain't the bundeswehr!
            res = true;
        }
        catch (Exception e) {
            System.err.println("WARNING: Could not write serialized ProtGraph object to file '" + filePath + "'.");
            e.printStackTrace();
            res = false;
        }
        finally {
            try {
                if(fos != null) {
                    fos.close();
                }
                if(outStream != null) {
                    outStream.close();
                }
            } catch (Exception e) { /* go 2 hell */ }
        }

        return(res);

    }


    /**
     * Adds an edge labeled with spatialRelation between vertices x and y.
     */
    public void addContact(Integer x, Integer y, Integer spatialRelation) {
        
        if(x >= adjLists.size() || y >= adjLists.size()) {
            System.err.println("ERROR: addContact(): Cannot add contact between SSEs with indeces " + x + " and " + y + ", list size is " + adjLists.size() + ".");
            System.exit(-1);
        }
        
        adjLists.get(x).add(y);
        adjLists.get(y).add(x);
        matrix[x][y] = spatialRelation;
        matrix[y][x] = spatialRelation;
    }

    /**
     * Removes the edge between vertices x and y.
     */
    public void removeContact(Integer x, Integer y) {
        matrix[x][y] = SpatRel.NONE;
        matrix[y][x] = SpatRel.NONE;
        adjLists.get(x).remove(y);
        adjLists.get(y).remove(x);
    }

    
    /**
     * Returns the type of the contact between the SSEs at indices (x, y) as an Integer. See the SpatRel class for info on the contact
     * types encoded by the Integers. Atm, the following contacts are defined: 0=no contact, 1=mixed, 2=parallel, 3=antiparallel, 4=ligand.
     * You can also call SpatRel.getString(i) to get the string representation of some Integer i.
     */
    public Integer getContactType(Integer x, Integer y) {

        if(x >= this.sseList.size() || y >= this.sseList.size()) {
            System.err.println("ERROR: getContactType(): Contact " + x + "/" + y + " out of range (graph has " + this.sseList.size() + " vertices).");
            System.exit(-1);
        }

        return(matrix[x][y]);
    }

    /**
     * Returns the number of edges in this graph. Note that this should be divided
     * by 2 in order to get the number of contacts between SSEs because a contact x<->y is
     * counted twice (x->y and y->x) in this undirected graph.
     */
    public Integer numEdges() {
        Integer n = 0;

        for(Integer k = 0; k < this.size; k++) {
            for(Integer l = 0; l < this.size; l++) {
                if(this.sseContactExistsPos(k, l)) {
                    n++;
                }
            }
        }
        return(n);
    }
    
    
    /**
     * Determines the number of SSE contacts in this graph (which is numEdges() / 2).
     * @return the number of SSE contacts
     */
    public Integer numSSEContacts() {
        return(this.numEdges() / 2);        
    }


    /**
     * Returns the degree of the vertex at index x.
     */
    public Integer degreeOfVertex(Integer x) {
        Integer degree = 0;

        for(Integer i = 0; i < this.size; i++) {
            if(this.containsEdge(x, i)) {
                degree++;
            }
        }
        return(degree);
    }

    
    /**
     * Returns the number of connected components this graph consists of, computing them first if necessary.
     */
    public Integer numConnectedComponents() {
        
        if( ! this.connectedComponentsComputed) {
            this.computeConnectedComponents();
        }
        
        return(this.connectedComponents.size());
    }

    
    /**
     * Determines whether this graph is connected, i.e. consists of a single connected component.
     */
    public Boolean isConnected() {
        return(this.numConnectedComponents().equals(1));
    }

    /**
     * Determines the neighbors of a vertex.
     * @return An ArrayList containing the vertex list indices of all neighbors of the vertex at 'vertexIndex'.
     */
    public ArrayList<Integer> neighborsOf(Integer vertexIndex) {
        return(adjLists.get(vertexIndex));
    }
    
    
    /**
     * This function determines a neighbor of the vertex at index 'vertexID' which is not the vertex with index 'noThisNeighbor'.
     * So if a vertex has 2 neighbors, it gets you the other one.
     * @param vertexID the index of the vertex to consider
     * @param notThisNeighbor the index of the neighbor you do NOT want
     * @return the index of the second neighbor or -1 if no such vertex exists or if this neighbor cannot be determined uniquely (i.e., the vertex 'vertexID' does NOT have exactly 2 neighbors).
     */
    public Integer getVertexNeighborBut(Integer vertexID, Integer notThisNeighbor) {
        
        Integer err = -1;
        
        if(this.degreeOfVertex(vertexID) != 2) {
            return(err);
        }
        
        for(Integer i : this.neighborsOf(vertexID)) {
            if(i != notThisNeighbor) {
                return(i);
            }
        }
        
        return(err);
    }



    /**
     * Determines all connected components of this graph and writes them into the connectedComponents ArrayList.
     * See Cormen et al. 2001, Introduction to Algorithms.
     */
    public void computeConnectedComponents() {
        ArrayList<ProtGraph> conComps = new ArrayList<ProtGraph>();

        // If the list of SSEs is empty, there are no connected components
        if(this.size < 1) {
            return;
        }

        Integer [] color = new Integer [this.size];
        Integer [] dist = new Integer [this.size];
        Integer [] predec = new Integer [this.size];
        Integer [] m = new Integer [this.size];
        Integer v = null;
        LinkedList<Integer> queue;

        Integer conCompNum;

        // Init stuff
        for(Integer i = 0; i < sseList.size(); i++) {
            color [i] = 1;  // 1 = white, 2 = gray, 3 = black. White vertices have not been handled yet, gray ones
                            //  are currently being handled and black ones have already been handled.
            dist[i] = Integer.MAX_VALUE;    // distance of vertex i to the root of the connected component
            predec[i] = -1;       // the predecessor of vertex i
            m[i] = 0;               // the connected component vertex i is part of
        }

        // The number of the 1st connected component. Do NOT use 0!
        conCompNum = 1;     // current connected component number
        queue = new LinkedList<Integer>();

        // Start breadth-first search in every vertex
        for(Integer i = 0; i < sseList.size(); i++) {

            // If vertex i is not yet part of any connected component...
            if(! (m[i] > 0)) {

                // Mark the current vertex with the current CC number. This is not mentioned in Cormen et al. 2001 but
                //  not doing it will obviously break stuff.
                m[i] = conCompNum;

                color[i] = 2;   // i is currently being handled
                dist[i] = 0;    // i is the root of this connected component
                queue.addFirst(i);
            
                while( ! queue.isEmpty()) {
                    v = queue.peekFirst();      // v is the 1st element of the FIFO

                    // For all neighbors w of v
                    for(Integer w : neighborsOf(v)) {
                        // If w has not been treated yet
                        if(color[w].equals(1)) {
                            color[w] = 2;               // ...now it is being treated
                            dist[w] = dist[v] + 1;      // w is a successor of v (a neighbor of v that is treated after v)
                            predec[w] = v;              // so v is a predecessor of w
                            m[w] = conCompNum;          // w is part of the current connected component because it has been reached from its root v
                            queue.addLast(w);           // add the neighbors of v to the queue so all their neighbors are checked, too.
                                                        //   (Note that adding them to the end makes this find all vertices in distance n before finding
                                                        //    any vertex in distance n + 1 to i.)

                        }                        
                    }
                    queue.removeFirst();    // This vertex has just been handled,
                    color[v] = 3;           //  so mark it as handled.
                }
                // The queue is empty, so all vertices reachable from i have been checked. Start the next
                //  connected component.
                conCompNum++;
            }
        }

        // All vertices are marked with their comCompNum, create the graphs
        /*
        System.out.print("++Printing array info for all " + this.size + " vertices:\n++");
        for(Integer i = 0; i < this.size; i++) {
            System.out.printf("%2d", i);
        }
        System.out.print("\n++CC marking\n++");
        for(Integer i = 0; i < this.size; i++) {
            System.out.print(" " + m[i]);
        }
        System.out.print("\n++color\n++");
        for(Integer i = 0; i < this.size; i++) {
            System.out.print(" " + color[i]);
        }
        System.out.print("\n++dist\n++");
        for(Integer i = 0; i < this.size; i++) {
            System.out.print(" " + dist[i]);
        }
        System.out.print("\n++predecessor\n++");
        for(Integer i = 0; i < this.size; i++) {
            System.out.print(" " + predec[i]);
        }
        System.out.print("\n++Printing of CC markings done.\n");
         */

        // Iterate through all connected components (CCs)
        Integer [] numInNewGraph;
        Integer [] numInOldGraph;
        Integer numVerticesAdded;
        Integer numEdgesAdded;
        ProtGraph tpg;
        for(Integer i = 0; i <= conCompNum; i++) {
            numVerticesAdded = 0;
            numEdgesAdded = 0;
             numInNewGraph = new Integer [sseList.size()];
             numInOldGraph = new Integer [sseList.size()];
             // init the arrays
             for(Integer j = 0; j < sseList.size(); j++) {
                 numInNewGraph[j] = -1;
                 numInOldGraph[j] = -1;
             }


            // For each CC, create a graph with all SSEs that are marked with this connected component number.
            // We need to get all SSEs first because we need to pass the list to the constructor:
            ArrayList<SSE> tmpSSEList = new ArrayList<SSE>();
            for(Integer j = 0; j < sseList.size(); j++) {

                // If SSE j is marked to be part of connected component i
                if(m[j].equals(i)) {
                    // ...add it to the list of SSEs for that CC.
                    tmpSSEList.add(sseList.get(j));
                    numInNewGraph[j] = numVerticesAdded;
                    numInOldGraph[numVerticesAdded] = j;
                    numVerticesAdded++;
                }

            }
            // Ok, we got the SSEs. Now create the graph.
            if(tmpSSEList.size() < 1) { continue; }
            tpg = new ProtGraph(tmpSSEList);

            // Now add the contacts/edges between the vertices by iterating through the contact matrix of this graph and
            //  translating the indices to the new graph.
            for(Integer k = 0; k < this.size; k++) {
                for(Integer l = 0; l < this.size; l++) {
                    
                    // If there is such a contact in this graph..
                    if(this.sseContactExistsPos(k, l)) {

                        // ...we may need to add the contact to the new graph. Only if both vertices of that edge
                        //  are part of the new graph, of course.
                        if(numInNewGraph[k] >= 0 && numInNewGraph[l] >= 0) {
                            tpg.addContact(numInNewGraph[k], numInNewGraph[l], matrix[k][l]);
                            numEdgesAdded++;
                        }
                    }
                }
            }

            // All the contacts have been added, the graph is complete. Let's add it to the list.
            //System.out.println("  Found new connected component consisting of " + numVerticesAdded + "/" + tpg.numVertices() + " vertices and " + numEdgesAdded + "/" + tpg.numEdges() + " edges:");
            //tpg.print();
            tpg.declareFoldingGraphOf(this); // Each connected component of a protein graph is a folding graph
            tpg.setInfo(this.pdbid, this.chainid, this.graphType);
            tpg.setFoldingGraphNumber(i);
            conComps.add(tpg);
        }
        
        this.connectedComponents = conComps;
        this.connectedComponentsComputed = true;
    }


    /**
     * Determines the index of the SSE in the SSE list that the given DSSP residue is part of.
     * @param dsspResNum the DSSP residue number, which is unique in a chain
     * @return the index of the SSE containing dsspResNum or -1 if no SSE in the list contains this residue
     */
    public Integer getSSEPosOfDsspResidue(Integer dsspResNum) {
        Integer pos = -1;

        for(Integer i = 0; i < this.sseList.size(); i++) {
            if( (this.sseList.get(i).getStartResidue().getDsspResNum() <= dsspResNum) && (this.sseList.get(i).getEndResidue().getDsspResNum() >= dsspResNum)  ) {
                //System.out.println("   +DSSP Residue " + dsspResNum + " is part of SSE #" + i + ": " + this.sseList.get(i).shortStringRep() + ".");
                return(i);
            }
        }

        //System.out.println("   -DSSP Residue " + dsspResNum + " is NOT part of any SSE in list, returning " + pos + ".");
        return(pos);
    }

    /**
     * Returns the SSE represented by vertex position in this graph.
     * @param position the index in the SSE list
     * @return the SSE object at the given index
     */
    public SSE getSSEByPosition(Integer position) {
        if(position >= this.size) {
            System.err.println("ERROR: getSSE(): Index " + position + " out of range, matrix size is " + this.size + ".");
            System.exit(-1);
        }
        return(sseList.get(position));
    }


    /**
     * Returns the position of an SSE (defined by its DSSP start and end residues) in the vertex list.
     * @return The index of the SSE if it was found, -1 otherwise.
     */
    public Integer getSsePositionInList(Integer dsspStart, Integer dsspEnd) {

        for(Integer i = 0; i < this.sseList.size(); i++) {

            if(this.sseList.get(i).getStartResidue().getDsspResNum().equals(dsspStart)) {
                if(this.sseList.get(i).getEndResidue().getDsspResNum().equals(dsspEnd)) {
                    return(i);
                }
            }

        }

        //System.out.println("    No SSE with DSSP start and end residues " + dsspStart + "/" + dsspEnd + " found.");
        return(-1);
    }


    /**
     * Inits the arrays, removing all edges from this graph.
     */
    private void init() {
        for(Integer i = 0; i < size; i++) {
            for(Integer j = 0; j < size; j++) {
                matrix[i][j] = SpatRel.NONE;
                distMatrix[i][j] = Integer.MAX_VALUE;
            }
        }
    }


    /**
     * Inits the arrays, removing all edges from this graph.
     */
    public void reinit() {
        this.init();
    }

    /**
     * Prints the matrix of spatial relations between the SSEs to STDOUT.
     */
    public void print() {

        String spacer = "    ";

        System.out.println(spacer + "Spatial relations matrix of the " + this.size + " SSEs follows:");
        //System.out.print("SSEs:");
        System.out.print(spacer);
        for(Integer i = 0; i < this.size; i++) {
            System.out.printf("%2d", + sseList.get(i).getSSESeqChainNum());
        }
        System.out.print("\n");

        for(Integer i = 0; i < this.size; i++) {
            // print line i
            System.out.print(spacer);
            for(Integer j = 0; j < this.size; j++) {
                // print column j of line i
                if(i.equals(j)) {
                    System.out.print(". ");
                }
                else {
                    System.out.printf("%s ", SpatRel.getString(matrix[i][j]));
                }
            }
            System.out.print("\n");
        }
    }

    // public String get

    /**
     * Implements the Bron-Kerbosch algorithm to find all maximal (= non-extandable) cliques. Note that these are NOT
     * the largest cliques in the graph.
     */
    public ArrayList<Set<Integer>> getMaximalCliques() {

        cliques = new ArrayList<Set<Integer>>();        // global class var

        List<Integer> potential_clique = new ArrayList<Integer>();
        List<Integer> candidates = new ArrayList<Integer>();
        List<Integer> already_found = new ArrayList<Integer>();

        // add all candidate vertices
        for(Integer i = 0; i < this.size; i++) {
            candidates.add(i);
        }

        findCliques(potential_clique, candidates, already_found);
        return cliques;

    }

    /**
     * Part of the Bron-Kerbosch algorithm.
     */
    private void findCliques(List<Integer> potential_clique, List<Integer> candidates, List<Integer> already_found)
    {
        List<Integer> candidates_array = new ArrayList<Integer>(candidates);
        if (!end(candidates, already_found)) {
            // for each candidate_node in candidates do
            for (Integer candidate : candidates_array) {
                List<Integer> new_candidates = new ArrayList<Integer>();
                List<Integer> new_already_found = new ArrayList<Integer>();

                // move candidate node to potential_clique
                potential_clique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (Integer new_candidate : candidates) {
                    if (this.containsEdge(candidate, new_candidate)) {
                        new_candidates.add(new_candidate);
                    } 
                }

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (Integer new_found : already_found) {
                    if (this.containsEdge(candidate, new_found)) {
                        new_already_found.add(new_found);
                    } 
                }

                // if new_candidates and new_already_found are empty
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) {
                    // potential_clique is maximal_clique
                    cliques.add(new HashSet<Integer>(potential_clique));
                }
                else {
                    // recursive call
                    findCliques(
                        potential_clique,
                        new_candidates,
                        new_already_found);
                } 

                // move candidate_node from potential_clique to already_found;
                already_found.add(candidate);
                potential_clique.remove(candidate);
            }
        }
    }

    /**
     * part of the Bron-Kerbosch Algorithm.
     */
    private boolean end(List<Integer> candidates, List<Integer> already_found)
    {
        // if a node in already_found is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for (Integer found : already_found) {
            edgecounter = 0;
            for (Integer candidate : candidates) {
                if (this.containsEdge(found, candidate)) {
                    edgecounter++;
                } // of if
            } // of for
            if (edgecounter == candidates.size()) {
                end = true;
            }
        } // of for
        return end;
    }


    /**
     * Generates the sequential notation (SEQ) of this protein graph.
     * @return the notation as a String
     */
    public String getNotationSEQ() {
        
        System.err.println("WARNING: getNotationSEQ(): not implemented yet.");

        return("");
    }

    /**
     * Generates the key notation (KEY) of this protein graph. Note that this notation differs
     * depending on the graph type: for all graphs but alpa(-only) and beta(-only) graphs, the SSE type is written
     * behind the distance.
     *
     *  Example: [5, 1x, -2] is an alpha or beta graph. Vertex 1 is connected to 6 (parallel, which is the default): "+5" ,
     *                                                  6 connected with 7 (antiparallel or mixed, marked by the "x"): "+1x" and
     *                                                  7 is connected to 5 (parallel again): "-2".
     *
     *
     *  An albe graph (or *lig graph) needs to add the SSE type to the notation, e.g.: [h, 3xh, 2e, -1l] is another graph,
     *  and the SSEs are of the following types: 1=>helix, 4=>helix, 6=>beta sheet, 5=>ligand.
     * 
     * * @return the notation as a String
     *
     */
    public String getNotationKEY(Boolean addSSEType) {

        if(this.isBifurcated()) {
            System.err.println("WARNING: #KEY notation not supported for bifurcated graphs. Check before requesting this.");
            return("");
        }

        if(! this.isConnected()) {
            System.err.println("WARNING: #KEY notation only supported for connected graphs. (All folding graphs are connected - is this a protein graph instead of a folding graph?)");
            return("");
        }
        
        System.err.println("WARNING: getNotationKEY(): not implemented yet.");

        return("");
    }
    
    /**
     * Returns the number of SSEs of type 'type' in this graph. Supported types are "HELIX", "SHEET", "LIGAND" and "OTHER"
     */
    public Integer numSSE(String type) {
        Integer num = 0;

        for(SSE s : this.sseList) {
            if(type.equals("HELIX")) {
                if(s.isHelix()) {
                    num++;
                }
            }
            else if(type.equals("SHEET")) {
                if(s.isBetaStrand()) {
                    num++;
                }
            }
            else if(type.equals("LIGAND")) {
                if(s.isLigandSSE()) {
                    num++;
                }
            }
            else if(type.equals("OTHER")) {
                if(s.isOtherSSE()) {
                    num++;
                }
            }
            else {
                System.err.println("ERROR: numSSE(): SSE type '" + type + "' not supported.");
                System.exit(-1);
            }
        }

        return(num);
    }

    /**
     * Determines all SSEs of type 'type' in this graph.
     * @return An ArrayList containing the indices of the SSEs.
     */
    public ArrayList<Integer> getAllSSEsOfType(String type) {
        ArrayList<Integer> sses = new ArrayList<Integer>();

        for(Integer i = 0; i < sseList.size(); i++) {
            if(type.equals("HELIX")) {
                if( (sseList.get(i)).isHelix() ) {
                    sses.add(i);
                }
            }
            else if(type.equals("SHEET")) {
                if( (sseList.get(i)).isBetaStrand() ) {
                    sses.add(i);
                }
            }
            else if(type.equals("LIGAND")) {
                if( (sseList.get(i)).isLigandSSE() ) {
                    sses.add(i);
                }
            }
            else if(type.equals("OTHER")) {
                if( (sseList.get(i)).isOtherSSE() ) {
                    sses.add(i);
                }
            }
            else {
                System.err.println("ERROR: getAllSSEsOfType(): SSE type '" + type + "' not supported.");
                System.exit(-1);
            }
        }

        return(sses);
    }

    /**
     * Determines all SSEs of type 'type' in this graph.
     * @return An ArrayList containing the indices of the SSEs.
     */
    public ArrayList<Integer> getAllSSEsOfTypeFromList(String type, ArrayList<Integer> sseIndices) {
        ArrayList<Integer> sses = new ArrayList<Integer>();

        for(Integer i = 0; i < sseIndices.size(); i++) {
            if(type.equals("HELIX")) {
                if( (sseList.get(sseIndices.get(i))).isHelix() ) {
                    sses.add(sseIndices.get(i));
                }
            }
            else if(type.equals("SHEET")) {
                if( (sseList.get(sseIndices.get(i))).isBetaStrand() ) {
                    sses.add(sseIndices.get(i));
                }
            }
            else if(type.equals("LIGAND")) {
                if( (sseList.get(sseIndices.get(i))).isLigandSSE() ) {
                    sses.add(sseIndices.get(i));
                }
            }
            else if(type.equals("OTHER")) {
                if( (sseList.get(sseIndices.get(i))).isOtherSSE() ) {
                    sses.add(sseIndices.get(i));
                }
            }
            else {
                System.err.println("ERROR: getAllSSEsOfTypeFromList(): SSE type '" + type + "' not supported.");
                System.exit(1);
            }
        }

        return(sses);
    }


    /**
     * Checks whether this graph contains a beta barrel.
     * @return true if it does, false otherwise
     */
    public Boolean containsBetaBarrel() {

        // Currently this implementation is just a heuristic. We assume that there is a beta barrel if
        // at least 4 connected beta sheets exists, all of which have exactly 2 neighbors which are beta sheets
        // and members of the same set of at least 4 beta sheets.
        ArrayList<Integer> allSheets = this.getAllSSEsOfType("SHEET");
        
        // at least 4 sheets required
        if(allSheets.size() < 4) {
            return(false);
        }

        ArrayList<Integer> neighList = new ArrayList<Integer>();
        ArrayList<Integer> betaNeighList = new ArrayList<Integer>();

        // Check neighbor conditions for all sheets. Note that we assume that all beta strands of a graph have to
        // be part of a beta barrel if such a barrel exists, which may be too harsh.
        for(Integer vertexIndex : allSheets) {
            neighList = this.neighborsOf(vertexIndex);
           
            // Beta strands which are part of a beta barrel obviously must have at least 2 neighbors that
            //  are beta strands.
            betaNeighList = getAllSSEsOfTypeFromList("SHEET", neighList);
            if( betaNeighList.size() < 2) {
                return(false);
            }            
        }

        // None of the checks proved that this is not a beta barrel, so we assume it is.
        return(true);
    }

    /**
     * Returns a string array of size 2. The first element contains the opening bracket for this graph's serial notation,
     * and the second the closing bracket.
     */
    public String[] getNotationBrackets() {
        String[] brackets = new String[2];

        // default brackets: "[" and "]"
        brackets[0] = "[";
        brackets[1] = "]";

        // non-bifurctaed graphs that contain beta barrels use "(" and ")"
        if(this.containsBetaBarrel()) {
            brackets[0] = "(";
            brackets[1] = ")";
        }

        // all bifurcated graphs use "{" and "}"
        if(this.isBifurcated()) {
            brackets[0] = "{";
            brackets[1] = "}";
        }

        return(brackets);
    }
    
    
    /**
     * Returns a static String that is the header for the plcc format.
     * @return the multi-line header string, including a label with PDB ID and graph type
     */
    private String getPlccFormatHeader() {
        String outString = "# This is the plcc SSE info file for the " + this.graphType + " graph of PDB entry " + this.pdbid + ", chain " + this.chainid + ".\n";
        outString += "# First character in a line indicates the line type ('#' => comment, '>' => meta data, '|' => SSE, '=' => contact).\n";
        //outString += "# Note on parsing this file: You can savely remove all whitespace from a line before splitting it.\n";
        
        return(outString);
    }

    /**
     * Returns the contents of the SSE list text file that explains the SSEs in the image. Used for the plcc graph file format. The String includes
     * multiple lines (one for each SSE) which are organized in fields separated by the field separator "|".
     * Each line contains the following fields in this order:
     *
     * PDB ID | chain ID | graph type | sequential SSE number in chain | SSE number in image | SSE type | DSSP start residue # | DSSP end residue # | PDB end residue ID (format: chain-res#-icode) | PDB start residue ID | AA sequence
     *
     * This function is not meant to be called externally anymore, use getGraphFilePLG() instead.
     * 
     *
     */
    private String getSSEListString() {
        String outString = "# SSEs follow in format '| PDB ID | chain ID | graph type | sequential SSE number in chain | SSE number in graph and image | SSE type | DSSP start residue # | DSSP end residue # | PDB end residue ID (format: chain-res#-icode) | PDB start residue ID | AA sequence'\n";

        SSE s = null;
        for(Integer i = 0; i < this.sseList.size(); i++) {
            s = this.sseList.get(i);
            outString += String.format("| %s | %s | %s | %d | %d | %s | %d | %d | %s | %s | %s \n", this.pdbid, this.chainid, this.graphType, s.getSSESeqChainNum(), (i + 1), s.getSseType(), s.getStartDsspNum(), s.getEndDsspNum(), s.getStartPdbResID(), s.getEndPdbResID(), s.getAASequence());
        }

        outString += "# Printed info on " + this.sseList.size() + " SSEs.\n";

        return(outString);
    }
    
    
    /**
     * Returns a multi-line String which represents this graph in our own plcc format, v2. All info required to draw the graph is included in the file format, but stuff like the atom coordinates etc is not.
     * Internally, this calls getSSEListString() and getContactListString() and returns their concatenated results (for historical reasons). See their java doc for more info on the format. It is really easy and also documented in the
     * output String itself (using comment lines) though.
     * 
     * @return a String representing this protein graph in plcc v2 format
     */
    public String toPlccGraphFormatString() {
        this.metadata.put("format_version", "2");
        return(this.getPlccFormatHeader() + this.getMetaDataString() + this.getSSEListString() + this.getContactListString());
    }
    
    
    /**
     * Returns a parse-able String representation of the meta data of this protein graph. Used for the plcc graph file format.
     * @return the meta data String (includes comment lines)
     */
    private String getMetaDataString() {
        String outString = "# The graph meta data follows in format '> key > value'.\n";
        
        HashMap<String, String> md = this.metadata;       
        
        for(String key : md.keySet()) {
            outString += "> " + key + " > " + (String)md.get(key) + "\n";
        }
        
        return(outString);
    }
    
    
    /**
     * Returns a (multi-line) string representing the contacts between the SSEs of this graph. Used for the plcc graph file format. The string is designed in a 
     * way that makes parsing it easy: each line contains info on a contact between a pair of SSEs in the following format:
     *   SSE1 = spatial_relation = SSE2
     * e.g., '1 = p = 3' means that SSE #1 is in contact with SSE #3, and they are parallel to each other. 
     * 
     * This function is not meant to be called externally anymore, use getGraphFilePLG() instead.
     * 
     */
    private String getContactListString() {
        String outString = "# The contacts between the SSEs follow in format '= SSE1 = contact_type = SSE2'. The SSEs are labeled by their position in this graph.\n";
        Integer numContacts = 0;
        
        for (Integer i = 0; i < this.sseList.size(); i++) {
            for (Integer j = (i + 1); j < this.sseList.size(); j++) {
                if(this.sseContactExistsPos(i, j)) {
                    outString += "= " + (i + 1) + " = " + SpatRel.getString(this.matrix[i][j]) + " = " + (j + 1) + "\n";
                    numContacts++;
                }
            }            
        }
        
        outString += "# Listed " + numContacts + " contacts. EOF.";
        
        return(outString);
    }
    


    /**
     * NOT IMPLEMENTED YET.
     * Returns the KEY notation of this vertex.
     * 
     */
    public String getNotationKEYforVertex(Integer x) {
        System.err.println("WARNING: getNotationKEYforVertex(): NOT IMPLEMENTED.");
        return("IMPLEMENT_ME");

    }

    /**
     * NOT IMPLEMENTED YET.
     * Returns the position of this vertex in the KEY notation.
     */
    public Integer getPositionKEYForVertex(Integer x) {
        System.err.println("WARNING: getPositionKEYforVertex(): NOT IMPLEMENTED.");
        return(0);
    }

    /**
     * Draws the protein graph image of this graph, writing the image in PNG format to the file 'filePath' (which should maybe end in ".png").
     * If 'nonProteinGraph' is true, this graph is considered a custom (=non-protein) graph and the color coding for vertices and edges is NOT used.
     * In that case, the graph is drawn black and white and the labels for the N- and C-termini are NOT drawn.
     * 
     * @param filePath the file system path where to write the graph image (without file extension and the dot before it)
     * @param nonProteinGraph whether the graph is a non-protein graph and thus does NOT contain information on the relative SSE orientation in the expected way. If so, it is drawn in gray scale because the color code become useless (true => gray scale, false => color).
     * @return whether the graph could be drawn and written to the file filePath
     */
    public Boolean drawProteinGraph(String filePath, Boolean nonProteinGraph) {

        
        Integer numVerts = this.numVertices();

        Boolean bw = nonProteinGraph;                                                  
        
        // All these values are in pixels
        // page setup
        PageLayout pl = new PageLayout(numVerts);        
        Position2D vertStart = pl.getVertStart();
        
        try {

            // ------------------------- Prepare stuff -------------------------
            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            BufferedImage bi = new BufferedImage(pl.getPageWidth(), pl.getPageHeight(), BufferedImage.TYPE_INT_ARGB);
            
            SVGGraphics2D ig2;
            
            
            //if(Settings.get("plcc_S_img_output_format").equals("SVG")) {                    
                // Apache Batik SVG library, using W3C DOM tree implementation
                // Get a DOMImplementation.
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                // Create an instance of org.w3c.dom.Document.
                String svgNS = "http://www.w3.org/2000/svg";
                Document document = domImpl.createDocument(svgNS, "svg", null);
                // Create an instance of the SVG Generator.
                ig2 = new SVGGraphics2D(document);
           // }
            //else {
            //    ig2 = (SVGGraphics2D)bi.createGraphics();
            //}
            
            ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // make background white
            ig2.setPaint(Color.WHITE);
            ig2.fillRect(0, 0, pl.getPageWidth(), pl.getPageHeight());
            ig2.setPaint(Color.BLACK);


            // prepare font
            Font font = pl.getStandardFont();
            ig2.setFont(font);
            FontMetrics fontMetrics = ig2.getFontMetrics();

            // ------------------------- Draw header -------------------------

            // check width of header string
            String proteinHeader = "The " + this.graphType + " graph of PDB entry " + this.pdbid + ", chain " + this.chainid + " [V=" + this.numVertices() + ", E=" + this.numSSEContacts() + "].";
            //Integer stringWidth = fontMetrics.stringWidth(proteinHeader);       // Should be around 300px for the text above
            Integer stringHeight = fontMetrics.getAscent();
            String sseNumberSeq;    // the SSE number in the primary structure, N to C terminus
            String sseNumberGraph;  // the SSE number in this graph, 1..(this.size)

            if(Settings.getBoolean("plcc_B_graphimg_header")) {
                ig2.drawString(proteinHeader, pl.headerStart.x, pl.headerStart.y);
            }

            // ------------------------- Draw the graph -------------------------
            
            // Draw the edges as arcs
            java.awt.Shape shape;
            Arc2D.Double arc;
            ig2.setStroke(new BasicStroke(2));  // thin edges
            Integer edgeType, leftVert, rightVert, leftVertPosX, rightVertPosX, arcWidth, arcHeight, arcTopLeftX, arcTopLeftY, spacerX, spacerY;
            for(Integer i = 0; i < this.sseList.size(); i++) {
                for(Integer j = i + 1; j < this.sseList.size(); j++) {

                    // If there is a contact...
                    if(this.containsEdge(i, j)) {

                        // determine edge type and the resulting color
                        edgeType = this.getContactType(i, j);
                        if(edgeType.equals(SpatRel.PARALLEL)) { ig2.setPaint(Color.RED); }
                        else if(edgeType.equals(SpatRel.ANTIPARALLEL)) { ig2.setPaint(Color.BLUE); }
                        else if(edgeType.equals(SpatRel.MIXED)) { ig2.setPaint(Color.GREEN); }
                        else if(edgeType.equals(SpatRel.LIGAND)) { ig2.setPaint(Color.MAGENTA); }
                        else { ig2.setPaint(Color.LIGHT_GRAY); }

                        if(bw) { ig2.setPaint(Color.LIGHT_GRAY); }      // for non-protein graphs

                        // determine the center of the arc and the width of its rectangle bounding box
                        if(i < j) { leftVert = i; rightVert = j; }
                        else { leftVert = j; rightVert = i; }
                        leftVertPosX = pl.getVertStart().x + (leftVert * pl.vertDist);
                        rightVertPosX = pl.getVertStart().x + (rightVert * pl.vertDist);

                        arcWidth = rightVertPosX - leftVertPosX;
                        arcHeight = arcWidth / 2;

                        arcTopLeftX = leftVertPosX;
                        arcTopLeftY = pl.getVertStart().y - arcHeight / 2;

                        spacerX = pl.vertRadius;
                        spacerY = 0;

                        // draw it                                                
                        arc = new Arc2D.Double(arcTopLeftX + spacerX, arcTopLeftY + spacerY, arcWidth, arcHeight, 0, 180, Arc2D.OPEN);
                        shape = ig2.getStroke().createStrokedShape(arc);
                        ig2.fill(shape);

                    }
                }
            }

            // Draw the vertices as circles
            Ellipse2D.Double circle;
            Rectangle2D.Double rect;
            ig2.setStroke(new BasicStroke(2));
            for(Integer i = 0; i < this.sseList.size(); i++) {
                
                // pick color depending on SSE type
                if(this.sseList.get(i).isHelix()) { ig2.setPaint(Color.RED); }
                else if(this.sseList.get(i).isBetaStrand()) { ig2.setPaint(Color.BLACK); }
                else if(this.sseList.get(i).isLigandSSE()) { ig2.setPaint(Color.MAGENTA); }
                else if(this.sseList.get(i).isOtherSSE()) { ig2.setPaint(Color.GRAY); }
                else { ig2.setPaint(Color.LIGHT_GRAY); }

                if(bw) { ig2.setPaint(Color.GRAY); }      // for non-protein graphs
                
                // draw a shape based on SSE type
                if(this.sseList.get(i).isBetaStrand()) {
                    // beta strands are black, filled squares
                    rect = new Rectangle2D.Double(vertStart.x + (i * pl.vertDist), vertStart.y, pl.vertDiameter, pl.vertDiameter);
                    ig2.fill(rect);
                    
                }
                else if(this.sseList.get(i).isLigandSSE()) {
                    // ligands are magenta circles (non-filled)
                    circle = new Ellipse2D.Double(vertStart.x + (i * pl.vertDist), vertStart.y, pl.vertDiameter, pl.vertDiameter);
                    //ig2.fill(circle);
                    ig2.setStroke(new BasicStroke(3));  // this does NOT get filled, so give it a thicker border
                    ig2.draw(circle);
                    ig2.setStroke(new BasicStroke(2));
                }
                else {
                    // helices and all others are filled circles (helices are red circles, all others are gray circles)
                    circle = new Ellipse2D.Double(vertStart.x + (i * pl.vertDist), vertStart.y, pl.vertDiameter, pl.vertDiameter);
                    ig2.fill(circle);                    
                }
            }
            
            // Draw the markers for the N-terminus and C-terminus if there are any vertices in this graph            
            ig2.setStroke(new BasicStroke(2));
            ig2.setPaint(Color.BLACK);
            
            if( ! bw) {
                if(this.sseList.size() > 0) {                    
                    ig2.drawString("N", vertStart.x - pl.vertDist, vertStart.y + 20);    // N terminus label
                    ig2.drawString("C", vertStart.x + this.sseList.size() * pl.vertDist, vertStart.y + 20);  // C terminus label
                }
            }
                        
            // Draw the vertex numbering into the footer
            // Determine the dist between vertices that will have their vertex number printed below them in the footer field
            Integer printNth = 1;
            if(this.sseList.size() > 9) { printNth = 1; }
            if(this.sseList.size() > 99) { printNth = 2; }
            if(this.sseList.size() > 999) { printNth = 3; }
            
            // line markers: S for sequence order, G for graph order
            Integer lineHeight = 40;            
            if(this.sseList.size() > 0) {                                            
                ig2.drawString("G", pl.getFooterStart().x - pl.vertDist, pl.getFooterStart().y);
                ig2.drawString("S", pl.getFooterStart().x - pl.vertDist, pl.getFooterStart().y + lineHeight);
            }
            else {
                ig2.drawString("(Graph has no vertices.)", pl.getFooterStart().x, pl.getFooterStart().y);
            }

            for(Integer i = 0; i < this.sseList.size(); i++) {
                // Draw label for every nth vertex
                if((i + 1) % printNth == 0) {
                    sseNumberGraph = "" + (i + 1);
                    sseNumberSeq = "" + (this.sseList.get(i).getSSESeqChainNum());
                    //stringWidth = fontMetrics.stringWidth(sseNumberSeq);
                    stringHeight = fontMetrics.getAscent();                                        
                    
                    ig2.drawString(sseNumberGraph, pl.getFooterStart().x + (i * pl.vertDist) + pl.vertRadius / 2, pl.getFooterStart().y + (stringHeight / 4));
                    ig2.drawString(sseNumberSeq, pl.getFooterStart().x + (i * pl.vertDist) + pl.vertRadius / 2, pl.getFooterStart().y + lineHeight + (stringHeight / 4));                    
                }
            }
            


            // all done, write the image to disk
            //if(Settings.get("plcc_S_img_output_format").equals("SVG")) {
                ig2.stream(new FileWriter(filePath),false);                
            //}
                
            if(Settings.get("plcc_S_img_output_format").equals("PNG")) {
                //ig2.drawImage(bi, null, 0, 0);  // Test
                //PNGTranscoder tc = new PNGTranscoder();
                //ImageIO.write(tc., "PNG", new File(filePath));
                //IO.convertSVGtoPNG(filePath, filePath + ".png", 0, 0, pl.getPageWidth(), pl.getPageHeight());
            }
            
            

        } catch (Exception e) {
            System.err.println("WARNING: Could not write image file for protein graph to file '" + filePath + "'.");
            return(false);
        }

        return(true);
    }

    
    /**
     * This function creates a connector between the 2D points (startX, startY) and (targetX, targetY). This connector is returned as a list of Shape
     * objects that can be painted on a Graphics2D canvas by stroking or filling them (e.g., G2Dinstance.fill(shapeInstance) or similar). The shapes
     * are created using the given Stroke (call G2Dinstance.getStroke() to use the current Stroke).
     * 
     * If the start and end points are on the same height (i.e., startY == targetY), the connector will look 
     * similar to an 'S' and will consist of a single shape (an arc forming a half circle). Otherwise it will
     * consist of 3 Shapes (two arcs and a line).
     * 
     * You can choose whether the connector should start upwards or downwards from (startX, startY) using the startUpwards parameter.
     * 
     * @param startX the x coordinate of the start point
     * @param startY the y coordinate of the start point
     * @param targetX the x coordinate of the end point
     * @param targetY the y coordinate of the end point
     * @param stroke the Stroke to use. You can get one from your Graphics2D instance.
     * @param startUpwards whether to start upwards from the 2D Point (startX, startY). If this is false, downwards is used instead.
     * @return a list of Shapes that can be painted on a G2D canvas.
     */
    public ArrayList<Shape> getArcConnector(Integer startX, Integer startY, Integer targetX, Integer targetY, Stroke stroke, Boolean startUpwards) {

        if(startY == targetY) {
            return(getSimpleArcConnector(startX, startY, targetX, stroke, startUpwards));            
        }
        else {
            return(getCrossoverArcConnector(startX, startY, targetX, targetY, stroke, startUpwards));
        }        
    }
    
    
    /**
     * The function that implements a simple half circle-shaped arc connector between the 2D points (startX, startY) and (targetX, targetY) in the
     * requested direction. Internal function, call the more general getArcConnector() function instead.
     * 
     * You can choose whether the connector should start upwards or downwards from (startX, startY) using the startUpwards parameter.
     * 
     *   upwards:                downwards:
     * 
     *      __                   s    t
     *     /  \                  |    |
     *    |    |                  \__/
     *    s    t                
     * 
     * @param startX the x coordinate of the start point
     * @param bothY the y coordinate of both the start point and the end point
     * @param targetX the x coordinate of the end point
     * @param stroke the Stroke to use. You can get one from your Graphics2D instance.
     * @param startUpwards whether to start upwards from the 2D Point (startX, startY). If this is false, downwards is used instead.
     * @return a list of Shapes that can be painted on a G2D canvas.
     */
    private ArrayList<Shape> getSimpleArcConnector(Integer startX, Integer bothY, Integer targetX, Stroke stroke, Boolean startUpwards) {
        
        ArrayList<Shape> parts = new ArrayList<Shape>();
        Integer leftVertPosX, rightVertPosX, arcWidth, arcHeight, vertStartY, arcTopLeftX, arcTopLeftY;
        
        // ensure left-right order
        if(startX < targetX) { 
            leftVertPosX = startX;
            rightVertPosX = targetX; }
        else
        { 
            leftVertPosX = targetX; 
            rightVertPosX = startX;
        }
        
        // stuff common for up/down
        vertStartY = bothY;
        arcWidth = rightVertPosX - leftVertPosX;
        arcHeight = arcWidth / 2;
        arcTopLeftX = leftVertPosX;
        arcTopLeftY = vertStartY - arcHeight / 2;
        
        Arc2D arc;
        if(startUpwards) {                                                           
            // create the Shape
            arc = new Arc2D.Double(arcTopLeftX, arcTopLeftY, arcWidth, arcHeight, 0, 180, Arc2D.OPEN);
        }
        else {
            arc = new Arc2D.Double(arcTopLeftX, arcTopLeftY, arcWidth, arcHeight, 180, 180, Arc2D.OPEN);
        }
        
        Shape shape = stroke.createStrokedShape(arc);
        // add it to the parts list
        parts.add(shape);
        return(parts);
    }
    
    
    /**
     * The function that implements a more complex 'S'-shaped arc connector between the 2D points (startX, startY) and (targetX, targetY) in the
     * requested direction. Internal function, call the more general getArcConnector() function instead.
     * 
     * This connector consists of 3 Shapes: two half-circles and a line (think of the letter 'S').
     * 
     * You can choose whether the connector should start upwards or downwards from (startX, startY) using the startUpwards parameter.
     * 
     * upwards:                downwards:
     *      __                     __
     *     /  \                   /   \
     *    |    |                  |    |
     *    s    |                  |    t
     *         |   t         s    |
     *         |   |         |    |
     *         \__/           \__/
     * 
     * @param startX the x coordinate of the start point
     * @param startY the y coordinate of the start point
     * @param targetX the x coordinate of the end point
     * @param targetY the y coordinate of the end point
     * @param stroke the Stroke to use. You can get one from your Graphics2D instance.
     * @param startUpwards whether to start upwards from the 2D Point (startX, startY). If this is false, downwards is used instead.
     * @return a list of Shapes that can be painted on a G2D canvas.
     */
    private ArrayList<Shape> getCrossoverArcConnector(Integer startX, Integer startY, Integer targetX, Integer targetY, Stroke stroke, Boolean startUpwards) {
        
        Integer upwards = 0;
        Integer downwards = 180;
        
        ArrayList<Shape> parts = new ArrayList<Shape>();
        Integer leftVertPosX, rightVertPosX, bothArcsSumWidth, bothArcsSumHeight, vertStartY, leftArcHeight, leftArcWidth, rightArcHeight, rightArcWidth;
        Integer leftArcUpperLeftX, leftArcUpperLeftY, centerBetweenBothArcsX, centerBetweenBothArcsY, leftArcEndX, leftArcEndY, rightArcEndX, rightArcEndY;
        Integer leftArcLowerRightX, leftArcLowerRightY, leftArcUpperRightX, leftArcUpperRightY;
        Integer rightArcLowerRightX, rightArcLowerRightY, rightArcUpperRightX, rightArcUpperRightY, rightArcUpperLeftX, rightArcUpperLeftY;
        Integer lineStartX, lineStartY, lineEndX, lineEndY, lineLength;
        
        
        // ensure left-right order
        if(startX < targetX) { 
            leftVertPosX = startX;
            rightVertPosX = targetX; }
        else
        { 
            leftVertPosX = targetX; 
            rightVertPosX = startX;
        }
        
        // stuff common for up/down
        vertStartY = startY;
        lineLength = Math.abs(startY - targetY);
        
        bothArcsSumWidth = rightVertPosX - leftVertPosX;
        leftArcWidth = rightArcWidth = bothArcsSumWidth / 2;
        
        bothArcsSumHeight = bothArcsSumWidth / 2;
        leftArcHeight = rightArcHeight = bothArcsSumHeight / 2;
        
        centerBetweenBothArcsX = rightVertPosX - (bothArcsSumWidth / 2);    // this is where the upright line is created
        centerBetweenBothArcsY = vertStartY;
        
        leftArcUpperLeftX = leftVertPosX;
        leftArcUpperLeftY = vertStartY - bothArcsSumHeight / 2;
        leftArcLowerRightX = leftArcUpperLeftX + leftArcWidth;
        leftArcLowerRightY = leftArcUpperLeftY + leftArcHeight;
        leftArcUpperRightX = leftArcUpperLeftX + leftArcWidth;
        leftArcUpperRightY = leftArcUpperLeftY;
        
        // everything computed, now start to create the shapes based on the required arc starting angle (up or down)
        Shape shape; Arc2D arc;
        
        if(startUpwards) {                  
            
            // left arc ends starts in lower right corner and ens in lower right corner of its bounding rectangle (looks like an inverted 'U')
            leftArcEndX = leftArcLowerRightX;
            leftArcEndY = leftArcLowerRightY;
            
            // create the Shape for the left arc, which starts upwards in this case
            arc = new Arc2D.Double(leftArcUpperLeftX, leftArcUpperLeftY, leftArcWidth, leftArcHeight, upwards, 180, Arc2D.OPEN);
            shape = stroke.createStrokedShape(arc);
            parts.add(shape);
            
            // create the Shape for the line, which goes straight down
            lineStartX = leftArcEndX;
            lineStartY = leftArcEndY;
            lineEndX = leftArcEndX;
            lineEndY = leftArcEndY + lineLength;    // the line goes downwards, therefor the '+' in this case!
            Line2D l = new Line2D.Double(lineStartX, lineStartY, lineEndX, lineEndY);
            shape = stroke.createStrokedShape(l);
            parts.add(shape);
            
            // create the Shape for the right arc. it starts in upper left corner and ends in upper right corner of its bounding rectangle (looks like a 'U').
            rightArcUpperLeftX = lineEndX;
            rightArcUpperLeftY = lineEndY;
            arc = new Arc2D.Double(rightArcUpperLeftX, rightArcUpperLeftY, rightArcWidth, rightArcHeight, downwards, 180, Arc2D.OPEN);
            shape = stroke.createStrokedShape(arc);
            parts.add(shape);
        }
        else {
            // left arc ends in upper right corner of its bounding rectangle
            leftArcEndX = leftArcUpperRightX;
            leftArcEndY = leftArcUpperRightY;
            
            // create the Shape for the left arc, which starts downwards in this case
            arc = new Arc2D.Double(leftArcUpperLeftX, leftArcUpperLeftY, leftArcWidth, leftArcHeight, downwards, 180, Arc2D.OPEN);
            shape = stroke.createStrokedShape(arc);
            parts.add(shape);
            
            // create the Shape for the line, which goes straight up
            lineStartX = leftArcEndX;
            lineStartY = leftArcEndY;
            lineEndX = leftArcEndX;
            lineEndY = leftArcEndY - lineLength;    // the line goes downwards, therefor the '-' in this case!
            Line2D l = new Line2D.Double(lineStartX, lineStartY, lineEndX, lineEndY);
            shape = stroke.createStrokedShape(l);
            parts.add(shape);
            
            // create the Shape for the right arc. it starts in upper left corner and ends in upper right corner of its bounding rectangle (looks like a 'U').
            rightArcUpperLeftX = lineEndX;
            rightArcUpperLeftY = lineEndY;
            arc = new Arc2D.Double(rightArcUpperLeftX, rightArcUpperLeftY, rightArcWidth, rightArcHeight, upwards, 180, Arc2D.OPEN);
            shape = stroke.createStrokedShape(arc);

        }
        return(parts);
    }



    /**
     * Draws the current folding graph in the notation 'notation' (which is one of "KEY", "SEQ", "ADJ" or "RED") and writes the image
     * in PNG format to file 'filePath'.
     * @param filePath the output path of the image
     * @param notation the notation to use. valid notation strings are 'KEY', 'ADJ', 'RED' and 'SEQ'.
     * @return true if it worked out, false otherwise 
     */
    public Boolean drawFoldingGraph(String notation, String filePath) {

        // Folding graphs are the connected components of a protein graph, they are thus connected.
        if( ! this.isConnected()) {
            System.err.println("ERROR: drawFoldingGraph(): Can't use folding graph-specific funtion on protein graph (this graph is not connected).");
            return(false);
        }

        // They should also be marked as folding graphs.
        if( ! this.isFoldingGraph()) {
            System.err.println("WARNING: drawFoldingGraph(): This graph is connected but it is not declared to be a folding graph. Still trying to draw.");
        }

        // Check minimum size
        if(this.numVertices() < Settings.getInteger("plcc_I_min_fgraph_size_draw")) {
            System.out.println("INFO: drawFoldingGraph(): Ignoring folding graph of size " + this.numVertices() + ", minimum size is " + Settings.getInteger("plcc_I_min_fgraph_size_draw") + ".");
            return(false);
        }

        // OK, let's go.
        if(notation.equals("KEY")) {
            return(this.drawFoldingGraphKEY(filePath));
        }
        else if(notation.equals("ADJ")) {
            return(this.drawFoldingGraphADJ(filePath));
        }
        else if(notation.equals("RED")) {
            return(this.drawFoldingGraphRED(filePath));
        }
        else if(notation.equals("SEQ")) {
            return(this.drawFoldingGraphSEQ(filePath));
        }
        else {
            System.err.println("ERROR: Folding graph notation '" + notation + "' invalid.");
            return(false);
        }
    }
    
    
    /**
     * Draws the ADJ notation image of this graph, writing the image in PNG format to the file 'filePath' (which should maybe end in ".png").
     * The ADJ notation is the same style we use for the PGs. It is similar to the RED style but includes all SSEs of the PG, not only
     * those of the current FG.
     * @param filePath the output path of the image
     * @return true if it worked out, false otherwise 
     */
    private Boolean drawFoldingGraphADJ(String filePath) {
        //System.err.println("WARNING: Folding graph notation 'ADJ' not implemented yet, drawing RED instead.");
        //return(this.drawFoldingGraphRED(filePath));
        return(false);
    }
    
    
    /**
     * Draws the SEQ notation image of this graph, writing the image in PNG format to the file 'filePath' (which should maybe end in ".png").
     * @param filePath the output path of the image
     * @return true if it worked out, false otherwise 
     */
    private Boolean drawFoldingGraphSEQ(String filePath) {
        //System.err.println("WARNING: Folding graph notation 'SEQ' not implemented yet.");
        return(false);
    }

    
    
    /**
     * Draws the KEY notation image of this graph, writing the image in PNG format to the file 'filePath' (which should maybe end in ".png").
     * @param filePath the output path of the image
     * @return true if it worked out, false otherwise
     */
    private Boolean drawFoldingGraphKEY(String filePath) {
        
        if(this.isBifurcated()) {
            if(Settings.getInteger("plcc_I_debug_level") > 0) {
                System.err.println("NOTE: ProtGraph.drawFoldingGraphKEY(): Called for bifurcated graph but KEY notation not supported for such graphs, skipping.");
            }
            return(false);
        }
        
        // Prepare the vertex order
        ArrayList<Integer> spatOrder = this.getSpatialOrderingOfVertexIndices();
        if(spatOrder.size() != this.size) {
            if(Settings.getInteger("plcc_I_debug_level") > 0) {
                System.err.println("NOTE: ProtGraph.drawFoldingGraphKEY(): Could not determine a valid spatial vertex ordering (" + spatOrder.size() + "/" + this.size + "), graph bifurcated?");
            }
            return(false);
        }

        // All these values are in pixels
        Integer numVert = this.numVertices();
        final Integer NONE = 0;     // no direction (because this SSE-pair does not appear in the list)
        final Integer UP = 1;       // direction: upwards (for the SSE symbols, e.g. arrows). Note that the direction represents the spatial relation between this SSE and its predecessor (i.e., an SSE pair), it is NOT a property of a single SSE.
        final Integer DOWN = 2;     // direction: downwards

        // page setup
        Integer marginLeft = 40;
        Integer marginRight = 40;
        Integer marginTop = 40;
        Integer marginBottom = 40;
        Integer minImgHeight = 80;

        // The header that contains the text describing of the graph.
        Integer headerHeight = 40;

        // The footer contains the vertex numbering.
        Integer footerHeight = 40;

        // Where to start drawing
        Integer headerStartX = marginLeft;
        Integer headerStartY = marginTop;
        Integer imgStartX = marginLeft;
        Integer imgStartY = marginTop + headerHeight;
        Integer footerStartX = marginLeft;


        // drawing of objects
        Integer vertDist = 50;                  // distance between (centers of) vertices in the drawing
        Integer vertRadius = 10;                // the width of a drawn vertex (radius around its center)
        Integer vertHeight = 80;                // height of vertex element graphics (arrow height)
        Integer vertWidth = 40;                 // height of vertex graphics (arrow width)
        Integer vertDiameter = 2 * vertRadius;

        // Determine the maximal arc height. The height of an arc is half of its width (see the part where the arcs
        //  are drawn below), but the arcHeight really describes the height of the full circle this half-circle is
        //  a part of. So we gotta divide by 2 again; the maximal height is thus a quarter of the maximal distance between
        //  vertices in the image.
        Integer maxVertDist = this.numVertices() * vertDist;
        Integer maxArcHeight = maxVertDist / 4;

        // the image area: the part where the vertices and arcs are drawn
        Integer imgWidth = numVert * vertDist + 2 * vertRadius;
        Integer imgHeight = maxArcHeight + vertRadius;
        if(imgHeight < minImgHeight) { imgHeight = minImgHeight; }

        // The text drawn in the proteinHeader is about 300px wide with the current text. We assume a maximum of
        //  500 here and make this the minimum width of the content frame.
        Integer headerTextWidth = 500;
        if(imgWidth < headerTextWidth) {
            imgWidth = headerTextWidth;
        }

        // where to start drawing the vertices
        Integer vertStartX = imgStartX;
        Integer vertStartY = imgStartY + maxArcHeight;
        Integer footerStartY = vertStartY + vertRadius + 40;

        // putting it all together
        Integer pageWidth = marginLeft + imgWidth + marginRight;
        Integer pageHeight = marginTop + headerHeight + imgHeight + footerHeight + marginBottom;               

        try {

            // ------------------------- Prepare stuff -------------------------
            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            BufferedImage bi = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig2 = bi.createGraphics();
            ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // make background white
            ig2.setPaint(Color.WHITE);
            ig2.fillRect(0, 0, pageWidth, pageHeight);
            ig2.setPaint(Color.BLACK);


            // prepare font
            Font font = new Font("TimesRoman", Font.PLAIN, 18);
            ig2.setFont(font);
            FontMetrics fontMetrics = ig2.getFontMetrics();

            // ------------------------- Draw header -------------------------

            // check width of header string
            String proteinHeader = "The " + this.graphType + " graph of PDB entry " + this.pdbid + ", chain " + this.chainid + " in KEY notation.";
            Integer stringWidth = fontMetrics.stringWidth(proteinHeader);       // Should be around 300px for the text above
            Integer stringHeight = fontMetrics.getAscent();
            String sseNumber;

            if(Settings.getBoolean("plcc_B_graphimg_header")) {
                ig2.drawString(proteinHeader, headerStartX, headerStartY);
            }

            // ------------------------- Draw the graph -------------------------

            // Draw the edges as arcs
            java.awt.Shape shape;
            Arc2D.Double arc;
            Integer edgeType, connCenterX, connCenterY, leftVert, rightVert, leftVertSpat, rightVertSpat, leftVertPosX, leftVertPosY, rightVertPosX, rightVertPosY, connWidth, connHeight, connTopLeftX, connTopLeftY, spacerX, spacerY;
            
            // determine the headings of the vertices in the image. if two adjacent SSEs are parallel, they point in the same direction in the image. if they are antiparallel, they point into different directions.
            Integer[] headingsSpatOrder = new Integer[this.size];    // the heading of the vertex in the image (UP or DOWN). This is in the order of spatOrder variable, not the the original vertex list in the SSEList variable.
            Integer[] headingsSeqOrder = new Integer[this.size];
            headingsSpatOrder[0] = UP;  // heading of the 1st vertex is up by definition (it has no predecessor)
            
            if(this.size > 1) {
                
                for(Integer i = 1; i < this.size; i++) {
                    Integer sseIndex = spatOrder.get(i);
                    Integer lastsseIndex = spatOrder.get(i - 1);
                    Integer spatRel = this.getContactType(sseIndex, lastsseIndex);
                    
                    if(spatRel == SpatRel.PARALLEL || spatRel == SpatRel.LIGAND) {
                        headingsSpatOrder[i] = headingsSpatOrder[i-1];
                    }
                    else if(spatRel == SpatRel.NONE) {
                        headingsSpatOrder[i] = headingsSpatOrder[i-1];    // whatever
                        System.err.println("ERROR: Vertices without contact are considered neighbors in the graph.");
                        System.exit(1);
                    }
                    else {
                        // all other spatial relations, e.g. SpatRel.ANTIPARALLEL
                        headingsSpatOrder[i] = (headingsSpatOrder[i-1] == UP ? DOWN : UP);
                    }                
                }
                
                // copy from spatOrder to seqOrder array
                Integer seqIndex;
                for(Integer i = 0; i < this.size; i++) {
                    seqIndex = spatOrder.get(i);
                    headingsSeqOrder[seqIndex] = headingsSpatOrder[i];
                }
            }                                    
            
            // now draw the connectors
            Integer iSpatIndex, jSpatIndex;
            Boolean startUpwards;
            if(this.numEdges() > 0) {                          
                for(Integer i = 0; i < this.sseList.size(); i++) {
                    for(Integer j = i + 1; j < this.sseList.size(); j++) {

                        // If there is a contact...
                        if(this.containsEdge(i, j)) {

                            // determine edge type and the resulting color
                            edgeType = this.getContactType(i, j);
                            if(edgeType.equals(SpatRel.PARALLEL)) { ig2.setPaint(Color.RED); }
                            else if(edgeType.equals(SpatRel.ANTIPARALLEL)) { ig2.setPaint(Color.BLUE); }
                            else if(edgeType.equals(SpatRel.MIXED)) { ig2.setPaint(Color.GREEN); }
                            else if(edgeType.equals(SpatRel.LIGAND)) { ig2.setPaint(Color.MAGENTA); }
                            else { ig2.setPaint(Color.LIGHT_GRAY); }

                            // determine the center of the arc and the width of its rectangle bounding box
                            iSpatIndex = spatOrder.get(i);
                            jSpatIndex = spatOrder.get(j);
                            if(iSpatIndex < jSpatIndex) { leftVertSpat = iSpatIndex; leftVert = i; rightVertSpat = jSpatIndex; rightVert = j;}
                            else { leftVertSpat = jSpatIndex; leftVert = j; rightVertSpat = iSpatIndex; rightVert = i;}
                            leftVertPosX = vertStartX + (leftVertSpat * vertDist);
                            rightVertPosX = vertStartX + (rightVertSpat * vertDist);

                            connWidth = rightVertPosX - leftVertPosX;
                            connHeight = connWidth / 2;

                            connCenterX = rightVertPosX - (connWidth / 2);
                            connCenterY = vertStartY;

                            connTopLeftX = leftVertPosX;
                            connTopLeftY = vertStartY - connHeight / 2;

                            spacerX = vertWidth;
                            spacerY = 0;

                            
                            // Determine the y axis positions where the connector should start (at the left vertex) and end (at the right vertex). This depends
                            //  on whether the respective vertex points upwards or downwards.
                            if(headingsSeqOrder[leftVert] == UP) {
                                // the left vertex points upwards, so the arc should start at its top
                                leftVertPosY = vertStartY - vertHeight;
                                startUpwards = true;
                            }
                            else {
                                // the left vertex points downwards, so the arc should start at its bottom
                                leftVertPosY = vertStartY;
                                startUpwards = false;
                            }
                            
                            if(headingsSeqOrder[rightVert] == UP) {
                                // the left vertex points upwards, so the arc should start at its top
                                rightVertPosY = vertStartY - vertHeight;
                            }
                            else {
                                // the left vertex points downwards, so the arc should start at its bottom
                                rightVertPosY = vertStartY;
                            }
                            
                            
                            // draw it
                            ig2.setStroke(new BasicStroke(2));
                            ArrayList<Shape> connShapes = this.getArcConnector(leftVertPosX, leftVertPosY, rightVertPosX, rightVertPosY, ig2.getStroke(), startUpwards);
                            for(Shape s : connShapes) {
                                ig2.draw(s);
                            }                                                        
                        }
                    }
                }
            }


            // Draw the vertices as arrows or barrels (depending on the type)
            Polygon p;
            for(Integer i = 0; i < this.sseList.size(); i++) {
                if(this.sseList.get(i).isHelix()) { ig2.setPaint(Color.RED); }
                else if(this.sseList.get(i).isBetaStrand()) { ig2.setPaint(Color.BLACK); }
                else if(this.sseList.get(i).isLigandSSE()) { ig2.setPaint(Color.MAGENTA); }
                else if(this.sseList.get(i).isOtherSSE()) { ig2.setPaint(Color.GRAY); }
                else { ig2.setPaint(Color.LIGHT_GRAY); }
                
                // draw it
                ig2.setStroke(new BasicStroke(1));
                
                if(this.sseList.get(i).isHelix()) {
                    p = getDefaultArrowPolygon(vertStartY - vertHeight, vertStartX + (i * vertDist), vertStartY);
                    
                }
                else {
                    p = getDefaultBarrelPolygon(vertStartY - vertHeight, vertStartX + (i * vertDist), vertStartY);
                }
                
                shape = ig2.getStroke().createStrokedShape(p);
                
                if(headingsSpatOrder[i] == DOWN) { ig2.rotate(180); }
                ig2.draw(shape);
                if(headingsSpatOrder[i] == DOWN) { ig2.rotate(-180); }
            }
            
            

            // Draw the vertex numbering into the footer
            font = new Font("TimesRoman", Font.PLAIN, 18);
            ig2.setFont(font);
            fontMetrics = ig2.getFontMetrics();
            ig2.setPaint(Color.BLACK);

            // Determine the dist between vertices that will have their vertex number printed below them in the footer field
            Integer printNth = 1;
            if(this.sseList.size() > 9) { printNth = 1; }
            if(this.sseList.size() > 99) { printNth = 2; }
            if(this.sseList.size() > 999) { printNth = 3; }

            for(Integer i = 0; i < this.sseList.size(); i++) {
                // Draw label for every 2nd vertex
                if((i + 1) % printNth == 0) {
                    sseNumber = "" + (spatOrder.get(i) + 1);
                    stringWidth = fontMetrics.stringWidth(sseNumber);
                    stringHeight = fontMetrics.getAscent();
                    ig2.drawString(sseNumber, footerStartX + (i * vertDist) + vertRadius / 2, footerStartY + (stringHeight / 4));
                }
            }



            // all done, write the image to disk
            ImageIO.write(bi, "PNG", new File(filePath));

        } catch (Exception e) {
            System.err.println("WARNING: Could not write image file for graph to file '" + filePath + "'.");
            return(false);
        }

        return(true);
    }


    /**
     * Draws this folding graph in reduced description (only SSEs of this FG count), writing the image file to 'filePath'.
     * @param filePath the output path of the image
     * @return true if it worked out, false otherwise
     */
    private Boolean drawFoldingGraphRED(String filePath) {
        return(this.drawProteinGraph(filePath, false));
    }


   /**
   * DEPRECATED: use getArrowPolygon() instead! 
   * Draws a simply arrow (3 lines) on the given Graphics2D context. Not used anymore.
   * @param g The Graphics2D context to draw on
   * @param x The x location of the "tail" of the arrow
   * @param y The y location of the "tail" of the arrow
   * @param xx The x location of the "head" of the arrow
   * @param yy The y location of the "head" of the arrow
   */
    @Deprecated private void drawArrow( Graphics2D g, int tailX, int tailY, int headX, int headY ) {
        
        //DEPRECATED: use getArrowPolygon() instead!
        
        float arrowWidth = 10.0f ;
        float theta = 0.423f ;
        int[] xPoints = new int[ 3 ] ;
        int[] yPoints = new int[ 3 ] ;
        float[] vecLine = new float[ 2 ] ;
        float[] vecLeft = new float[ 2 ] ;
        float fLength;
        float th;
        float ta;
        float baseX, baseY ;

        xPoints[ 0 ] = headX ;
        yPoints[ 0 ] = headY ;

        // build the line vector
        vecLine[ 0 ] = (float)xPoints[ 0 ] - tailX ;
        vecLine[ 1 ] = (float)yPoints[ 0 ] - tailY ;

        // build the arrow base vector - normal to the line
        vecLeft[ 0 ] = -vecLine[ 1 ] ;
        vecLeft[ 1 ] = vecLine[ 0 ] ;

        // setup length parameters
        fLength = (float)Math.sqrt( vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1] ) ;
        th = arrowWidth / ( 2.0f * fLength ) ;
        ta = arrowWidth / ( 2.0f * ( (float)Math.tan( theta ) / 2.0f ) * fLength ) ;

        // find the base of the arrow
        baseX = ( (float)xPoints[ 0 ] - ta * vecLine[0]);
        baseY = ( (float)yPoints[ 0 ] - ta * vecLine[1]);

        // build the points on the sides of the arrow
        xPoints[ 1 ] = (int)( baseX + th * vecLeft[0] );
        yPoints[ 1 ] = (int)( baseY + th * vecLeft[1] );
        xPoints[ 2 ] = (int)( baseX - th * vecLeft[0] );
        yPoints[ 2 ] = (int)( baseY - th * vecLeft[1] );

        g.drawLine( tailX, tailY, (int)baseX, (int)baseY ) ;
        g.fillPolygon( xPoints, yPoints, 3) ;
    }
  
    
    /**
     * Creates an outlined arrow (7 lines) on the given Graphics2D context, using the Polygon class. The arrow points straight up, thus headX is = tailX and does not have to be supplied.
     * You can transform this is you want to change its angle. It's a shape so you can stroke or fill it, too.
     * You should also check out getDefaultArrowPolygon() for an easy way to get arrows with the same width, length and head length.
     *
     * @param tailX The x location of the center of the "tail" of the arrow
     * @param tailY The y location of the center of the "tail" of the arrow
     * @param headY The y location of the "head" of the arrow
     * @param widthTail The width of the arrow at the tail
     * @param widthHead The width of the arrow at the broadest part of the head
     */
    private Polygon getArrowPolygon(int headY, int tailX, int tailY, int widthTail, int widthHead, int lengthHead) {
        int[] xPoints = new int[7] ;
        int[] yPoints = new int[7] ;
     
        // Create all points of the arrow.
        
        //Start at the left point of the tail and add the other points counter-clockwise.
        xPoints[0] = tailX - (widthTail / 2);
        yPoints[0] = tailY;
        
        // The right point of the tail.
        xPoints[1] = tailX + (widthTail / 2);
        yPoints[1] = tailY;
        
        // The right point of the start of the head.
        xPoints[2] = tailX + (widthTail / 2);
        yPoints[2] = headY + lengthHead;
        
        // The right-most point of the head.
        xPoints[3] = tailX + (widthHead / 2);
        yPoints[3] = headY + lengthHead;
        
        // The arrow tip
        xPoints[4] = tailX;
        yPoints[4] = headY;
        
        // The left-most point of the head.
        xPoints[5] = tailX - (widthHead / 2);
        yPoints[5] = headY + lengthHead;
        
        // The left point of the start of the head.
        xPoints[6] = tailX - (widthTail / 2);
        yPoints[6] = headY + lengthHead;
        
        // The line closing the polygon (between the last we just set and the first point) is automatically inserted to close the Polygon
        return(new Polygon(xPoints, yPoints, 7));     
    }
    
    
    /**
     * Creates an outlined barrel (4 lines) on the given Graphics2D context, using the Polygon class. The barrel points straight up.
     * You can transform this is you want to change its angle. It's a shape so you can stroke or fill it, too.
     * You should also check out getDefaultBarrelPolygon() for an easy way to get barrels with the same width and length.
     *
     * @param tailX The x location of the center of the "tail" of the barrel
     * @param tailY The y location of the center of the "tail" of the barrel
     * @param headX The x location of the "head" of the barrel
     * @param headY The y location of the "head" of the barrel
     * @param width The width of the barrel
     */
    private Polygon getBarrelPolygon(int headX, int headY, int tailX, int tailY, int width) {
        
        int numPoints = 4;
        
        int[] xPoints = new int[numPoints] ;
        int[] yPoints = new int[numPoints] ;
     
        // Create all points of the arrow.
        
        //Start at the left point of the tail and add the other points counter-clockwise.
        xPoints[0] = tailX - (width / 2);
        yPoints[0] = tailY;
        
        // The right point of the tail.
        xPoints[1] = tailX + (width / 2);
        yPoints[1] = tailY;
        
        // The right point of the head.
        xPoints[2] = headX + (width / 2);
        yPoints[2] = headY;
        
        // The left point of the head.
        xPoints[3] = headX - (width / 2);
        yPoints[3] = headY;
                
        // The line closing the polygon (between the last we just set and the first point) is automatically inserted to close the Polygon
        return(new Polygon(xPoints, yPoints, numPoints));     
    }
    
    
    /**
     * Just a helper function that sets default values for the width of the barrel.
     * See getBarrelPolygon() for details on the other parameters.
     */
    private Polygon getDefaultBarrelPolygon(int headY, int bothX, int tailY) {
        
        int defaultWidth = 10;
        
        return(getBarrelPolygon(bothX, headY, bothX, tailY, defaultWidth));
    }
    

    
    /**
     * Just a helper function that sets default values for the width of the arrow. See the first 3 parameters of the drawOutlinedArrow() function for parameter explanation. 
     */
    private Polygon getDefaultArrowPolygon(int headY, int bothX, int tailY) {
        
        int defaultWidthTail = 10;
        int defaultWidthHead = 20;
        int defaultLengthHead = 20;
        
        return(getArrowPolygon(headY, bothX, tailY, defaultWidthTail, defaultWidthHead, defaultLengthHead));
    }
    
    
    /**
     * Uses Dijkstra's algorithm to compute the distance matrix of this graph, i.e. all pairwise distances between vertices.
     * Writes the results into distMatrix[][].
     */
    public void calculateDistancesWithinGraph() {
        
        // clear or init data
        for(Integer i = 0; i < size; i++) {
            for(Integer j = 0; j < size; j++) {
                distMatrix[i][j] = Integer.MAX_VALUE;
            }
        }
        
        // compute paths for all vertices
        for(Integer i = 0; i < this.size; i++) {
            computeAllPairwiseDistancesForVertex(i);            
        }       
        
        this.distancesCalculated = true;
    }
    
    
    /**
     * A wrapper around computePaths() which only fills out the distance matrix for the vertex sourceVertex. Note that you need to
     * call this for all vertices in order to get the complete distance matrix.
     * @param sourceVertex the source vertex for which the distances to all other vertices should be computed and inserted into distMatrix.
     */
    private void computeAllPairwiseDistancesForVertex(Integer sourceVertex) {
        computePaths(sourceVertex, sourceVertex);
    }
    
    
    /**
     * A wrapper around computePaths() which returns the path from source to target.
     * @param source the source vertex
     * @param target the target vertex
     * @return the path as an ArrayList of the vertices in the path (in correct order from source to target, of course)
     */
    private ArrayList<Integer> getPathBetweenVertices(Integer source, Integer target) {
        return(computePaths(source, target));
    }
    
    
    /**
     * Sets the metadata for this graph.
     * @param md the meta data Hashmap to assign to this graph.
     */
    public void setMetaData(HashMap<String, String> md) {
        this.metadata = md;
    }
    
    
    /**
     * Part of Dijkstra's algorithm, calculates the distances to all other vertices for a single source vertex.
     * Called by calculateDistancesWithinGraph() for each vertex.
     * This function can also return the shortest path to a target vertex along the way. If you do not need this
     * and only want the distance matrix to be filled, you can make 'targetVertex' an arbitrary value (e.g., the same as
     * sourceVertex) and ignore the returned ArrayList.
     * @param sourceVertex the source vertex. The distances to all the other vertices will be computed for this vertex.
     * @param targetVertex the target vertex. A path to this vertex will be in the returned ArrayList. If you don't need this, just set targetVertex to the 
     * same value as sourceVertex and ignore the resulting (empty) ArrayList.
     * @return an ArrayList containing the shortest path from sourceVertex to targetVertex, described by the indices of all vertices of the path.
     */    
    private ArrayList<Integer> computePaths(Integer sourceVertex, Integer targetVertex) {
        
        //System.out.println("DEBUG: Computing distances from vertex " + sourceVertex + " to all other vertices.");
        
        // init stuff
        Integer src = sourceVertex;
        Integer[] minDistances = new Integer[this.size];
        Integer[] previous = new Integer[this.size];        // This allows us to determine the path from sourceVertex to a target vertex at
                                                            // the end of this function (take the target vertex, than follow the 'previous' values
                                                            // until it is <0. Do not forget to invert this path in the end (to get source=>target instead of target=>source).
        
        ArrayList<Integer> pathToTarget = new ArrayList<Integer>();
        
        for(Integer i = 0; i < this.size; i++) { minDistances[i] = Integer.MAX_VALUE; previous[i] = -1; }
        
        // go
        minDistances[src] = 0;      // the distance of the vertex to itself is 0
        PriorityQueue<PriorityVertex> vertexQueue = new PriorityQueue<PriorityVertex>();
  	vertexQueue.add(new PriorityVertex(src, minDistances[src]));
        
        while (!vertexQueue.isEmpty()) {
            
  	    PriorityVertex u = vertexQueue.poll();
            
            assert u.minDistance == minDistances[u.index] : "ERROR: ProtGraph.computePaths(): minDistances[u.index] and u.minDistance inconsistent.";
  
            // Visit each edge exiting u...
            ArrayList<PriorityVertex> neighborsOfU = new ArrayList<PriorityVertex>();
            for(Integer index : adjLists.get(u.index)) {
               neighborsOfU.add(new PriorityVertex(index, minDistances[index])); 
            }
            
            //System.out.println("DEBUG: Vertex " + u + " has " + neighborsOfU.size() + " neighbors.");
            
            
            for (PriorityVertex neighborOfU : neighborsOfU) {
                
                // ... compute their distance ...                
                Integer distanceThroughU = minDistances[u.index] + 1;     // That vertex is a neighbor of u, so the distance to it through u is the distance to u + 1.
                                                                    // Note that during the first run, u==sourceVertex, so minDinstaces[u] == 0.
                
  		if (distanceThroughU < minDistances[neighborOfU.index]) {
                    
                    //System.out.println("DEBUG: Updated minDistance of vertex " + neighborOfU + " to source vertex " + sourceVertex + ": new value via vertex " + u + " is " + distanceThroughU + ".");
                    
  		    vertexQueue.remove(neighborOfU);
  
  		    minDistances[neighborOfU.index] = distanceThroughU;   // this is what we are after
                    neighborOfU.minDistance = distanceThroughU;             // gotta keep track of both spots
  		    previous[neighborOfU.index] = u.index;
                    
                    // ...and re-add this vertex to the end of the queue
  		    vertexQueue.add(neighborOfU);
  		}
                
                assert neighborOfU.minDistance == minDistances[neighborOfU.index] : "ERROR: ProtGraph.computePaths(): minDistances[neighborOfU.index] and neighborOfU.minDistance inconsistent.";
            }
        }
        
        // fill in the distMatrix from the minDist array
        for(Integer i = 0; i < this.size; i++) {
            distMatrix[src][i] = minDistances[i];
            distMatrix[i][src] = minDistances[i];
        }     
        
        // reconstruct the path from target to source via backtracking
        
        // gotta reverse this, of course (we want source=>target, not target=>source)
        for (Integer vertex = targetVertex; vertex >= 0; vertex = previous[vertex]) {
            pathToTarget.add(vertex);
        }
        
        assert pathToTarget.size() == distMatrix[sourceVertex][targetVertex] : "ERROR: ProtGraph.computePaths(): length of path between a vertex pair inconsistent with distMatrix entry.";
        
        Collections.reverse(pathToTarget);        
        return(pathToTarget);
    }
    
        
    /**
     * Returns the only neighbor of vertex at index 'vertIndex' (it is assumed that this vertex only has 1 neighbor).
     * @param vertIndex the vertex
     * @return the index of the only neighbor of this vertex or -1 if no such vertex exists, i.e., the vertex does not have exactly 1 neighbor.
     */
    public Integer getSingleNeighborOf(Integer vertIndex) {
        if(this.degreeOfVertex(vertIndex) != 1) {
            return(-1);
        }
        else {
            return(this.neighborsOf(vertIndex).get(0));
        }
    }
    
    
    /**
     * Debug function, prints the distance matrix of this graph to STDOUT. Note that is not the contact matrix of the SSEs but
     * the distance matrix of the vertices in the graph, computed using Dijkstra's shortest path algorithm.
     * 
     * You have to call calculateDistancesWithinGraph() first.
     * 
     */
    public void printDistMatrix() {
        
        if(! this.distancesCalculated) {
            this.calculateDistancesWithinGraph();
        }
        
        String s = "";
        
        for( Integer i = 0; i < this.size; i++ ) {
            for(Integer j = 0; j < this.size; j++) {
                s += (distMatrix[i][j] == Integer.MAX_VALUE ? "i" : distMatrix[i][j]) + " ";
            }
            s += "\n";
        }                
        System.out.print("Distance matrix follows (i := infinite/no path between vertices):\n" + s);
    }
    
    
    /**
     * Returns the connected components of this graph as a list of graphs. Computes them first if this has not yet been done.
     * @return the list of CCs as graphs
     */
    public ArrayList<ProtGraph> getConnectedComponents() {
        if( ! this.connectedComponentsComputed) {
            this.computeConnectedComponents();            
        }
        
        return(this.connectedComponents);
    }
    
    
    /**
     * Determines the spatial vertex ordering of this folding graph. This only makes sense for Folding graphs which are not bifurcated and have at least 2 vertices of degree 1 (the first and the last). Since folding graphs are by definition connected, each vertex
     * has *at least* one neighbor.
     * 
     * In detail, this order can be described as follows: the first vertex in the list is the vertex of degree 1 which is closest to the N terminus in the amino acid sequence. Note that the N terminus itself may
     * not be part of this folding graph, which is no problem though. If no such vertex exists, the order does not exist.
     * The next vertex is the neighbor of this vertex in the folding graph (i.e., as SSE that this SSE is in contact with in 3D) and so on.
     * Note that this requires that all vertices (except for the first and last one) have exactly 2 neighbors. This cannot be the case if the graph is bifurcated (but the inverse does not hold, i.e., non-bifurcated graphs do not necessarily have a valid KEY ordering).
     * 
     * @return a list containing the vertex indices in the requested order, or an empty list if no such order exists
     */
    public ArrayList<Integer> getSpatialOrderingOfVertexIndices() {
        ArrayList<Integer> spatOrder = new ArrayList<Integer>();
        
        if(this.isBifurcated()) {
            System.err.println("ERROR: getSpatialOrderingOfVertexIndices(): Called for bipartite graph, exiting.");
            System.exit(1);
        }
        
        //if(! this.distancesCalculated) {
        //    this.calculateDistancesWithinGraph();
        //}
        
        Integer start = this.closestToNTerminusDegree1();
        if(start < 0) {
            System.err.println("WARNING: getSpatialOrderingOfVertexIndices(): No vertex of degree 1 found, order not possible.");
            // No vertex found so this notation is not posible, return an empty ArrayList
            return(spatOrder);
        }              

        Integer next;
        Integer last = start;
        Integer cur = this.getSingleNeighborOf(start);
        while(cur > 0) {
            spatOrder.add(cur);            
            next = this.getVertexNeighborBut(cur, last);
            last = cur;
            cur = next;
        }

        assert spatOrder.size() == this.size : "ERROR: ProtGraph.getSpatialOrderingOfVertexIndices(): Length of spatial order array does not match number of graph vertices." ;
        
        return(spatOrder);
    }
    
    
    /**
     * This function determines whether the vertex at index 'parentIndex' in the SSE list of the parent protein graph is part of this folding graph (which is only one of the connected components of its parent).
     * WARNING: This function only makes sense for folding graphs (not protein graphs) and calling it from a protein graph is a critical error. Check this first using the isFoldingGraph() function.
     * @param parentIndex the index of the vertex in question in the SSE list of the parent protein graph
     * @return true if the vertex occurs in this folding graph, false otherwise
     */
    public Boolean parentVertexIsPartOfThisFoldingGraph(Integer parentIndex) {
        
        if(this.isProteinGraph()) {
            System.err.println("ERROR: parentVertexIsPartOfThisFoldingGraph(): this is folding graph only function which is not supported for protein graphs.");
            System.exit(1);
        }
                      
       for(SSE s : this.sseList) {
           if(this.parent.getSSEByPosition(parentIndex).sameSSEas(s)) {
               return(true);
           }
       }
              
       return(false);
    }
    
    
    /**
     * Returns the metadata of this graph.
     * @return the meta data hashmap.
     */
    public HashMap<String, String> getMetadata() {
        return(this.metadata);
    }

    
    /**
     * Determines whether this vertex is isolated, i.e., it has no neighbors and thus the degree of the vertex is 0.
     * @return true if the vertex is isolated, false otherwise
     */ 
    public Boolean isIsolatedVertex(Integer vertPosition) {
        if(this.degreeOfVertex(vertPosition) == 0) {
            return(true);
        }
        else {
            return(false);
        }
    }

}

/**
 * A class required only for the priority queue used in the Dijkstra implementation of the ProtGraph class. 
 * Allows comparing vertices by their minDistance property.
 * 
 * @author spirit
 */
class PriorityVertex implements Comparable<PriorityVertex> {
      public Integer index;
      public Integer minDistance;
      
      @Override public String toString() { return("" + index); }

      /**
       * Implements comparability between PriorityVertex instances via the minDinstance property, as required by the priorityQueue.
       */ 
      public int compareTo(PriorityVertex other) {
          return(Double.compare(this.minDistance, other.minDistance));
      }
      
      /**
       * Constructor which creates a new PriorityVertex using the position in the sequential SSE list and the minDistance property.
       * A PriorityVertex object represents an SSE in the protein graph. It is used by the Dijkstra implementation to compute distances and 
       * shortest paths with the protein graph.
       * @param index the position of the SSE in the SSE list of the protein graph
       * @param minDistance the minimal distance of this vertex to the vertex for which the single-source shortest path problem is currently solved (see ProtGraph.computePaths() function).
       */
      PriorityVertex(Integer index, Integer minDistance) {
          this.index = index;
          this.minDistance = minDistance;
      }  
  }




