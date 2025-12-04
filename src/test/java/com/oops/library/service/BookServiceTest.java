package com.oops.library.service;

import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.entity.AncientScript;
import com.oops.library.entity.Book;
import com.oops.library.entity.GeneralBook;
import com.oops.library.repository.BookRepository;
import com.oops.library.repository.BorrowLogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowLogRepository borrowLogRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------
    // getAllBooks()
    // -------------------------------------------------------
    @Test
    void testGetAllBooks_ReturnsList() {
        List<Book> mockBooks = Arrays.asList(new GeneralBook(), new GeneralBook());
        when(bookRepository.findAll()).thenReturn(mockBooks);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testGetAllBooks_ReturnsEmptyListOnError() {
        when(bookRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        List<Book> result = bookService.getAllBooks();

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------
    // getBookById()
    // -------------------------------------------------------
    @Test
    void testGetBookById() {
        Book mockBook = new GeneralBook();
        when(bookRepository.findBookById(1L)).thenReturn(mockBook);

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        verify(bookRepository).findBookById(1L);
    }

    // -------------------------------------------------------
    // findById()
    // -------------------------------------------------------
    @Test
    void testFindById_Success() throws Exception {
        Book mockBook = new GeneralBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));

        Book result = bookService.findById(1L);

        assertNotNull(result);
    }

    @Test
    void testFindById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EnchantedLibraryException.class,
                () -> bookService.findById(1L));
    }

    // -------------------------------------------------------
    // saveBook()
    // -------------------------------------------------------
    @Test
    void testSaveBook() {
        Book book = new GeneralBook();

        bookService.saveBook(book);

        verify(bookRepository).save(book);
    }

    // -------------------------------------------------------
    // deleteBook() - GeneralBook
    // -------------------------------------------------------
    @Test
    void testDeleteBook_GeneralBook() throws Exception {
        GeneralBook book = new GeneralBook();
        book.setCoverImagePath("covers/book1.png");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(borrowLogRepository).deleteByBook(book);
        verify(fileStorageService).deleteFile("covers/book1.png");
        verify(bookRepository).delete(book);
        verify(fileStorageService, never()).deleteFile(null);
    }

    // -------------------------------------------------------
    // deleteBook() - AncientScript
    // -------------------------------------------------------
    @Test
    void testDeleteBook_AncientScript() throws Exception {
        AncientScript script = new AncientScript();
        script.setCoverImagePath("covers/script.png");
        script.setManuscriptPath("manuscripts/script.txt");

        when(bookRepository.findById(2L)).thenReturn(Optional.of(script));

        bookService.deleteBook(2L);

        verify(borrowLogRepository).deleteByBook(script);
        verify(fileStorageService).deleteFile("covers/script.png");
        verify(fileStorageService).deleteFile("manuscripts/script.txt");
        verify(bookRepository).delete(script);
    }

    // -------------------------------------------------------
    // deleteBook() - not found
    // -------------------------------------------------------
    @Test
    void testDeleteBook_NotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EnchantedLibraryException.class,
                () -> bookService.deleteBook(99L));
    }
}
