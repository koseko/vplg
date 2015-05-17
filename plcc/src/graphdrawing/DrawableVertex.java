/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012 - 2015. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */
package graphdrawing;

/**
 *
 * @author spirit
 */
public class DrawableVertex implements IDrawableVertex {

    private final String sseFgNotation;
    
    
    public DrawableVertex(String sseFgNotation) {
        this.sseFgNotation = sseFgNotation;
    }
    
    
    @Override
    public String getSseFgNotation() {
        return this.sseFgNotation;
    }
    
}
