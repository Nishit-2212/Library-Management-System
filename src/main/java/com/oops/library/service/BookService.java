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
	
	public List<Book> getAllBooks() throws EnchantedLibraryException {
		List<Book> booksAvailableCurrently = bookRepository.findAllBooks();
		if(booksAvailableCurrently == null || booksAvailableCurrently.isEmpty()) {
			throw new EnchantedLibraryException("There are no books at this point of time");
		}
		return booksAvailableCurrently;
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

		@Autowired
	private BorrowLogRepository borrowLogRepository;

	@Autowired
	private FileStorageService fileStorageService;
	
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
