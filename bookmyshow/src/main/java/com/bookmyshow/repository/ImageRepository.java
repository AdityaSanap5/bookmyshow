package com.bookmyshow.repository;

import com.bookmyshow.entity.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {

    Image findByUserEmail(String email);

}
