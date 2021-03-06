/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include "MultAlign.h"



typedef std::list<EdgeDescriptor> c_edge;

typedef std::list<c_edge> c_clique;
typedef std::list<VertexDescriptor_p> s_clique_p;


MultAlign::MultAlign() : graphs(), products(), alignments(), I(0),CUTOFF(0) {count = 0;}

MultAlign::MultAlign(const std::vector<Graph*> g, const std::vector<ProductGraph*> p, const std::vector<BronKerbosch*> a, int i, int cutoff, std::string out) 
                    : graphs(g), products(p), alignments(a), I(i),CUTOFF(cutoff), oManager(Mult_Output(out)){
    count = 0;
} 

MultAlign::~MultAlign() {}

/*
 * Expands a complex clique by one simple clique
 * returns the new complex clique. all inputs are unchanged
 */
c_clique MultAlign::combine(c_clique& complex, s_clique_p& simple, const ProductGraph& pg){
    c_clique result;                                  //new complex clique
    for(c_edge& c_e : complex) {                               //for each complex edge
        for(VertexDescriptor_p& p_v : simple){                                     //for each pvertex
            if (c_e.front() == pg.getProductGraph()[p_v].edgeFst){               //if the pvertex matches the complex edge
                result.push_back(c_e);                                        //copy complex edge to result list
                result.back().push_back( pg.getProductGraph()[p_v].edgeSec); }    //insert into the complex edge 
        }
    }
    return result;
}

/*
 * Compute the multiple alignment from the given graph vectors.
 * The results will be saved in text files in the directory saved in the class
 */
void MultAlign::run() { 
    /* works as an initializer for the intersect function.
     * each clique in the 0-1 alignment is transfered into a temporary complex clique. 
     * The cc is then expanded by the recursive intersect function.
     */
    for(s_clique_p& clique : BK_Output::get_result_all(*alignments[1])) {     //for each simple clique in the 0-1 alignment
        c_clique complex = c_clique();                                        //create new complex clique
        for(VertexDescriptor_p& p_vertex : clique) {                          //for each p_vertex in the simple clique
            complex.push_back(c_edge());                                      //create new list (complex edge)
            complex.back().push_back(products[1]->getProductGraph()[p_vertex].edgeFst);          //add the g1 edge as the first element  
            complex.back().push_back(products[1]->getProductGraph()[p_vertex].edgeSec);          //add the g2 edge as the second element 
        }//end turn the simple clique into a complex one
        intersect(complex, 2); //call intersect to add the remaining layers to the cc
    }
}

void MultAlign::filter() {
    this->count = this->oManager.filter_iso();
}

/*
 * Function to expand a complex clique. Each recursion layer intersects the cc with all sc of the remaining graphs.
 * Once the cc is complete, it is passed to the output manager to be saved in a file.
 * Maximum recursion depth is the number of the graphs in the multiple alignment.
 * takes a complex clique and an int counter of the number of already included graphs
 */
void MultAlign::intersect(c_clique& complex, int i){
    if (i<this->I) { //clique complete?
        for(s_clique_p& clique : BK_Output::get_result_all(*alignments[i])) {
            c_clique new_complex = this->combine(complex, clique, *products[i]);
            if(new_complex.size() > this->CUTOFF) {
                intersect(new_complex, i+1);
            }
        } 
    } else {
        this->oManager.out(complex, this->graphs);
        ++count;
    }  
}

/*
 * return the total number of complex cliques
 */
unsigned long MultAlign::num_cliques(){
    return this->count;
}

std::vector<unsigned int> MultAlign::distribution() {
    return this->oManager.distribution();
}

