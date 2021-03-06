package alltests;

/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2015. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */

import graphdrawing.IDrawableEdge;
import graphdrawing.IDrawableGraph;
import graphdrawing.IDrawableVertex;
import java.util.ArrayList;
import java.util.List;
import proteingraphs.SSEGraph;
import junit.framework.TestCase;
import linnottograph.ILinnotToGraph;
import linnottograph.LinnotToGraph;
import proteingraphs.FoldingGraph;
import proteingraphs.GraphCreator;
import resultcontainers.PTGLNotationFoldResult;
import proteingraphs.PTGLNotations;
import proteingraphs.ProtGraph;




/**
 *
 * @author spirit
 */
public class TestLinnotREDToGraph extends TestCase {
    
    private String linnot;
    private String graphType;
    private String linnotType;
    private ILinnotToGraph ltg;
    
    /**
     * Sets up the test environment and object.
     */
     @org.junit.Before@Override
    public void setUp() {
        linnot = "[h,-1mh,2pe]";
        graphType = SSEGraph.GRAPHTYPE_ALBELIG;
        linnotType = FoldingGraph.FG_NOTATION_RED;
        ltg = new LinnotToGraph(linnot, linnotType, graphType);
    }
    
     
    @org.junit.Test public void testEdges() {             
        List<IDrawableEdge> edges = ltg.getEdges();
        assertEquals(2, edges.size());
    }
    
    @org.junit.Test public void testVertices() {             
        List<IDrawableVertex> verts = ltg.getVertices();
        assertEquals(3, verts.size());
    }
    
    @org.junit.Test public void testGraph() {             
        IDrawableGraph dg = ltg.getDrawableGraph();
        ProtGraph pg = GraphCreator.fromDrawableGraph(dg);
        assertEquals(3, pg.getVertices().size());
    }
    
    @org.junit.Test public void testResGraphFG() {             
        IDrawableGraph dg = ltg.getDrawableGraph();
        ProtGraph pg = GraphCreator.fromDrawableGraph(dg);
        ArrayList<FoldingGraph> fgs = pg.getFoldingGraphs();
        assertEquals(1, fgs.size());
    }
    
    @org.junit.Test public void testResGraphLinnotRed() {             
        IDrawableGraph dg = ltg.getDrawableGraph();
        ProtGraph pg = GraphCreator.fromDrawableGraph(dg);
        PTGLNotations p = new PTGLNotations(pg);
        List<PTGLNotationFoldResult> linnots = p.getResults();
        if(linnots.size() != 1) {
            fail("wrong number of FGs in resulting PG");
        }
        // there is only one in the list anyways
        for(PTGLNotationFoldResult l : linnots) {
            assertEquals(this.linnot, l.redNotation);
        }
    }
    
    @org.junit.Test public void testResGraphLinnotAdj() {             
        IDrawableGraph dg = ltg.getDrawableGraph();
        ProtGraph pg = GraphCreator.fromDrawableGraph(dg);
        PTGLNotations p = new PTGLNotations(pg);
        List<PTGLNotationFoldResult> linnots = p.getResults();
        if(linnots.size() != 1) {
            fail("wrong number of FGs in resulting PG");
        }
        // there is only one in the list anyways
        for(PTGLNotationFoldResult l : linnots) {
            assertEquals(this.linnot, l.adjNotation);
        }
    }
    
}
