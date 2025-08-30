/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   DLinkedListSE.h
 * Author: LTSACH
 *
 * Created on 31 August 2020, 14:13
 */

#ifndef DLINKEDLISTSE_H
#define DLINKEDLISTSE_H
#include "list/DLinkedList.h"
#include "sorting/ISort.h"

template<class T>
class DLinkedListSE: public DLinkedList<T>{
public:
    
    DLinkedListSE(
            void (*removeData)(DLinkedList<T>*)=0, 
            bool (*itemEQ)(T&, T&)=0 ) : 
            DLinkedList<T>(removeData, itemEQ){
        
    };
    
    DLinkedListSE(const DLinkedList<T>& list){
        this->copyFrom(list);
    }
    
    void sort(int (*comparator)(T&,T&)=0){
        //TODO: implement this function
        //     - You should implement the merge sort algorithm
        typename DLinkedList<T>::Node* realHead = this->head->next;
        typename DLinkedList<T>::Node* realTail = this->tail->prev;

        if (realHead == this->tail) {
            return;
        }

        realHead->prev = nullptr;
        if (realTail != this->head) realTail->next = nullptr; 

        typename DLinkedList<T>::Node* sortedHead = mergeSort(realHead, comparator);

        typename DLinkedList<T>::Node* sortedTail = sortedHead;
        this->count = 0;
        while (sortedTail && sortedTail->next) {
            sortedTail = sortedTail->next;
            this->count++;
        }
        if (sortedHead) this->count++;

        this->head->next = sortedHead;
        if (sortedHead) sortedHead->prev = this->head;
        else this->head->next = this->tail;

        this->tail->prev = sortedTail;
        if (sortedTail) sortedTail->next = this->tail;
        else this->tail->prev = this->head;
    };
    
protected:
    static int compare(T& lhs, T& rhs, int (*comparator)(T&,T&)=0){
        if(comparator != 0) return comparator(lhs, rhs);
        else{
            if(lhs < rhs) return -1;
            else if(lhs > rhs) return +1;
            else return 0;
        }
    }
    typename DLinkedList<T>::Node* mergeSort(typename DLinkedList<T>::Node* head, int (*comparator)(T&, T&)) {
        if (!head || !head->next) {
            return head;
        }
        typename DLinkedList<T>::Node* mid = split(head);
        typename DLinkedList<T>::Node* left = head;
        typename DLinkedList<T>::Node* right = mid;
        left = mergeSort(left, comparator);
        right = mergeSort(right, comparator);
        return merge(left, right, comparator);
    }

    typename DLinkedList<T>::Node* split(typename DLinkedList<T>::Node* head) {
        typename DLinkedList<T>::Node* slow = head;
        typename DLinkedList<T>::Node* fast = head->next;

        while (fast && fast->next) {
            slow = slow->next;
            fast = fast->next->next;
        }
        typename DLinkedList<T>::Node* mid = slow->next;
        slow->next = nullptr;
        if (mid) {
            mid->prev = nullptr;
        }
        return mid;
    }
    typename DLinkedList<T>::Node* merge(
        typename DLinkedList<T>::Node* left, 
        typename DLinkedList<T>::Node* right, 
        int (*comparator)(T&, T&)) {

        typename DLinkedList<T>::Node Temp;
        typename DLinkedList<T>::Node* tail = &Temp;

        while (left && right) {
            if (compare(left->data, right->data, comparator) <= 0) {
                tail->next = left;
                left->prev = tail;
                left = left->next;
            } else {
                tail->next = right;
                right->prev = tail;
                right = right->next;
            }
            tail = tail->next;
        }
        if (left) {
            tail->next = left;
            left->prev = tail;
        } else if (right) {
            tail->next = right;
            right->prev = tail;
        }

        return Temp.next;
    }

};

#endif /* DLINKEDLISTSE_H */

