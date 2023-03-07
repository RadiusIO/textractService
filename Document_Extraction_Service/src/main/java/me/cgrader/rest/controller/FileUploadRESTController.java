package me.cgrader.rest.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.cgrader.storage.IStorageService;
import me.cgrader.storage.StorageFileNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import software.amazon.awssdk.services.textract.TextractClient;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.cgrader.textract.AnalyzeDocument.analyzeDoc;

@RestController
@RequestMapping("/api/v1")
public class FileUploadRESTController {

    private final IStorageService storageService;
    private final TextractClient textractClient;
    Logger logger = LogManager.getLogger(FileUploadRESTController.class);

    @Autowired
    public FileUploadRESTController(IStorageService storageService, TextractClient textractClient) {
        this.storageService = storageService;
        this.textractClient = textractClient;
    }

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok().body("{\"DES health check\":\"okay\"}");
    }

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> listUploadedFiles(Model model) {

        Stream<String> filelist = storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadRESTController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString());

        return ResponseEntity.ok().body("{\"File list\":\"" + filelist.collect(Collectors.toList()) + "\"}");
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping(path = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {

        SortedMap<String, Object> responseMap = new TreeMap<>();
        responseMap.put("filename", file.getOriginalFilename());

        if (file.isEmpty() || file.getSize() == 0) {
            return ResponseEntity.ok().body("{\"Error\":\"Uploaded file is empty\"}");
        } else {
            if (verifyAllowedFileTypes(file.getContentType().toLowerCase())) {
                storageService.store(file);
                this.logger.info("You successfully uploaded " + file.getOriginalFilename() + "!");
                List list = analyzeDoc(textractClient, this.storageService.getRootLocation().toString() + "/" + file.getOriginalFilename());

                if (list == null) {
                    this.logger.error("An error occurred while trying to extract text from file: " + file.getOriginalFilename());
                    responseMap.put("Error", "An error occurred while trying to extract text from file");
                } else if (list.isEmpty()) {
                    this.logger.debug("Could not extract any text from given file: " + file.getOriginalFilename());
                    responseMap.put("Error", "Could not extract any text from given file");
                } else {
                    this.logger.debug("Successfully extracted text from given file: " + file.getOriginalFilename());
                    responseMap.put("extractedText", list);
                }
            } else {
                this.logger.error("Uploaded file type not supported, only jpg/png are allowed: " + file.getOriginalFilename() + " " + file.getContentType().toLowerCase());
                responseMap.put("Error", "Uploaded file type not supported, only jpg/png are allowed");
            }

            Gson gson = new Gson();
            Type gsonType = new TypeToken<HashMap>() { }.getType();
            String gsonString = gson.toJson(responseMap, gsonType);
            return ResponseEntity.ok().body(gsonString);
        }
    }

    private boolean verifyAllowedFileTypes(String type) {
        if ((type.equals("image/jpg")
                || type.equals("image/jpeg")
                || type.equals("image/png"))) {
            return true;
        } else {
            return false;
        }
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        this.logger.error(exc.getMessage());
        return ResponseEntity.notFound().build();
    }

}
