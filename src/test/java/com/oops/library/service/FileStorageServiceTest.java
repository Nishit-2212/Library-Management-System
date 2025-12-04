package com.oops.library.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private Path rootLocation;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("uploads");
        rootLocation = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
    }

    @Test
    void testStoreFile_success() throws Exception {
        // Mock file
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");

        // Mock input stream
        InputStream mockStream = new ByteArrayInputStream("dummy".getBytes());
        when(file.getInputStream()).thenReturn(mockStream);

        // Mock Files static methods
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                       .thenReturn(10L);

            // Execute
            String stored = fileStorageService.storeFile(file, "images");

            // Verify important behavior
            mockedFiles.verify(() -> Files.createDirectories(rootLocation.resolve("images")));
            mockedFiles.verify(() -> Files.copy(eq(mockStream), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)));

            assertNotNull(stored);
            assertTrue(stored.startsWith("/uploads/images/"));
        }
    }

    @Test
    void testStoreFile_emptyFile_returnsNull() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        String stored = fileStorageService.storeFile(file, "photos");

        assertNull(stored);
    }

    @Test
    void testDeleteFile_success() {
        String storedPath = "/uploads/images/sample.png";

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            fileStorageService.deleteFile(storedPath);

            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)));
        }
    }

    @Test
    void testDeleteFile_nullInput_doesNothing() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            fileStorageService.deleteFile(null);

            // Nothing should be invoked
            mockedFiles.verifyNoInteractions();
        }
    }
}
