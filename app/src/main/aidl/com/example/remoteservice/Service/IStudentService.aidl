package com.example.remoteservice.Service;
import com.example.remoteservice.Service.Student;
// Declare any non-default types here with import statements

interface IStudentService {
    Student getStudentById(int id);
}