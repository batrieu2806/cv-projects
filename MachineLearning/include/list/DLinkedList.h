
#ifndef DLINKEDLIST_H
#define DLINKEDLIST_H

#include "list/IList.h"

#include <sstream>
#include <iostream>
#include <type_traits>
using namespace std;

template <class T>
class DLinkedList : public IList<T>
{
public:
    class Node;        // Forward declaration
    class Iterator;    // Forward declaration
    class BWDIterator; // Forward declaration

protected:
    Node *head; // this node does not contain user's data
    Node *tail; // this node does not contain user's data
    int count;
    bool (*itemEqual)(T &lhs, T &rhs);        // function pointer: test if two items (type: T&) are equal or not
    void (*deleteUserData)(DLinkedList<T> *); // function pointer: be called to remove items (if they are pointer type)

public:
    DLinkedList(
        void (*deleteUserData)(DLinkedList<T> *) = 0,
        bool (*itemEqual)(T &, T &) = 0);
    DLinkedList(const DLinkedList<T> &list);
    DLinkedList<T> &operator=(const DLinkedList<T> &list);
    ~DLinkedList();

    // Inherit from IList: BEGIN
    void add(T e);
    void add(int index, T e);
    T removeAt(int index);
    bool removeItem(T item, void (*removeItemData)(T) = 0);
    bool empty();
    int size();
    void clear();
    T &get(int index);
    int indexOf(T item);
    bool contains(T item);
    string toString(string (*item2str)(T &) = 0);
    // Inherit from IList: END

    void println(string (*item2str)(T &) = 0)
    {
        cout << toString(item2str) << endl;
    }
    void setDeleteUserDataPtr(void (*deleteUserData)(DLinkedList<T> *) = 0)
    {
        this->deleteUserData = deleteUserData;
    }

    bool contains(T array[], int size)
    {
        int idx = 0;
        for (DLinkedList<T>::Iterator it = begin(); it != end(); it++)
        {
            if (!equals(*it, array[idx++], this->itemEqual))
                return false;
        }
        return true;
    }

    /*
     * free(DLinkedList<T> *list):
     *  + to remove user's data (type T, must be a pointer type, e.g.: int*, Point*)
     *  + if users want a DLinkedList removing their data,
     *      he/she must pass "free" to constructor of DLinkedList
     *      Example:
     *      DLinkedList<T> list(&DLinkedList<T>::free);
     */
    static void free(DLinkedList<T> *list)
    {
        typename DLinkedList<T>::Iterator it = list->begin();
        while (it != list->end())
        {
            delete *it;
            it++;
        }
    }

    /* begin, end and Iterator helps user to traverse a list forwardly
     * Example: assume "list" is object of DLinkedList

     DLinkedList<char>::Iterator it;
     for(it = list.begin(); it != list.end(); it++){
            char item = *it;
            std::cout << item; //print the item
     }
     */


    Iterator begin()
    {
        return Iterator(this, true);
    }
    Iterator end()
    {
        return Iterator(this, false);
    }

    /* last, beforeFirst and BWDIterator helps user to traverse a list backwardly
     * Example: assume "list" is object of DLinkedList

     DLinkedList<char>::BWDIterator it;
     for(it = list.last(); it != list.beforeFirst(); it--){
            char item = *it;
            std::cout << item; //print the item
     }
     */
    

    BWDIterator bbegin()
    {
        return BWDIterator(this, true);
    }
    BWDIterator bend()
    {
        return BWDIterator(this, false);
    }

protected:
    static bool equals(T &lhs, T &rhs, bool (*itemEqual)(T &, T &))
    {
        if (itemEqual == 0)
            return lhs == rhs;
        else
            return itemEqual(lhs, rhs);
    }
    void copyFrom(const DLinkedList<T> &list);
    void removeInternalData();
    Node *getPreviousNodeOf(int index);

    //////////////////////////////////////////////////////////////////////
    ////////////////////////  INNER CLASSES DEFNITION ////////////////////
    //////////////////////////////////////////////////////////////////////
public:
    class BWDIterator {
private:
    DLinkedList<T>* pList; // Pointer to the parent list
    Node* pNode;           // Pointer to the current node

public:
    // Constructor
    BWDIterator(DLinkedList<T>* pList = nullptr, bool begin = true) {
        if (begin) {
            // Start at the last data node (before the tail)
            if (pList) this->pNode = pList->tail->prev;
            else this->pNode = nullptr;
        } else {
            // End of the iteration (before the head)
            if (pList) this->pNode = pList->head;
            else this->pNode = nullptr;
        }
        this->pList = pList;
    }

    // Dereference operator
    T& operator*() {
        return this->pNode->data;
    }

