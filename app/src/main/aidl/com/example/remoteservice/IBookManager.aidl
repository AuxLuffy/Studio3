// IBookManager.aidl
package com.example.remoteservice;

// Declare any non-default types here with import statements
import com.example.remoteservice.Book;
import com.example.remoteservice.IOnNewBookArrivedListener;
interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
