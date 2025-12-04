package com.oops.library.entity;

import java.time.LocalDateTime;

public class BookBorrowingDTO {
    private Long bookId;
    private String bookTitle;
    private String author;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private boolean overdue;
	public BookBorrowingDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BookBorrowingDTO(Long bookId, String bookTitle, String author, LocalDateTime borrowDate,
			LocalDateTime dueDate, boolean overdue) {
		super();
		this.bookId = bookId;
		this.bookTitle = bookTitle;
		this.author = author;
		this.borrowDate = borrowDate;
		this.dueDate = dueDate;
		this.overdue = overdue;
	}
	public Long getBookId() {
		return bookId;
	}
	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}
	public String getBookTitle() {
		return bookTitle;
	}
	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public LocalDateTime getBorrowDate() {
		return borrowDate;
	}
	public void setBorrowDate(LocalDateTime borrowDate) {
		this.borrowDate = borrowDate;
	}
	public LocalDateTime getDueDate() {
		return dueDate;
	}
	public void setDueDate(LocalDateTime dueDate) {
		this.dueDate = dueDate;
	}
	public boolean isOverdue() {
		return overdue;
	}
	public void setOverdue(boolean overdue) {
		this.overdue = overdue;
	}
    
   
    
}