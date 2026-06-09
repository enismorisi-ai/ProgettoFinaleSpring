package it.aulab.progetto_finale.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

import it.aulab.progetto_finale.dtos.ArticleDto;
import it.aulab.progetto_finale.models.Article;
import it.aulab.progetto_finale.models.Category;
import it.aulab.progetto_finale.models.User;
import it.aulab.progetto_finale.repositories.ArticleRepository;
import it.aulab.progetto_finale.repositories.UserRepository;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        String url = "";
        boolean hasFile = file != null && !file.isEmpty();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId())).get();
            article.setUser(user);
        }

        if(hasFile){
            try{
                CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                url = futureUrl.get();
            }
            catch(Exception e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il salvataggio dell'immagine", e);
            }
        }

        article.setIsAccepted(null);

        Article savedArticle = articleRepository.save(article);
        if(hasFile){
            savedArticle.setImage(imageService.saveImageOnDB(url, savedArticle));
        }

        return modelMapper.map(savedArticle, ArticleDto.class);
    }

    @Override
    public void delete(Long key) {
        if(articleRepository.existsById(key)){
            Article article = articleRepository.findById(key).get();

            try{
                String path = article.getImage().getPath();
                article.getImage().setArticle(null);
                imageService.deleteImage(path);
            } catch(Exception e){
                e.printStackTrace();
            }

            articleRepository.deleteById(key);
        } else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if(optArticle.isPresent()){
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        } else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author id=" + key + " not found");
        }
    }

    @Override
    public List<ArticleDto> readAll() {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for(Article article : articleRepository.findAll()){
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @Override
    @Transactional
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
        String url = "";
        boolean hasFile = file != null && !file.isEmpty();

        //Controllo l'esistenza dell'articolo in base al suo id
        if(articleRepository.existsById(key)){
            //Assegno all'articolo proveniente dal form lo stesso id dell'articolo originale
            updatedArticle.setId(key);
            //Recupero l'articolo originale non modificato
            Article article = articleRepository.findById(key).get();
            //Imposto l'utente dell'articolo del form con l'utente dell'articolo originale
            updatedArticle.setUser(article.getUser());

            // Controllo se l'immagine e' presente nel form
            if(hasFile){
                try{
                    // Elimino l'immagine precedente
                    if(article.getImage() != null){
                        imageService.deleteImage(article.getImage().getPath());
                    }
                    
                    // Salvo la nuova immagine
                    CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                    url = futureUrl.get();

                    // Essendo l'immagine modificata, l'articolo torna in revisione
                    updatedArticle.setIsAccepted(null);
                    Article savedArticle = articleRepository.save(updatedArticle);
                    //Salvo il nuovo path nel db
                    savedArticle.setImage(imageService.saveImageOnDB(url, savedArticle));

                    return modelMapper.map(savedArticle, ArticleDto.class);
                } catch(Exception e){
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento dell'immagine", e);
                }
            }
            else{
                // Se l'immagine non e' stata modificata posso impostare sull'articolo modificato la stessa immagine dell'articolo originale
                updatedArticle.setImage(article.getImage());

                // Se i campi sono diversi l'articolo torna in revisione
                if(hasArticleContentChanged(article, updatedArticle)){
                    updatedArticle.setIsAccepted(null);
                }
                else{
                    updatedArticle.setIsAccepted(article.getIsAccepted());
                }

                return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
            }
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private boolean hasArticleContentChanged(Article article, Article updatedArticle) {
        Long articleCategoryId = article.getCategory() != null ? article.getCategory().getId() : null;
        Long updatedArticleCategoryId = updatedArticle.getCategory() != null ? updatedArticle.getCategory().getId() : null;

        return !Objects.equals(article.getTitle(), updatedArticle.getTitle())
            || !Objects.equals(article.getSubtitle(), updatedArticle.getSubtitle())
            || !Objects.equals(article.getBody(), updatedArticle.getBody())
            || !Objects.equals(article.getPublishDate(), updatedArticle.getPublishDate())
            || !Objects.equals(articleCategoryId, updatedArticleCategoryId);
    }
    
    public List<ArticleDto> searchByCategory(Category category){
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for(Article article : articleRepository.findByCategory(category)){
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    public List<ArticleDto> searchByAuthor(User user){
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for(Article article : articleRepository.findByUser(user)){
            dtos.add(modelMapper.map(article,ArticleDto.class));
        }
        return dtos;
    }

    public void setIsAccepted(Boolean result, Long id){
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    public List<ArticleDto> search(String keyword){
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for(Article article : articleRepository.search(keyword)){
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    } 

}
