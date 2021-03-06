/*
 * File:   newtestclass.h
 * Author: ben
 *
 * Created on Apr 28, 2015, 2:59:20 PM
 */

#ifndef NEWTESTCLASS_H
#define	NEWTESTCLASS_H

#include <cppunit/extensions/HelperMacros.h>
#include "../GraphService.h"

class newtestclass : public CPPUNIT_NS::TestFixture {
    CPPUNIT_TEST_SUITE(newtestclass);

    CPPUNIT_TEST(test_get_label);
    CPPUNIT_TEST(test_getPropertyValue);
    CPPUNIT_TEST(test_getNumVertices);
    CPPUNIT_TEST(test_getEdges);
    CPPUNIT_TEST(test_getGraphProperties);
    CPPUNIT_TEST(test_getNumEdges);
    CPPUNIT_TEST(test_getVertices);
    CPPUNIT_TEST(test_computeDegreeDist);
    CPPUNIT_TEST(test_get_adjacent);
    CPPUNIT_TEST(test_get_adjacent_all);
    CPPUNIT_TEST(test_get_abs_counts);
    CPPUNIT_TEST(test_get_norm_counts);
    CPPUNIT_TEST(test_get_labeled_abs_counts);
    CPPUNIT_TEST(test_get_labeled_norm_counts);
    CPPUNIT_TEST(test_get_graphlet_identifier);
    CPPUNIT_TEST(test_get_patterns);
    CPPUNIT_TEST(test_compute_CAT);
    CPPUNIT_TEST(test_reverse_string);
    CPPUNIT_TEST(test_get_length_2_patterns);
    CPPUNIT_TEST(test_get_length_3_patterns);
    
    CPPUNIT_TEST_SUITE_END();

public:
    newtestclass();
    virtual ~newtestclass();
    void setUp();
    void tearDown();

private:
    
    Graph oneNodeGraph, threeNodesGraph;
    GraphService service1, service2, service3;
    VertexDescriptor v, u, u3,v3,w3;
    vertex_info vi, ui, ui3, vi3, wi3;
    edge_info ei, fi, ei3, fi3, gi3;
    EdgeDescriptor e3,f3,g3;
    std::vector<std::pair<int,int>> testEdgeVector;
    std::vector<int> testVertexVector;
    std::vector<int> testVectorDegDist;
    std::vector<std::string> test_prop_vector;
    std::vector<int> test_adjacent_vector,test_adjacent_vector1,test_adjacent_vector2;
    std::vector<std::vector<int>> test_adj_all_vector;
    std::vector<std::vector<int>> test_count_vector_int;
    std::vector<std::vector<float>> test_count_vector_float;
    std::string test_string = "ABC";
    std::set<std::string> test_rev_set = {"ABC", "CBA"};
    std::set<std::string> test_CAT_set = {"ABC", "BCA", "CAB"};
    std::vector<std::string> test_2p_vec;
    std::vector<std::vector<std::string>> test_3p_vec;

    void test_get_label();
    void test_getPropertyValue();
    void test_getNumVertices();
    void test_getEdges();
    void test_getGraphProperties();
    void test_getNumEdges();
    void test_getVertices();
    void test_computeDegreeDist();
    void test_get_adjacent();
    void test_get_adjacent_all();
    void test_get_abs_counts();
    void test_get_norm_counts();
    void test_get_labeled_abs_counts();
    void test_get_labeled_norm_counts();
    void test_get_graphlet_identifier();
    void test_get_patterns();
    void test_compute_CAT();
    void test_reverse_string();
    void test_get_length_2_patterns();
    void test_get_length_3_patterns();
};

#endif	/* NEWTESTCLASS_H */

