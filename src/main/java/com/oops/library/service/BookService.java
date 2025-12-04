package com.oops.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oops.library.repository.BookRepository;
import com.oops.library.repository.BorrowLogRepository;
import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.entity.*;
import java.util.*;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowLogRepository borrowLogRepository;

    @Autowired
    private FileStorageService fileStorageService;
    
    public List<Book> getAllBooks() {
        try {
            List<Book> books = bookRepository.findAll();
            // Return empty list instead of throwing exception
            return books != null ? books : new ArrayList<>();
        } catch (Exception e) {
            // Log the error but return empty list
            System.err.println("Error fetching books: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findBookById(bookId);
    }

    public Book findById(Long bookId) throws EnchantedLibraryException {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new EnchantedLibraryException("Book not found with id: " + bookId));
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }
    
    @Transactional
    public void deleteBook(Long bookId) throws EnchantedLibraryException {
        Book book = findById(bookId);
        if (book == null) {
            throw new EnchantedLibraryException("Book not found with id: " + bookId);
        }

        // Delete associated borrow logs first
        borrowLogRepository.deleteByBook(book);

        // Delete any associated files
        if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
            fileStorageService.deleteFile(book.getCoverImagePath());
        }
        if (book instanceof AncientScript) {
            String manuscriptPath = ((AncientScript) book).getManuscriptPath();
            if (manuscriptPath != null && !manuscriptPath.isEmpty()) {
                fileStorageService.deleteFile(manuscriptPath);
            }
        }

        // Finally delete the book
        bookRepository.delete(book);
    }
}