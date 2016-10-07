package io.github.vilmosz.tuner.rest.controller;

import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import io.github.vilmosz.tuner.rest.model.AudioFragment;
import io.github.vilmosz.tuner.rest.service.PitchDetectorResult;
import io.github.vilmosz.tuner.rest.service.PitchDetectorService;

@RestController
@RequestMapping("/api/dsp")
public class DspController {

    private static final Logger LOG = Logger.getLogger(DspController.class);

    @RequestMapping(value = "/pitch", method = RequestMethod.POST)
    public ResponseEntity<PitchDetectorResult> detectPitch(
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "sessionId", required = true) String sessionId,
            @RequestParam(value = "seq", required = true) int seq,
            @RequestParam(value = "samples") int samples) throws IOException, InterruptedException, UnsupportedAudioFileException {
        LOG.info(String.format("Fragment [%s-%d] upload=%d samples=%d", sessionId, seq, file.getSize(), samples));
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            Map<PitchEstimationAlgorithm, Float> result = PitchDetectorService.detect(bytes, samples);
            return new ResponseEntity<PitchDetectorResult>(new PitchDetectorResult(new AudioFragment(sessionId, seq, samples), result), HttpStatus.OK);
        } else {
        	throw new UnsupportedAudioFileException("Empty upload");
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<AudioFragment> upload(
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "sessionId", required = true) String sessionId,
            @RequestParam(value = "seq", required = true) int seq,
            @RequestParam(value = "samples") int samples) throws IOException, InterruptedException, UnsupportedAudioFileException {
        LOG.info(String.format("Upload [%s] seq=%d : %d", sessionId, seq, file.getSize()));
        if (!file.isEmpty()) {
            if (seq <= 5) {
                byte[] bytes = file.getBytes();
                /*
                AudioBuffer.i().put(sessionId, bytes);
                if(seq == 5) {
                    AudioBuffer.i().write(sessionId);                    
                } 
                */
                PitchDetectorService.detect(bytes, samples);
                return new ResponseEntity<AudioFragment>(new AudioFragment(sessionId, seq, samples), HttpStatus.OK);
            } else {
                return new ResponseEntity<AudioFragment>(new AudioFragment(sessionId, seq, 0), HttpStatus.OK);
            }
        }
        return new ResponseEntity<AudioFragment>(new AudioFragment(sessionId, seq, file.getSize()), HttpStatus.OK);
    }
    
}
