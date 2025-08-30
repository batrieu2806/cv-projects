#include "TextBuffer.h"

// ----------------- DoublyLinkedList -----------------
template <typename T>
DoublyLinkedList<T>::DoublyLinkedList() {
    // TODO
    head = nullptr;
    tail = nullptr;
    length = 0;
}

template <typename T>
DoublyLinkedList<T>::~DoublyLinkedList() {
    // TODO
    Node* curr = head;
    while (curr) {
        Node* next = curr->next;
        delete curr;
        curr = next;
    }
    head = tail = nullptr;
    length = 0;
}

template <typename T>
DoublyLinkedList<T>::Node::Node(T data) {
    this->data = data;
    this->prev = nullptr;
    this->next = nullptr;
}

template <typename T>
DoublyLinkedList<T>::Node::Node() {
    data = data;
    prev = nullptr;
    next = nullptr;
}
// TODO: implement other methods of DoublyLinkedList
template <typename T>
typename DoublyLinkedList<T>::Node* DoublyLinkedList<T>::getNode(int index) const {
    if (index < 0 || index >= length) {
        throw std::out_of_range("Index is invalid!");
    }
    Node* curr = head;
    for (int i = 0;i < index;i++){
        curr = curr->next;
    }
    return curr;
}

template <typename T>
int DoublyLinkedList<T>::indexOf(T item) const {
    Node* curr = head;
    int index = 0;
    while (curr != NULL){   
        if (curr->data == item) {
            return index;
        }
        curr = curr->next;
        index++;
    }
    return -1;
}


template <typename T>
bool DoublyLinkedList<T>::contains(T item) const {
    return indexOf(item) != -1;
}

template <typename T>
T& DoublyLinkedList<T>::get(int index) const {
    return getNode(index)->data;
}

template <typename T>
int DoublyLinkedList<T>::size() const {
    return length;
}

template <typename T>
void DoublyLinkedList<T>::insertAt(int index, T data) {
    if (index < 0 || index > length) {
        throw std::out_of_range("Index is invalid!");
    }
    Node* newNode = new Node(data);
    if (index == 0 ){
        if (head == nullptr){
            head = newNode;
            tail = newNode;
        }
        else {
            head->prev = newNode;
            newNode->next = head;
            head = newNode;
        }
    }
    else if (index == length){
        Node* curr = tail;
        newNode->prev = curr;
        curr->next = newNode;
        tail = newNode;
    }
    else {
        Node*curr = getNode(index-1);
        Node*currNext = curr->next;
        curr->next = newNode;
        newNode->next = currNext;
        newNode->prev = curr;
        currNext->prev = newNode;
    }
    length++;
}

template <typename T>
void DoublyLinkedList<T>::insertAtHead(T data) {
    insertAt(0, data);
}

template <typename T>
void DoublyLinkedList<T>::insertAtTail(T data) {
    insertAt(length, data);
}

template <typename T>
void DoublyLinkedList<T>::reverse() {
    if (length <= 1) {
        return;
    }
    Node* curr = head;
    Node* Temp = nullptr;

    while (curr != NULL){
        Temp = curr->prev;
        curr->prev = curr->next;
        curr->next = Temp;
        curr = curr->prev;
    }
    Temp = head;
    head = tail;
    tail = Temp;
}


template <typename T>
string DoublyLinkedList<T>::toString(string (*convert2str)(T&)) const {
    std::ostringstream output;
    output << "[";
    Node* curr = head;
    while (curr != NULL) {
        if (convert2str) {
            output << convert2str(curr->data);
        } else {
            output << curr->data;
        }
        if (curr->next) {
            output << ", ";
        }
        curr = curr->next;
    }
    output << "]";
    return output.str();
}

template <typename T>
void DoublyLinkedList<T>::deleteAt(int index) {
    if (index < 0 || index >= length) {
        throw std::out_of_range("Index is invalid!");
    }
    if (length == 1){
        Node* curr = head;
        delete curr;
        head = nullptr;
        tail = nullptr;
    }
    else if (index == 0){
        Node*curr = head;
        Node*currNext = curr->next;
        
        head = currNext;
        head->prev = nullptr;
        delete curr;
    }
    else if (index == length - 1){
        Node*curr = tail;
        Node*currPrev = curr->prev;

        tail = currPrev;
        tail->next = nullptr;
        delete curr;
    }
    else {
        Node* curr = getNode(index);
        Node*currNext = curr->next;
        Node*currPrev = curr->prev;

        currPrev->next = currNext;
        currNext->prev = currPrev;
        delete curr;
    }
    length--;
}

