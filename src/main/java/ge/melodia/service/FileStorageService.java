/*
 * FileStorageService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Stores uploaded files on the local disk.
 */
package ge.melodia.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ge.melodia.exception.FileStorageException;
import ge.melodia.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Saves uploaded audio files and images into the upload folder and
 * loads them back. Files get a random UUID name so that two uploads
 * with the same original name never overwrite each other; only that
 * name is stored in the database.
 */
@Service
public class FileStorageService {

    public static final String AUDIO_FOLDER = "audio";
    public static final String IMAGE_FOLDER = "images";

    private static final Set<String> AUDIO_EXTENSIONS =
            Set.of("mp3", "wav", "ogg", "m4a", "flac");
    private static final Set<String> IMAGE_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Map<String, String> AUDIO_CONTENT_TYPES = Map.of(
            "mp3", "audio/mpeg",
            "wav", "audio/wav",
            "ogg", "audio/ogg",
            "m4a", "audio/mp4",
            "flac", "audio/flac");

    private final Path audioDirectory;
    private final Path imageDirectory;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.audioDirectory = root.resolve(AUDIO_FOLDER);
        this.imageDirectory = root.resolve(IMAGE_FOLDER);

        try {
            Files.createDirectories(audioDirectory);
            Files.createDirectories(imageDirectory);
        } catch (IOException e) {
            throw new FileStorageException(
                    "Could not create upload folders in " + root, e);
        }
    }

    public Path getImageDirectory() {
        return imageDirectory;
    }

    /**
     * Saves an uploaded audio file.
     *
     * @param file uploaded file
     * @return generated file name
     */
    public String storeAudio(MultipartFile file) {
        return store(file, audioDirectory, AUDIO_EXTENSIONS, "audio/");
    }

    /**
     * Saves an uploaded image.
     *
     * @param file uploaded file
     * @return generated file name
     */
    public String storeImage(MultipartFile file) {
        return store(file, imageDirectory, IMAGE_EXTENSIONS, "image/");
    }

    /**
     * Saves a file from a stream (used for the demo data).
     *
     * @param in        file content
     * @param extension file extension without the dot
     * @param audio     true for audio, false for an image
     * @return generated file name
     */
    public String storeStream(InputStream in, String extension,
            boolean audio) {
        Path directory = audio ? audioDirectory : imageDirectory;
        return copy(in, directory, extension);
    }

    /**
     * Loads an audio file so that it can be streamed.
     *
     * @param fileName stored file name
     * @return the file as a resource
     */
    public Resource loadAudio(String fileName) {
        Path file = resolveSafely(audioDirectory, fileName);

        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException(
                        "Audio file is missing: " + fileName, null);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileStorageException("Bad file path: " + fileName, e);
        }
    }

    /**
     * Returns the MIME type of a stored audio file.
     *
     * @param fileName stored file name
     * @return MIME type such as audio/mpeg
     */
    public String audioContentType(String fileName) {
        return AUDIO_CONTENT_TYPES.getOrDefault(extensionOf(fileName),
                "application/octet-stream");
    }

    public void deleteAudio(String fileName) {
        delete(audioDirectory, fileName);
    }

    public void deleteImage(String fileName) {
        delete(imageDirectory, fileName);
    }

    private String store(MultipartFile file, Path directory,
            Set<String> allowedExtensions, String contentTypePrefix) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Please select a file to upload");
        }

        String originalName = StringUtils.cleanPath(
                String.valueOf(file.getOriginalFilename()));
        String extension = extensionOf(originalName);
        if (!allowedExtensions.contains(extension)) {
            throw new InvalidFileException("File type '." + extension
                    + "' is not allowed. Allowed: " + allowedExtensions);
        }

        String contentType = file.getContentType();
        if (contentType != null
                && !contentType.startsWith(contentTypePrefix)
                && !contentType.equals("application/octet-stream")) {
            throw new InvalidFileException("File content type '"
                    + contentType + "' is not allowed here");
        }

        try (InputStream in = file.getInputStream()) {
            return copy(in, directory, extension);
        } catch (IOException e) {
            throw new FileStorageException("Could not read upload", e);
        }
    }

    private String copy(InputStream in, Path directory, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = resolveSafely(directory, fileName);

        try {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Could not save " + fileName, e);
        }
        return fileName;
    }

    private void delete(Path directory, String fileName) {
        if (fileName == null) {
            return;
        }

        try {
            Files.deleteIfExists(resolveSafely(directory, fileName));
        } catch (IOException e) {
            throw new FileStorageException("Could not delete " + fileName, e);
        }
    }

    private Path resolveSafely(Path directory, String fileName) {
        Path file = directory.resolve(fileName).normalize();
        if (!file.startsWith(directory)) {
            throw new InvalidFileException("Invalid file name: " + fileName);
        }
        return file;
    }

    private String extensionOf(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            return "";
        }
        return extension.toLowerCase(Locale.ROOT);
    }
}
