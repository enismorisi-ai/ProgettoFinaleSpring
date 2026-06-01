package it.aulab.progetto_finale.repositories;

import org.springframework.data.repository.ListCrudRepository;

import it.aulab.progetto_finale.models.Category;

public interface CategoryRepository extends ListCrudRepository<Category,Long> {
    
}
