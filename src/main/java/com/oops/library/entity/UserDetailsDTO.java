package com.oops.library.entity;

import java.time.LocalDateTime;
import java.util.List;

public class UserDetailsDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String userType; // GUEST, LIBRARIAN, SCHOLAR
    private String profileImagePath;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean active;
    private int totalBooksBorrowed;
    private int currentlyBorrowedCount;
    private List<BookBorrowingDTO> currentBorrowings;
    private int overdueBooksCount;
	
	public UserDetailsDTO(Long id, String name, String email, String role, String userType, String profileImagePath,
			LocalDateTime createdAt, LocalDateTime lastLoginAt, boolean active, int totalBooksBorrowed,
			int currentlyBorrowedCount, List<BookBorrowingDTO> currentBorrowings, int overdueBooksCount) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.role = role;
		this.userType = userType;
		this.profileImagePath = profileImagePath;
		this.createdAt = createdAt;
		this.lastLoginAt = lastLoginAt;
		this.active = active;
		this.totalBooksBorrowed = totalBooksBorrowed;
		this.currentlyBorrowedCount = currentlyBorrowedCount;
		this.currentBorrowings = currentBorrowings;
		this.overdueBooksCount = overdueBooksCount;
	}
	
	
	public int getOverdueBooksCount() {
		return overdueBooksCount;
	}


	public void setOverdueBooksCount(int overdueBooksCount) {
		this.overdueBooksCount = overdueBooksCount;
	}


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getProfileImagePath() {
		return profileImagePath;
	}
	public void setProfileImagePath(String profileImagePath) {
		this.profileImagePath = profileImagePath;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}
	public void setLastLoginAt(LocalDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public int getTotalBooksBorrowed() {
		return totalBooksBorrowed;
	}
	public void setTotalBooksBorrowed(int totalBooksBorrowed) {
		this.totalBooksBorrowed = totalBooksBorrowed;
	}
	public int getCurrentlyBorrowedCount() {
		return currentlyBorrowedCount;
	}
	public void setCurrentlyBorrowedCount(int currentlyBorrowedCount) {
		this.currentlyBorrowedCount = currentlyBorrowedCount;
	}
	public List<BookBorrowingDTO> getCurrentBorrowings() {
		return currentBorrowings;
	}
	public void setCurrentBorrowings(List<BookBorrowingDTO> currentBorrowings) {
		this.currentBorrowings = currentBorrowings;
	}
	public UserDetailsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    // Constructors, Getters, Setters
    
}