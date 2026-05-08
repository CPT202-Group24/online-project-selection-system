package com.group24.projectselection.service.shared;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SharedDataAccessUtility {

    public <T> CrudResult<T> create(JpaRepository<T, ?> repository, T entity, String entityName) {
        try {
            T saved = repository.save(entity);
            return CrudResult.success(HttpStatus.CREATED.value(), entityName + " created.", saved);
        } catch (Exception ex) {
            return CrudResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create " + entityName + ".");
        }
    }

    public <T, ID> CrudResult<T> readById(JpaRepository<T, ID> repository, ID id, String entityName) {
        try {
            return repository.findById(id)
                    .map(v -> CrudResult.success(HttpStatus.OK.value(), entityName + " found.", v))
                    .orElseGet(() -> CrudResult.error(HttpStatus.NOT_FOUND.value(), entityName + " not found."));
        } catch (Exception ex) {
            return CrudResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to read " + entityName + ".");
        }
    }

    public <T> CrudResult<List<T>> readAll(JpaRepository<T, ?> repository, String entityName) {
        try {
            List<T> items = repository.findAll();
            return CrudResult.success(HttpStatus.OK.value(), entityName + " list loaded.", items);
        } catch (Exception ex) {
            return CrudResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to read " + entityName + " list.");
        }
    }

    public <T> CrudResult<T> update(JpaRepository<T, ?> repository, T entity, String entityName) {
        try {
            T saved = repository.save(entity);
            return CrudResult.success(HttpStatus.OK.value(), entityName + " updated.", saved);
        } catch (Exception ex) {
            return CrudResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update " + entityName + ".");
        }
    }

    public <T, ID> CrudResult<Void> deleteById(JpaRepository<T, ID> repository, ID id, String entityName) {
        try {
            if (!repository.existsById(id)) {
                return CrudResult.error(HttpStatus.NOT_FOUND.value(), entityName + " not found.");
            }
            repository.deleteById(id);
            return CrudResult.success(HttpStatus.NO_CONTENT.value(), entityName + " deleted.", null);
        } catch (Exception ex) {
            return CrudResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete " + entityName + ".");
        }
    }
}