int ConvertChar(char c){
    if (isupper(c)){
        return c + 31;
    }
    return c + 0;
}

template <typename T>
typename DoublyLinkedList<T>::Node* DoublyLinkedList<T>::getMiddle(){
    if (head == nullptr){
        return nullptr;
    }
    Node* middle =head;
    Node* end = head;

    while (middle->next && end->next->next) {
        end = end->next->next;
        middle = middle->next;
    }
    return middle;
}

template <typename T>
typename DoublyLinkedList<T>::Node* DoublyLinkedList<T>::mergeSortCore(Node* left, Node* right){
    if constexpr (!std::is_same_v<T, char>) {
        return left ? left : right;
    } else {
          Node Temp;
    Node * tailed = &Temp;
    while (left && right){
        char lc = static_cast<char>(left->data);
        char rc = static_cast<char>(right->data);
        if (ConvertChar(lc) <= ConvertChar(rc)){
            tailed->next = left;
            left->prev = tailed;
            left = left->next;
        }
        else {
            tailed->next = right;
            right->prev = tailed;
            right = right->next;
        }
        tailed = tailed->next;
    }
    tailed->next = (left ? left : right);
    if (tailed->next) tailed->next->prev = tailed;

    return Temp.next;
    }
}

template <typename T>
typename DoublyLinkedList<T>::Node* DoublyLinkedList<T>::mergeSort(Node* head){
    if constexpr (!std::is_same_v<T, char>) {
        return head;
    }  else {
         if (head == nullptr || head->next == nullptr ) {
        return head;
    }
    Node* Middle = getMiddle();
    Node* right = Middle->next;    
    right->prev = nullptr;
    Node* left = head;

    Node* rightSorted = mergeSort(left);
    Node* leftSorted = mergeSort(right);

    return mergeSortCore(leftSorted,rightSorted);
    }
}

template <typename T>
void DoublyLinkedList<T>::mergeSortInt(){
    head = mergeSort(head);
    tail = nullptr;
    length = 0;
    Node* curr = head;
    while (curr) {
        tail = curr;
        length++;
        curr = curr->next;
    }
}


// ----------------- TextBuffer -----------------
TextBuffer::TextBuffer() {
    // TODO; 
    cursorPos = 0;
    isUndo = false;
    historyManager = new HistoryManager();
}

TextBuffer::~TextBuffer() {
    // TODO
    delete historyManager;
}

// TODO: implement other methods of TextBuffer


void TextBuffer::clearRedo(){
    if(isUndo){
        while(historyManager->size() > historyManager->currentNum() + 1){
            historyManager->deleteLastAction();
        }
        isUndo = false;
    }
}

void TextBuffer::insert(char c) {
    // TODO; 
    clearRedo();
    buffer.insertAt(cursorPos, c);
    historyManager->addAction("insert", cursorPos, c);
    cursorPos++;
}

string TextBuffer::getContent() const {
    string result;
    for (int i = 0; i < buffer.size(); ++i) {
        result += buffer.get(i);
    }
    return result;
}

int TextBuffer::getCursorPos() const {
    return cursorPos;
}

int TextBuffer::findFirstOccurrence(char c) const {
    for (int i = 0; i < buffer.size(); ++i) {
        if (buffer.get(i) == c) return i;
    }
    return -1;
}

int* TextBuffer::findAllOccurrences(char c, int &count) const {
    count = 0;
    int idx = 0;
    int* result = new int[999];

    for (int i = 0; i < buffer.size(); ++i) {
        if (buffer.get(i) == c){ 
            ++count;
            result[idx] = i;
            idx++;
        }
    }
    return result;
}


void TextBuffer::deleteChar(){
    if (cursorPos == 0) return;
    clearRedo();
    historyManager->addAction("delete",cursorPos,buffer.get(cursorPos - 1));
    buffer.deleteAt(cursorPos - 1);
    cursorPos--;
}

void TextBuffer::moveCursorLeft() {
    if (cursorPos == 0) {
        throw cursor_error();
    }
    clearRedo();
    historyManager->addAction("move", cursorPos, 'L');
    cursorPos--;
}

void TextBuffer::moveCursorRight() {
    if (cursorPos == buffer.size()) {
        throw cursor_error();
    }
    clearRedo();
    historyManager->addAction("move",cursorPos,'R');
    cursorPos++;
}
void TextBuffer::moveCursorTo(int index) {
    if (index < 0 || index > buffer.size()) {
        throw cursor_error();
    }
    clearRedo();
    historyManager->addAction("move", cursorPos,'J');
    cursorPos = index;
}