    // Equality operator
    bool operator!=(const BWDIterator& other) const {
        return this->pNode != other.pNode;
    }

    // Pre-decrement operator (move backward)
    BWDIterator& operator--() {
        this->pNode = this->pNode->prev;
        return *this;
    }

    // Post-decrement operator (move backward)
    BWDIterator operator--(int) {
        BWDIterator temp = *this;
        --(*this);
        return temp;
    }
    };

    class Node
    {
    public:
        T data;
        Node *next;
        Node *prev;
        friend class DLinkedList<T>;

    public:
        Node(Node *next = 0, Node *prev = 0)
        {
            this->next = next;
            this->prev = prev;
        }
        Node(T data, Node *next = 0, Node *prev = 0)
        {
            this->data = data;
            this->next = next;
            this->prev = prev;
        }
    };

    //////////////////////////////////////////////////////////////////////
    class Iterator
    {
    private:
        DLinkedList<T> *pList;
        Node *pNode;

    public:
        Iterator(DLinkedList<T> *pList = 0, bool begin = true)
        {
            if (begin)
            {
                if (pList != 0)
                    this->pNode = pList->head->next;
                else
                    pNode = 0;
            }
            else
            {
                if (pList != 0)
                    this->pNode = pList->tail;
                else
                    pNode = 0;
            }
            this->pList = pList;
        }

        Iterator &operator=(const Iterator &iterator)
        {
            this->pNode = iterator.pNode;
            this->pList = iterator.pList;
            return *this;
        }
        void remove(void (*removeItemData)(T) = 0)
        {
            pNode->prev->next = pNode->next;
            pNode->next->prev = pNode->prev;
            Node *pNext = pNode->prev; // MUST prev, so iterator++ will go to end
            if (removeItemData != 0)
                removeItemData(pNode->data);
            delete pNode;
            pNode = pNext;
            pList->count -= 1;
        }

        T &operator*()
        {
            return pNode->data;
        }
        bool operator!=(const Iterator &iterator)
        {
            return pNode != iterator.pNode;
        }
        // Prefix ++ overload
        Iterator &operator++()
        {
            pNode = pNode->next;
            return *this;
        }
        // Postfix ++ overload
        Iterator operator++(int)
        {
            Iterator iterator = *this;
            ++*this;
            return iterator;
        }
    };
};
//////////////////////////////////////////////////////////////////////
// Define a shorter name for DLinkedList:

template <class T>
using List = DLinkedList<T>;

//////////////////////////////////////////////////////////////////////
////////////////////////     METHOD DEFNITION      ///////////////////
//////////////////////////////////////////////////////////////////////

template <class T>
DLinkedList<T>::DLinkedList(
    void (*deleteUserData)(DLinkedList<T> *),
    bool (*itemEqual)(T &, T &))
{
    // TODO
    head = new Node();
    tail = new Node();

    head->next = tail;
    tail->prev = head;

    count = 0;

    this->deleteUserData = deleteUserData;
    this->itemEqual = itemEqual;
}

template <class T>
DLinkedList<T>::DLinkedList(const DLinkedList<T> &list)
{
    // TODO
    // Initialize head and tail as dummy nodes
    head = new Node();
    tail = new Node();

    // Link head and tail
    head->next = tail;
    tail->prev = head;

    count = 0;

    deleteUserData = list.deleteUserData;
    itemEqual = list.itemEqual;

    copyFrom(list);
}

template <class T>
DLinkedList<T> &DLinkedList<T>::operator=(const DLinkedList<T> &list)
{
    // TODO
    if (this == &list)
        return *this;

    removeInternalData();

    deleteUserData = list.deleteUserData;
    itemEqual = list.itemEqual;

    copyFrom(list);

    return *this;
}

template <class T>
DLinkedList<T>::~DLinkedList()
{
    // TODO
    removeInternalData();

    // Delete head and tail
    delete head;
    delete tail;
}

template <class T>
void DLinkedList<T>::add(T e)
{
    // TODO
    Node *newNode = new Node(e);

    newNode->prev = tail->prev;
    newNode->next = tail;

    tail->prev->next = newNode;
    tail->prev = newNode;



    count++;
}
template <class T>
void DLinkedList<T>::add(int index, T e)
{
    // TODO
    if (index < 0 || index > count)
        throw std::out_of_range("Index out of bounds");

    Node *node = new Node(e);

    Node *prevNode = getPreviousNodeOf(index);

    node->next = prevNode->next;
    node->prev = prevNode;

    prevNode->next->prev = node;
    prevNode->next = node;

    count++;
}

