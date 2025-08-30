#include <iostream>
#include <string>
#include "TextBuffer.h"
void check(bool cond, const string& msg) {
    cout << (cond ? "[PASS] " : "[FAIL] ") << msg << "\n";
}


int main(int argc, char** argv) {
    cout << "=== DoublyLinkedList<int> Tests ===\n";
    DoublyLinkedList<int> dll;
    check(dll.size() == 0, "empty list has size 0");
    check(dll.toString() == "[]", "empty list toString == []");

    dll.insertAtHead(1);
    dll.insertAtTail(3);
    dll.insertAt(1, 2); // [1,2,3]
    check(dll.toString() == "[1, 2, 3]", "insertAtHead/Tail/insertAt");

    check(dll.indexOf(2) == 1, "indexOf finds 2 at position 1");
    check(dll.contains(3), "contains(3) == true");
    dll.deleteAt(1);    // [1,3]
    check(dll.toString() == "[1, 3]", "deleteAt(1)");

    dll.reverse();      // [3,1]
    check(dll.toString() == "[3, 1]", "reverse()");

    cout << "\n=== TextBuffer Tests ===\n";
    TextBuffer tb;
    check(tb.getContent() == "" && tb.getCursorPos() == 0, "new buffer is empty, cursor==0");

    // insert "abc"
    for (char c : string("abc")) tb.insert(c);
    check(tb.getContent() == "abc", "insert('a','b','c')");
    check(tb.getCursorPos() == 3, "cursor at end after inserts");

    // insert in middle
    tb.moveCursorTo(1);  // a|bc
    tb.insert('X');      // aX|bc
    check(tb.getContent() == "aXbc", "insert('X') at pos 1");
    check(tb.getCursorPos() == 2, "cursor==2 after mid-insert");

    // delete middle
    tb.moveCursorLeft(); // a|Xbc
    tb.deleteChar();     // deletes 'a' -> |Xbc
    check(tb.getContent() == "Xbc", "deleteChar() deletes before cursor");
    check(tb.getCursorPos() == 0, "cursor==0 after deleteChar");

    // deleteAllOccurrences
    tb.clearRedo(); // assume you added this to reset history
    tb.deleteAllOccurrences('X');
    check(tb.getContent() == "bc", "deleteAllOccurrences('X')");

    // sortAscending
    TextBuffer tb2;
    for (char c : string("dbAC")) tb2.insert(c);
    tb2.sortAscending();
    check(tb2.getContent() == "ACdb", "sortAscending on \"dbAC\"");

    // undo/redo
    TextBuffer tb3;
    tb3.insert('A');
    tb3.insert('B');
    tb3.insert('C');
    tb3.moveCursorLeft();
    tb3.insert('X');
    tb3.moveCursorRight();
    tb3.deleteChar();  // history: insert A,B,C; move L; insert X; move R; delete C

    tb3.undo();  // should restore C
    check(tb3.getContent() == "ABXC", "undo deleteChar restores 'C'");
    tb3.undo();  // undo moveCursorRight
    check(tb3.getCursorPos() == 3, "undo moveCursorRight");
    tb3.undo();  // undo insert 'X'
    check(tb3.getContent() == "ABC", "undo insert('X')");

    tb3.redo();  // redo insert 'X'
    check(tb3.getContent() == "ABXC", "redo insert('X')");
    tb3.redo();  // redo moveCursorRight
    check(tb3.getCursorPos() == 4, "redo moveCursorRight");
    tb3.redo();  // redo deleteChar
    check(tb3.getContent() == "ABX", "redo deleteChar");

    cout << "\nAll done.\n";
    return 0;
}
