/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */
package algorithms;

import java.util.ArrayList;
import plcc.CompatGraph;
import plcc.SSEGraph;

/**
 * Defines methods to compute the edge compatibility graph of two protein graphs.
 * 
 * For details see: Theoretical Computer Science 250 (2001) 1–30, Fundamental Study: Enumerating all connected maximal 
 * common subgraphs in two graphs. Ina Koch 2001.
 * 
 * @author ts
 */
public class CompatGraphComputation {
    
    private SSEGraph graph1;
    private SSEGraph graph2;
    
    private Integer VERTEX1 = 0;
    private Integer VERTEX2 = 1;
    
    /** The source vertices in the Graphs G1 and G2 which led to the creation of the edge in the compatibility graph G. */
    //private Integer[][] sourceVertexOfEdgeFromGraph1;
    //private Integer[][] sourceVertexofEdgeFromGraph2;
    
    
    public CompatGraphComputation(SSEGraph g1, SSEGraph g2) {
        this.graph1 = g1;
        this.graph2 = g2;
    }
    

    /**
     * Return the size (number of vertices) of the larger graph of (graph1, graph2).
     * @return 
     */
    private Integer largerGraphSize() {
        if(this.graph1.numVertices() > this.graph2.numVertices()) {
            return(this.graph1.numVertices());
        }
        else {
            return(this.graph2.numVertices());
        }
    }
    
    
    /**
     * Tests whether the edge between the vertices v1 and v2 in the compatibility graph is a Z edge.
     * 
     * Definition of Z and U edges:
     * Let (e1, e2) and (f1, f2) two edge pairs from two graphs G1 and G2. Let the graph G the edge compatibility
     * graph of G1 and G2. An edge between two vertices (e1, e2) and (f1, f2) in G is called a Z edge if the 
     * edges e1, f1 in G1 share a vertex. Otherwise, the edge is called an U edge.     
     * 
     * @param v1 1st vertex of the edge
     * @param v2 2nd vertex of the edge
     * @param g the compatibility graph
     * @return true if it is a Z edge, false if it is an U edge
     */
    public Boolean compatGraphEdgeisZEdge(Integer v1, Integer v2, CompatGraph g) {
        Boolean isZEdge = false;
        
        Integer edge1v1 = g.getVertex(v1)[VERTEX1];
        Integer edge1v2 = g.getVertex(v1)[VERTEX2];               
        Integer edge2v1 = g.getVertex(v2)[VERTEX1];
        Integer edge2v2 = g.getVertex(v2)[VERTEX2];
        
        if(g.edgesShareVertex(edge1v1, edge1v2, edge2v1, edge2v2)) {
            isZEdge = true;
        }
        
        return(isZEdge);        
    }
    
    
    /**
     * Tests whether the edge between the vertices v1 and v2 in the compatibility graph is an U edge.
     * 
     * Definition of Z and U edges:
     * Let (e1, e2) and (f1, f2) two edge pairs from two graphs G1 and G2. Let the graph G the edge compatibility
     * graph of G1 and G2. An edge between two vertices (e1, e2) and (f1, f2) in G is called a Z edge if the 
     * edges e1, f1 in G1 share a vertex. Otherwise, the edge is called an U edge.
     * 
     * @param v1 1st vertex of the edge 
     * @param v2 2nd vertex of the edge
     * @param g the compatibility graph
     * @return true if it is an U edge, false if it is a Z edge
     */
    public Boolean compatGraphEdgeisUEdge(Integer v1, Integer v2, CompatGraph g) {                
        return( ! compatGraphEdgeisZEdge(v1, v2, g));        
    }
    
    
    
