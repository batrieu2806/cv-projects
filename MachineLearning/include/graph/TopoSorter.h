/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   TopoSorter.h
 * Author: ltsach
 *
 * Created on July 11, 2021, 10:21 PM
 */

#ifndef TOPOSORTER_H
#define TOPOSORTER_H
#include "graph/DGraphModel.h"
#include "list/DLinkedList.h"
#include "sorting/DLinkedListSE.h"

template<class T>
class TopoSorter{
public:
    static int DFS;
    static int BFS; 
    
protected:
    DGraphModel<T>* graph;
    int (*hash_code)(T&, int);
    
public:
    TopoSorter(DGraphModel<T>* graph, int (*hash_code)(T&, int)=0){
        //TODO
        if (!graph) {
        throw invalid_argument("Graph cannot be null");
    }
        this->graph = graph;
        this->hash_code = hash_code; // Assign provided hash function or nullptr
    }   
    DLinkedList<T> sort(int mode=0, bool sorted=true){
        //TODO
        if (mode == BFS) {
        return bfsSort(sorted);
        } else {
            return dfsSort(sorted);
        }
    }
    DLinkedList<T> bfsSort(bool sorted=true){ 
        //TODO
        auto inDegreeMap = vertex2inDegree(hash_code);
        DLinkedListSE<T> zeroDegreeList;
        DLinkedList<T> vertList = graph->vertices();

        for (auto vertex : vertList) {
            int deg = inDegreeMap.get(vertex);
            if (deg == 0) {
                zeroDegreeList.add(vertex);
            }
        }
        if (sorted) {
            zeroDegreeList.sort();
        }

        DLinkedList<T> topologicalOrder;
        while (!zeroDegreeList.empty()) {
            T vertex = zeroDegreeList.removeAt(0);

            topologicalOrder.add(vertex);
            DLinkedList<T> neighbors = graph->getOutwardEdges(vertex);
            for (auto neighbor : neighbors) {
                int newInDegree = inDegreeMap.get(neighbor) - 1;
                inDegreeMap.put(neighbor, newInDegree);
                if (newInDegree == 0) {
                    zeroDegreeList.add(neighbor);
                }
            }
            if (sorted && zeroDegreeList.size() > 1) {
                zeroDegreeList.sort();
            }
        }
        return topologicalOrder;
    }

    DLinkedList<T> dfsSort(bool sorted=true){
        //TODO
        xMap<T, bool> visited(hash_code);
        Stack<T> topologicalStack;
        DLinkedList<T> topologicalOrder;
        auto dfsVisit = [&](T vertex, auto& dfsVisitRef) -> void {
            visited.put(vertex, true);
            for (auto neighbor : graph->getOutwardEdges(vertex)) {
                if (!visited.containsKey(neighbor) || !visited.get(neighbor)) {
                    dfsVisitRef(neighbor, dfsVisitRef);
                }
            }
            topologicalStack.push(vertex);
        };
        for (auto vertex : graph->vertices()) {
            if (!visited.containsKey(vertex) || !visited.get(vertex)) {
                dfsVisit(vertex, dfsVisit);
            }
        }
        while (!topologicalStack.empty()) {
            topologicalOrder.add(topologicalStack.pop());
        }

        if (sorted) {
        }

        return topologicalOrder;
    }

protected:

    //Helper functions
    xMap<T, int> vertex2inDegree(int (*hash)(T&, int));
    xMap<T, int> vertex2outDegree(int (*hash)(T&, int));
    DLinkedList<T> listOfZeroInDegrees();

}; //TopoSorter
template<class T>
int TopoSorter<T>::DFS = 0;
template<class T>
int TopoSorter<T>::BFS = 1;

template<class T>
xMap<T, int> TopoSorter<T>::vertex2inDegree(int (*hash)(T&, int)) {
    xMap<T, int> inDegreeMap(hash);
    // Retrieve the vertices just once and store them in a local DLinkedList
    DLinkedList<T> vertList = graph->vertices();
    for (typename DLinkedList<T>::Iterator it = vertList.begin(); it != vertList.end(); ++it) {
        T vertex = *it;
        try {
            int inDegree = graph->inDegree(vertex);
            inDegreeMap.put(vertex, inDegree);
        } catch (const VertexNotFoundException& e) {
            cout << "VertexNotFoundException: " << e.what() << endl;
            throw;
        }
    }
    return inDegreeMap;
}

template<class T>
xMap<T, int> TopoSorter<T>::vertex2outDegree(int (*hash)(T&, int)) {
    xMap<T, int> outDegreeMap(hash);
    DLinkedList<T> vertList = graph->vertices(); 
    for (typename DLinkedList<T>::Iterator it = vertList.begin(); it != vertList.end(); ++it) {
        T vertex = *it;
        outDegreeMap.put(vertex, graph->outDegree(vertex));
    }

    return outDegreeMap;
}

template<class T>
DLinkedList<T> TopoSorter<T>::listOfZeroInDegrees() {
    DLinkedList<T> zeroInDegreeList;
    xMap<T, int> inDegreeMap = vertex2inDegree(hash_code);
    DLinkedList<T> vertList = graph->vertices(); 
    for (typename DLinkedList<T>::Iterator it = vertList.begin(); it != vertList.end(); ++it) {
        T vertex = *it;
        if (inDegreeMap.get(vertex) == 0) {
            zeroInDegreeList.add(vertex);
        }
    }

    return zeroInDegreeList;
}



/////////////////////////////End of TopoSorter//////////////////////////////////



#endif /* TOPOSORTER_H */

