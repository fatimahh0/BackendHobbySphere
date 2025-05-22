package com.hobbySphere.services; 
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory; 
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    private static final String CLIENT_ID = "215378473059-c8e47o5vubdfbnm079djjd3mmljgf1om.apps.googleusercontent.com";

    public GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance(); // Using GsonFactory here
        
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                jsonFactory
        )
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new Exception("Invalid ID token.");
        }
    }
}
