// IBookManager.aidl
package com.example.remoteservice;

// Declare any non-default types here with import statements
import com.example.remoteservice.Book;
interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}