template <class T>
typename DLinkedList<T>::Node *DLinkedList<T>::getPreviousNodeOf(int index)
{
    /**
     * Returns the node preceding the specified index in the doubly linked list.
     * If the index is in the first half of the list, it traverses from the head; otherwise, it traverses from the tail.
     * Efficiently navigates to the node by choosing the shorter path based on the index's position.
     */
    // TODO
    if (index < 0 || index > count){
        throw std::out_of_range("Index out of bounds"); 
    }
    Node *node = nullptr;

    if (index <= count / 2)
    {
        node = head;
        for (int i = 0; i < index; ++i)
        {
            node = node->next;
        }
    }
    else
    {
        node = tail;
        for (int i = count; i > index; --i)
        {
            node = node->prev;
        }
        node = node->prev;
    }
    return node;
}

template <class T>
T DLinkedList<T>::removeAt(int index)
{
    // TODO
    if (index < 0 || index >= count)
        throw std::out_of_range("Index out of bounds");

    Node *prevNode = getPreviousNodeOf(index);
    Node *nodeToRemove = prevNode->next;

    T data = nodeToRemove->data;

    prevNode->next = nodeToRemove->next;
    nodeToRemove->next->prev = prevNode;

    delete nodeToRemove;

    count--;

    return data;
}

template <class T>
bool DLinkedList<T>::empty()
{
    // TODO
    return count == 0;
}

template <class T>
int DLinkedList<T>::size()
{
    // TODO
    return count;
}

template <class T>
void DLinkedList<T>::clear()
{
    // TODO
    removeInternalData();
    head->next = tail;
    tail->prev = head;
    count = 0;
}

template <class T>
T &DLinkedList<T>::get(int index)
{
    // TODO
    if (index < 0 || index >= count)
        throw std::out_of_range("Index out of bounds");

    Node *prevNode = getPreviousNodeOf(index);
    Node *targetNode = prevNode->next;

    return targetNode->data;
}

template <class T>
int DLinkedList<T>::indexOf(T item)
{
    // TODO
    Node *node = head->next;
    for (int i = 0; i < count; i++, node = node->next)
    {
        if (equals(node->data, item, itemEqual))
            return i;
    }
    return -1;
}

template <class T>
bool DLinkedList<T>::removeItem(T item, void (*removeItemData)(T))
{
    // TODO
    Node *node = head->next;
    while (node != tail)
    {
        if (equals(node->data, item, itemEqual))
        {
            node->prev->next = node->next;
            node->next->prev = node->prev;

            if (removeItemData != nullptr)
                removeItemData(node->data);

            delete node;

            count--;

            return true;
        }
        node = node->next;
    }
    return false;
}

template <class T>
bool DLinkedList<T>::contains(T item)
{
    // TODO
    return indexOf(item) != -1;
}

template <class T>
string DLinkedList<T>::toString(string (*item2str)(T &))
{
    /**
     * Converts the list into a string representation, where each element is formatted using a user-provided function.
     * If no custom function is provided, it directly uses the element's default string representation.
     * Example: If the list contains {1, 2, 3} and the provided function formats integers, calling toString would return "[1, 2, 3]".
     *
     * @param item2str A function that converts an item of type T to a string. If null, default to string conversion of T.
     * @return A string representation of the list with elements separated by commas and enclosed in square brackets.
     */
    // TODO
    std::string result = "[";

    Node *current = head->next; // Start from the first data node
    for (int i = 0; i < this->count; i++)
    {
        if (i > 0)
        {
            result += ", ";
        }

        if (item2str)
        {
            result += item2str(current->data);
        }
        else
        {
            std::stringstream ss;
            ss << current->data;
            result += ss.str();
        }

        current = current->next;
    }

    result += "]"; 
    return result; 
}

template <class T>
void DLinkedList<T>::copyFrom(const DLinkedList<T> &list)
{
    /**
     * Copies the contents of another doubly linked list into this list.
     * Initializes the current list to an empty state and then duplicates all data and pointers from the source list.
     * Iterates through the source list and adds each element, preserving the order of the nodes.
     */
    // TODO
    Node *node = list.head->next;
    while (node != list.tail)
    {
        add(node->data);
        node = node->next;
    }
}

template <class T>
void DLinkedList<T>::removeInternalData()
{
    /**
     * Clears the internal data of the list by deleting all nodes and user-defined data.
     * If a custom deletion function is provided, it is used to free the user's data stored in the nodes.
     * Traverses and deletes each node between the head and tail to release memory.
     */
    // TODO
    if (deleteUserData != nullptr)
        deleteUserData(this);

    Node *node = head->next;
    while (node != tail)
    {
        Node *nextNode = node->next;
        delete node;
        node = nextNode;
    }

    head->next = tail;
    tail->prev = head;
    count = 0;
}

#endif /* DLINKEDLIST_H */
