package io.github.vilmosz.tuner.rest.controller;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimpleController {
    
    private static final Logger LOG = Logger.getLogger(SimpleController.class);

    @GetMapping(value = "/echo/{echo}")
    public ResponseEntity<String> test(@PathVariable("echo") String echo) {
        LOG.info(echo);
        return new ResponseEntity<String>(echo, HttpStatus.OK);
    }

    @GetMapping(value = "/getuuid", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getShortUUID() {
        return new ResponseEntity<String>(SimpleController.generateShortUUID(), HttpStatus.OK);
    }
        
    public static synchronized String generateShortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }
    
}
