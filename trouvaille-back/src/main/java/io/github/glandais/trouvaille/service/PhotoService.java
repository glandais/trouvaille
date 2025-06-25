package io.github.glandais.trouvaille.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;

@ApplicationScoped
public class PhotoService {
    public String createPhoto(InputStream data) {
        return "ok";
    }

    public void deletePhoto(String photoId) {
    }
}