void TextBuffer::undo(){
    if (historyManager->currentNum() < 0) {
        return;
    }
    auto currAction = historyManager->getAction(historyManager->currentNum());
    string currActionString = currAction.name;
    if (currActionString == "insert") {
        buffer.deleteAt(currAction.cursorPos);
        cursorPos = currAction.cursorPos; 
    }
    else if (currActionString == "delete"){
        buffer.insertAt(currAction.cursorPos, currAction.content);
        cursorPos = currAction.cursorPos + 1;
    }
    else if (currActionString == "move") {
        if (currAction.content == 'L') {
            cursorPos = currAction.cursorPos + 1;
        }
        else if (currAction.content == 'R') {
            cursorPos = currAction.cursorPos - 1;
        }
        else if (currAction.content == 'J') {
            cursorPos = currAction.cursorPos;
        }
    }

    historyManager->currentAdd(-1);
    isUndo = true; 
}

void TextBuffer::redo(){
    if (historyManager->currentNum() + 1 >= historyManager->size()) {
        return;
    }
    historyManager->currentAdd(1);
    auto currAction = historyManager->getAction(historyManager->currentNum());
    string currActionString = currAction.name;

    if (currActionString == "insert") {
        buffer.insertAt(currAction.cursorPos, currAction.content);
        cursorPos = currAction.cursorPos + 1;
    }
    else if (currActionString == "delete") {
        buffer.deleteAt(currAction.cursorPos);
        cursorPos = currAction.cursorPos;
    }
    else if (currActionString == "move") {
        if (currAction.content == 'L') {
            cursorPos = currAction.cursorPos - 1;
        }
        else if (currAction.content == 'R') {
            cursorPos = currAction.cursorPos + 1;
        }
        else if (currAction.content == 'J') {
            cursorPos = currAction.cursorPos;
        }
    }

    isUndo = true;
}

void TextBuffer::sortAscending() {
    clearRedo();
    int oldPos = cursorPos;
    historyManager->addAction("sort", oldPos, '\0');
    buffer.mergeSortInt();
    cursorPos = 0;
}

void TextBuffer::deleteAllOccurrences(char c) {
    clearRedo();

    bool found = false;
    int originalCursor = cursorPos;
    int i = 0;
    int n = buffer.size();
    while (i < n) {
        if (buffer.get(i) == c) {
            buffer.deleteAt(i);
            found = true;
            n--;
            if (i < cursorPos) {
                cursorPos--;
            }
        } else {
            i++;
        }
    }
    if (found) {
        cursorPos = 0;
    }
}

// ----------------- HistoryManager -----------------
TextBuffer::HistoryManager::HistoryManager() {
    // TODO
    current = -1;
}
TextBuffer::HistoryManager::action::action(const string& name, int cursorPos, char content){
    this->name = name;
    this->cursorPos = cursorPos;
    this->content = content;
}

TextBuffer::HistoryManager::action::action(){
    name = "";
    cursorPos = 0;
    content = '\0';
}

TextBuffer::HistoryManager::~HistoryManager() {
   // No need to manually destroy history
}

//TODO: implement other methods of HistoryManager
void TextBuffer::HistoryManager::addAction(const string &actionName, int cursorPos, char c){
    history.insertAtTail(action(actionName, cursorPos, c));
    current = history.size() - 1;
};

int TextBuffer::HistoryManager::size() const {
    return history.size();
}

int TextBuffer::HistoryManager::currentNum() const {
    return current;
}

void TextBuffer::HistoryManager::printHistory() const {
    std::ostringstream output;
    output << "[";

    for (int i = 0; i < history.size(); ++i) {
        const action& a = history.get(i);
        output << "(" << a.name << ", " << a.cursorPos << ", ";
        if (a.content == '\0') {
            output << "\\0";
        } else {
            output << a.content;
        }
        output << ")";
        if (i != history.size() - 1) {
            output << ", ";
        }
    }

    output << "]";
    std::cout << output.str() << std::endl;
}

void TextBuffer::HistoryManager::deleteLastAction() {
    if (history.size() > 0)
        history.deleteAt(history.size() - 1);
}

void TextBuffer::HistoryManager::currentAdd(int Num) {
    current += Num;
}

TextBuffer::HistoryManager::action TextBuffer::HistoryManager::getAction(int index) {
    return history.get(index);
}

// Explicit template instantiation for char, string, int, double, float, and Point
template class DoublyLinkedList<char>;
template class DoublyLinkedList<string>;
template class DoublyLinkedList<int>;
template class DoublyLinkedList<double>;
template class DoublyLinkedList<float>;
template class DoublyLinkedList<Point>;