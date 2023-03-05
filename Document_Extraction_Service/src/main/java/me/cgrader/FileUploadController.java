package me.cgrader;

import java.io.IOException;
import java.util.stream.Collectors;

import me.cgrader.storage.IStorageService;
import me.cgrader.storage.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import software.amazon.awssdk.services.textract.TextractClient;

import static me.cgrader.textract.AnalyzeDocument.analyzeDoc;

@RestController
@RequestMapping("/api/v1")
public class FileUploadController {

    private final IStorageService storageService;
    private final TextractClient textractClient;

    @Autowired
    public FileUploadController(IStorageService storageService, TextractClient textractClient) {
        this.storageService = storageService;
        this.textractClient = textractClient;
    }

    @GetMapping("/health")
    public String healthcheck() {
        return "{\"DES health check\":\"okay\"}";
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                                "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/image")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        if(file.isEmpty() || file.getSize()==0) {
            return "{\"Error\":\"Uploaded file is empty\"}";
        } else {
            if (verifyAllowedFileTypes(file.getContentType().toLowerCase())) {
                storageService.store(file);
                redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded " + file.getOriginalFilename() + "!");
                System.out.println("You successfully uploaded " + file.getOriginalFilename() + "!" );
                analyzeDoc(textractClient, this.storageService.getRootLocation().toString() + "/" + file.getOriginalFilename());
                return "redirect:/";
            } else {
                return "{\"Error\":\"Uploaded file type not supported, only jpg/png are allowed\"}";
            }
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
        return ResponseEntity.notFound().build();
    }

}
