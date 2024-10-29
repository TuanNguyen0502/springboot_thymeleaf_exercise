package vn.loh.springboot_thymeleaf_exercise.service;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.loh.springboot_thymeleaf_exercise.entities.Category;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    <S extends Category> S save(S s);

    List<Category> findByName(String name);

    Page<Category> findByName(String name, Pageable pageable);

    List<Category> findAll();

    List<Category> findAll(Sort sort);

    Page<Category> findAll(Pageable pageable);

    List<Category> findAllById(Iterable<Long> longs);

    Optional<Category> findById(Long aLong);

    <S extends Category> Optional<S> findOne(Example<S> example);

    long count();

    void deleteById(Long aLong);

    void delete(Category entity);

    void deleteAll();
}
