package it.aulab.progetto_finale.services;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

import it.aulab.progetto_finale.models.Article;
import it.aulab.progetto_finale.models.Image;

public interface ImageService {
    Image saveImageOnDB(String url, Article article);
    CompletableFuture<String> saveImageOnCloud(MultipartFile file) throws Exception;
    void deleteImage(String imagePath) throws IOException;
}