    /**
     * Determines whether the edges (graph1.v1, graph1.v2) in graph1 and (graph2.v1, graph2.v2) in graph2 are compatible.
     * 
     * Two edges (e1, e2) are compatible if and only if both their edge labels (p, a, m, l in the case of protein graphs) and
     * the labels of their vertices (H, E, L, O in the case of protein graphs) are the same.
     * 
     */
    /*
    private Boolean edgesCompatibleBetweenSourceGraphs(Integer v1, Integer v2) {
        Boolean res = false;
        
        Integer graph1MaxIndex = graph1.numVertices() - 1;
        Integer graph2MaxIndex = graph2.numVertices() - 1;
        
        if(v1 > graph1MaxIndex || v1 > graph2MaxIndex || v2 > graph1MaxIndex || v2 > graph2MaxIndex) {
            return(false);
        }
        
        // Do the edges exist at all in the graphs?
        if(graph1.containsEdge(v1, v2) && graph2.containsEdge(v1, v2)) {
            
            // If they exist, do the edge labels match?
            if(graph1.getContactType(v1, v2) == graph2.getContactType(v1, v2)) {
                
                // Do the vertex labels of v1 match?
                if(graph1.getSSEBySeqPosition(v1).getSseType().equals(graph2.getSSEBySeqPosition(v1).getSseType())) {
                    
                    // Do the vertex labels of v2 match?
                    if(graph1.getSSEBySeqPosition(v2).getSseType().equals(graph2.getSSEBySeqPosition(v2).getSseType())) {
                        res = true;                                            
                    }                    
                }
            }            
        }        
        
        return(res);
    }
    */
    
    
    /**
     * Determines whether the edges edgeGraph1 in graph1 and edgeGraph2 in graph2 are compatible.
     * 
     * Two edges (e1, e2) are compatible if and only if both their edge labels (p, a, m, l in the case of protein graphs) and
     * the labels of their vertices (H, E, L, O in the case of protein graphs) are the same.
     * 
     * @return true if they are compatible according to the definition given above, false otherwise
     * 
     */
    private Boolean edgesCompatibleBetweenSourceGraphs(Integer[] edgeGraph1, Integer[] edgeGraph2) {
                        
        if(this.graph1.getEdgeLabel(edgeGraph1[VERTEX1], edgeGraph1[VERTEX2]).equals(this.graph2.getEdgeLabel(edgeGraph2[VERTEX1], edgeGraph2[VERTEX2]))) {
            
            // edge labels are ok, but what about the vertices?            
            String g1e1Label = graph1.getSSEBySeqPosition(edgeGraph1[VERTEX1]).getSseType();
            String g1e2Label = graph1.getSSEBySeqPosition(edgeGraph1[VERTEX2]).getSseType();
            String g2e1Label = graph2.getSSEBySeqPosition(edgeGraph2[VERTEX1]).getSseType();
            String g2e2Label = graph2.getSSEBySeqPosition(edgeGraph2[VERTEX2]).getSseType();
            
            
            if(g1e1Label.equals(g2e1Label) && g1e2Label.equals(g2e2Label)) {
                // vertex labels are ok!
                return(true);
            }                                    
            
        }            
        return(false);                       
    }
    
    
    /**
     * Determines whether the edges e1=(e1v1, e1v2) and e2=(e2v1, e2v2) are the same edge in an undirected graph.
     * @param e1v1 one vertex of the edge e1
     * @param e1v2 the other vertex of the edge e1
     * @param e2v1 one vertex of the edge e2
     * @param e2v2 the other vertex of the edge e2
     * @return true if the edges are the same, false otherwise
     */
    public static Boolean sameEdges(Integer e1v1, Integer e1v2, Integer e2v1, Integer e2v2) {
        if( (e1v1 == e2v1 || e1v1 == e2v2) && (e1v2 == e2v1 || e1v2 == e2v2) ) {
            return(true);
        } else {
            return(false);
        }        
    }
    
    
   
    
    /**
     * Determines whether the edges e1=(e1v1, e1v2) and e2=(e2v1, e2v2) with e1 != e2 share a vertex in the graph g.
     * @param e1v1
     * @param e1v2
     * @param e2v1
     * @param e2v2
     * @param g the SSEGraph
     * @return the vertex label of the vertex (e.g., "E") if they share a vertex, the empty string "" if they don't
     */
    private String edgesShareVertexInGraph(Integer e1v1, Integer e1v2, Integer e2v1, Integer e2v2, SSEGraph g) {
        //TODO: this has to be checked in the SSEGraph, not in the contact graph
        
        String vertexLabel = "";
        
        if(sameEdges(e1v1, e1v2, e2v1, e2v2)) {
            throw new IllegalArgumentException("ERROR: edgesShareVertexInGraph(): Edges must be different.");
        }
        // TODO: implement me
        return(vertexLabel);
    }
            
            
    
