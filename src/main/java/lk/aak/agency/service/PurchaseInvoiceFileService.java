package lk.aak.agency.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class PurchaseInvoiceFileService {

    private static final long MAXIMUM_FILE_SIZE =
            25L * 1024L * 1024L;

    private static final Set<String>
            ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "pdf"
    );

    private final Path uploadDirectory;

    public PurchaseInvoiceFileService() {

        this.uploadDirectory = Paths.get(
                        "uploads",
                        "purchase-invoices"
                )
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadDirectory);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not create the purchase invoice upload directory.",
                    exception
            );
        }
    }

    public StoredInvoiceFile storeFile(
            MultipartFile file) {

        validateFile(file);

        String originalFileName =
                cleanOriginalFileName(
                        file.getOriginalFilename()
                );

        String extension =
                getFileExtension(originalFileName);

        String storedFileName =
                UUID.randomUUID()
                        + "."
                        + extension;

        Path destination =
                uploadDirectory
                        .resolve(storedFileName)
                        .normalize();

        validateDestination(destination);

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "The original invoice file could not be saved.",
                    exception
            );
        }

        String storedContentType =
                getStoredContentType(extension);

        return new StoredInvoiceFile(
                originalFileName,
                storedFileName,
                storedContentType,
                file.getSize()
        );
    }

    public Resource loadFile(
            String storedFileName) {

        if (storedFileName == null
                || storedFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "The invoice file was not found."
            );
        }

        Path filePath =
                uploadDirectory
                        .resolve(storedFileName)
                        .normalize();

        validateDestination(filePath);

        try {
            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new IllegalArgumentException(
                        "The invoice file was not found."
                );
            }

            return resource;

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "The invoice file could not be opened.",
                    exception
            );
        }
    }

    public void deleteFile(
            String storedFileName) {

        if (storedFileName == null
                || storedFileName.isBlank()) {

            return;
        }

        Path filePath =
                uploadDirectory
                        .resolve(storedFileName)
                        .normalize();

        validateDestination(filePath);

        try {
            Files.deleteIfExists(filePath);

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "The old invoice file could not be deleted.",
                    exception
            );
        }
    }

    private void validateFile(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select the original CBL invoice file."
            );
        }

        if (file.getSize() > MAXIMUM_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "The invoice file must be 25 MB or smaller."
            );
        }

        String originalFileName =
                cleanOriginalFileName(
                        file.getOriginalFilename()
                );

        String extension =
                getFileExtension(originalFileName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {

            throw new IllegalArgumentException(
                    "Only JPG, JPEG, PNG and PDF invoice files are allowed."
            );
        }

        validateFileSignature(file, extension);
    }

    private void validateFileSignature(
            MultipartFile file,
            String extension) {

        byte[] header = new byte[8];
        int bytesRead;

        try (InputStream inputStream =
                     file.getInputStream()) {

            bytesRead = inputStream.read(header);

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "The selected invoice file could not be checked.",
                    exception
            );
        }

        if (bytesRead < 4) {

            throw new IllegalArgumentException(
                    "The selected invoice file is empty or invalid."
            );
        }

        boolean validFile;

        if ("pdf".equals(extension)) {
            validFile = isPdf(header, bytesRead);

        } else if ("png".equals(extension)) {
            validFile = isPng(header, bytesRead);

        } else {
            validFile = isJpeg(header, bytesRead);
        }

        if (!validFile) {

            throw new IllegalArgumentException(
                    "The selected file content does not match "
                            + "its JPG, PNG or PDF extension."
            );
        }
    }

    private boolean isPdf(
            byte[] header,
            int bytesRead) {

        return bytesRead >= 5
                && header[0] == '%'
                && header[1] == 'P'
                && header[2] == 'D'
                && header[3] == 'F'
                && header[4] == '-';
    }

    private boolean isPng(
            byte[] header,
            int bytesRead) {

        return bytesRead >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private boolean isJpeg(
            byte[] header,
            int bytesRead) {

        return bytesRead >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private String getStoredContentType(
            String extension) {

        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";

            default -> throw new IllegalArgumentException(
                    "Unsupported invoice file type."
            );
        };
    }

    private String cleanOriginalFileName(
            String originalFileName) {

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "The selected invoice file has no valid name."
            );
        }

        String cleanedFileName =
                Paths.get(originalFileName)
                        .getFileName()
                        .toString()
                        .trim();

        if (cleanedFileName.contains("..")) {

            throw new IllegalArgumentException(
                    "The invoice file name is invalid."
            );
        }

        return cleanedFileName;
    }

    private String getFileExtension(
            String fileName) {

        int finalDotPosition =
                fileName.lastIndexOf('.');

        if (finalDotPosition < 0
                || finalDotPosition
                == fileName.length() - 1) {

            throw new IllegalArgumentException(
                    "The invoice file must have a valid extension."
            );
        }

        return fileName
                .substring(finalDotPosition + 1)
                .toLowerCase();
    }

    private void validateDestination(
            Path destination) {

        if (!destination.startsWith(
                uploadDirectory)) {

            throw new IllegalArgumentException(
                    "The invoice file location is invalid."
            );
        }
    }

    public record StoredInvoiceFile(
            String originalFileName,
            String storedFileName,
            String contentType,
            long fileSize) {
    }
}