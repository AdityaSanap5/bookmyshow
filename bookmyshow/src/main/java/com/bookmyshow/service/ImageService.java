package com.bookmyshow.service;

import com.bookmyshow.entity.Image;

import java.util.List;

public interface ImageService {

    public Image create(Image image);
    public List<Image> viewAll();
    public Image viewById(long id);



}