    /**
     * Computes the edge compatibility graph of graph1 and graph2.
     * 
     * For details see: Theoretical Computer Science 250 (2001) 1–30, Fundamental Study: Enumerating all connected maximal 
     * common subgraphs in two graphs. Ina Koch 2001.
     * 
     * @return the edge compatibility graph
     */
    public CompatGraph computeEdgeCompatibiltyGraph() {          
                
        CompatGraph h = new CompatGraph(new ArrayList<Integer[]>());                
        
        ArrayList<Integer[]> edgesG1 = graph1.getEdgeList();
        ArrayList<Integer[]> edgesG2 = graph2.getEdgeList();
        
        // add vertices to h
        Integer[] edgeG1, edgeG2;                
        for(Integer i = 0; i < edgesG1.size(); i++) {
            edgeG1 = edgesG1.get(i);
            for(Integer j = 0; j < edgesG2.size(); j++) {                
                edgeG2 = edgesG2.get(j);
                if(this.edgesCompatibleBetweenSourceGraphs(edgeG1, edgeG2)) {
                    h.addVertex(i, j);
                }           
            }            
        }
        
        
        //System.out.println("DEBUG: G1 #edges=" + edgesG1.size() + ", G2 #edges=" + edgesG2.size() + ".");
        //System.out.println("DEBUG: G1 #vertices=" + this.graph1.numVertices() + ", G2 #vertices=" + this.graph2.numVertices() + ".");
        //System.out.println("DEBUG: H #vertices=" + h.getVertices().size() + ".");
        
        // add edges to H
        Integer numVerticesInH = h.getVertices().size();
        Integer numEdgesInH = 0;
        Integer numEdgesInHFromSameLabel = 0;
        Integer numEdgesInHFromNotAdjacent = 0;
        
        
        Integer[] compatVertex1, compatVertex2;
        Integer sharedVertexG1, sharedVertexG2;
        for(Integer i = 0; i < h.numVertices(); i++) {
            for(Integer j = 0; j < h.numVertices(); j++) {
                
                if(i == j) {
                    continue;
                }
                
                // Remember: a compat vertex v=(e1, e2) in H is based on two edges e1=(graph1v1, graph1v2) 
                //           and e2=(graph2v1, graph2v2)
                compatVertex1 = h.getVertex(i);     
                compatVertex2 = h.getVertex(j);
                
                //System.out.println("DEBUG: compatVertex1=" + vertHToString(compatVertex1) + ", compatVertex2=" + vertHToString(compatVertex2) + ".");
                
                sharedVertexG1 = h.getSharedVertex(edgesG1.get(compatVertex1[VERTEX1])[VERTEX1], edgesG1.get(compatVertex1[VERTEX1])[VERTEX2], edgesG1.get(compatVertex2[VERTEX1])[VERTEX1], edgesG1.get(compatVertex2[VERTEX1])[VERTEX2]);
                sharedVertexG2 = h.getSharedVertex(edgesG2.get(compatVertex1[VERTEX2])[VERTEX1], edgesG2.get(compatVertex1[VERTEX2])[VERTEX2], edgesG2.get(compatVertex2[VERTEX2])[VERTEX1], edgesG2.get(compatVertex2[VERTEX2])[VERTEX2]);
                
                if(sharedVertexG1!= null && sharedVertexG2 != null) {
                    // check if label of shared vertix is the same in g1 and g2
                    String labelInG1 = this.graph1.getVertexLabel(sharedVertexG1);
                    String labelInG2 = this.graph2.getVertexLabel(sharedVertexG2);
                    
                    // if labels match, add edge
                    if(labelInG1.equals(labelInG2)) {
                        h.addEdge(i, j);
                        numEdgesInHFromSameLabel++;
                    }                                               
                }
                else if((sharedVertexG1 == null) && (sharedVertexG2 == null)) {
                    h.addEdge(i, j);
                    numEdgesInHFromNotAdjacent++;
                }                
            }
        }
        
                
        // DEBUG
        numEdgesInH = numEdgesInHFromSameLabel + numEdgesInHFromNotAdjacent;
        System.out.println(" CompatGraph H of " + graph1.toShortString() + " and " + graph2.toShortString() + " has " + numVerticesInH + " vertices and " + numEdgesInH + " edges (SL=" + numEdgesInHFromSameLabel + ",NA=" + numEdgesInHFromNotAdjacent + ").");
        
        
        
        
        return(h);
    }
    
    
    /**
     * Returns a string representation for a vertex of a compatibility graph. A vertex in H is based on
     * two edges (from G1 and G2).
     * @param vertex the vertex
     * @return its string representation
     */
    public String vertHToString(Integer[] vertex) {
        return("[V(H)::e1(G1)=" + vertex[VERTEX1] + ", e2(G2)=" + vertex[VERTEX2] + "]");
    }
    
}
