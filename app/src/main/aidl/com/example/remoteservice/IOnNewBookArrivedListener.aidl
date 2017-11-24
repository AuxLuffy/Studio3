// IOnNewBookArrivedListener.aidl
package com.example.remoteservice;

// Declare any non-default types here with import statements
import com.example.remoteservice.Book;
interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
