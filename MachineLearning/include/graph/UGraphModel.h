/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * File:   UGraphModel.h
 * Author: LTSACH
 *
 * Created on 24 August 2020, 15:16
 */

#ifndef UGRAPHMODEL_H
#define UGRAPHMODEL_H

#include "graph/AbstractGraph.h"
//include "stacknqueue/PriorityQueue.h"

//////////////////////////////////////////////////////////////////////
///////////// UGraphModel: Undirected Graph Model ////////////////////
//////////////////////////////////////////////////////////////////////

template <class T>
class UGraphModel : public AbstractGraph<T>
{
private:
public:
    // class UGraphAlgorithm;
    // friend class UGraphAlgorithm;

    UGraphModel(
        bool (*vertexEQ)(T &, T &),
        string (*vertex2str)(T &)) : AbstractGraph<T>(vertexEQ, vertex2str)
    {
    }

    void connect(T from, T to, float weight = 0)
    {
        // TODO
        typename AbstractGraph<T>::VertexNode* fromNode = this->getVertexNode(from);
        typename AbstractGraph<T>::VertexNode* toNode = this->getVertexNode(to);

        if (!fromNode) {
        throw VertexNotFoundException(this->vertex2str(from));
        }
        if (!toNode) {
            throw VertexNotFoundException(this->vertex2str(to));
        }
        if (fromNode == toNode) {
            fromNode->connect(toNode, weight);
            return;
        }
        fromNode->connect(toNode, weight);
        toNode->connect(fromNode, weight);
    }
    void disconnect(T from, T to)
    {
        // TODO
        typename AbstractGraph<T>::VertexNode* fromNode = this->getVertexNode(from);
        typename AbstractGraph<T>::VertexNode* toNode = this->getVertexNode(to);
        if (!fromNode) {
            throw VertexNotFoundException(this->vertex2str(from));
        }
        if (!toNode) {
            throw VertexNotFoundException(this->vertex2str(to));
        }
        typename AbstractGraph<T>::Edge* fromToEdge = fromNode->getEdge(toNode);
        if (!fromToEdge) {
            throw EdgeNotFoundException("Edge(" + this->vertex2str(from) + " → " + this->vertex2str(to) + ")");
        }
        if (fromNode == toNode) {
            fromNode->removeTo(toNode);
            return;
        }
        fromNode->removeTo(toNode);
        toNode->removeTo(fromNode);
    }
    void remove(T vertex)
    {
        // TODO
        typename AbstractGraph<T>::VertexNode* targetNode = this->getVertexNode(vertex);
        if (!targetNode) {
            throw VertexNotFoundException(this->vertex2str(vertex));
        }
        for (typename DLinkedList<typename AbstractGraph<T>::VertexNode*>::Iterator it = this->nodeList.begin(); it != this->nodeList.end(); ++it) {
            typename AbstractGraph<T>::VertexNode* node = *it;

            node->removeTo(targetNode);
            targetNode->removeTo(node);
        }

        this->nodeList.removeItem(targetNode, [](typename AbstractGraph<T>::VertexNode* node) { delete node; });
    }

    static UGraphModel<T> *create(
        T *vertices, int nvertices, Edge<T> *edges, int nedges,
        bool (*vertexEQ)(T &, T &),
        string (*vertex2str)(T &))
    {
        // TODO
        UGraphModel<T>* graph = new UGraphModel<T>(vertexEQ, vertex2str);
        for (int i = 0; i < nvertices; i++) {
            graph->add(vertices[i]);
        }
        for (int i = 0; i < nedges; i++) {
            Edge<T>& edge = edges[i];
            graph->connect(edge.from, edge.to, edge.weight);
        }
        return graph;

    }
};

#endif /* UGRAPHMODEL_H */
