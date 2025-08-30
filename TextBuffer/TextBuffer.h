#ifndef __TEXT_BUFFER_H__
#define __TEXT_BUFFER_H__

#include "main.h"

template <typename T>
class DoublyLinkedList {
    // TODO: may provide some attributes
private:
    struct Node{
        T data;
        Node* prev;
        Node* next;
        Node();
        Node(T data);
    };
    Node* getNode(int index) const;
    Node* head;
    Node* tail;
    int length;

    Node* mergeSortCore(Node* left, Node* right);
public:
    DoublyLinkedList(); // done
    ~DoublyLinkedList(); // done

    void insertAtHead(T data); //done 
    void insertAtTail(T data); //done 
    void insertAt(int index, T data); // done
    void deleteAt(int index); //done
    T& get(int index) const; // done
    int indexOf(T item) const; // done
    bool contains(T item) const;  // done
    int size() const; // done
    void reverse();// done
    string toString(string (*convert2str)(T&) = 0) const; // done
    
    void mergeSortInt();
    Node* mergeSort(Node* head);
    Node* getMiddle();
};

class TextBuffer {
private: 
    // TODO: may provide some attributes
    DoublyLinkedList<char> buffer; 
    int cursorPos;
    bool isUndo;

public:
    TextBuffer(); //done
    ~TextBuffer(); 

    void clearRedo();

    void insert(char c); //done
    void deleteChar(); // done
    void moveCursorLeft();// done
    void moveCursorRight();// done
    void moveCursorTo(int index);// done
    string getContent() const; //done
    int getCursorPos() const; //done
    int findFirstOccurrence(char c) const;//done
    int* findAllOccurrences(char c, int &count) const;//done
    void sortAscending();// done
    void deleteAllOccurrences(char c);
    void undo();// done
    void redo();// done
    void printHistory() const {
        historyManager->printHistory();
    }



public:
    class HistoryManager {
        // TODO: may provide some attributes
    private:
        struct action{
            string name;
            int cursorPos;
            char content;
            action(const string& name, int cursorPos, char content);
            action();
        };
        bool isUndo;
        int current;
        DoublyLinkedList<action> history;

    public:
        HistoryManager(); //done
        ~HistoryManager(); //done

        void addAction(const string &actionName, int cursorPos, char c); //done
        void printHistory() const; //done
        int size() const; //done
        int currentNum() const;

        void deleteLastAction();
        void currentAdd(int num);
        action getAction(int index);
    };

private:
    // History manager instance
    HistoryManager* historyManager;
};

#endif // __TEXT_BUFFER_H__

