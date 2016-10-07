package io.github.vilmosz.tuner.rest.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

public class PitchDetectorService {

    private static final Logger LOG = Logger.getLogger(PitchDetectorService.class);

    public static Map<PitchEstimationAlgorithm, Float> detect(byte[] data, int samples) throws UnsupportedAudioFileException, IOException {
        //int lengthInSamples = 4096;
        Map<PitchEstimationAlgorithm, Float> ret = new HashMap<>();
        float[] audioBuffer = PitchDetectorService.getAudioBuffer(data, samples);
        for (PitchEstimationAlgorithm algorithm : PitchEstimationAlgorithm.values()) {
            PitchDetector detector = algorithm.getDetector(44100, 1024);
            float pitch = 0;
            float[] shortAudioBuffer = new float[1024];
            System.arraycopy(audioBuffer, 0, shortAudioBuffer, 0, shortAudioBuffer.length);
            pitch = detector.getPitch(shortAudioBuffer).getPitch();
            LOG.info(String.format("%15s %8.3f Hz", algorithm, pitch));
            ret.put(algorithm, pitch);
        }
        return ret;
    }

    private static float[] getAudioBuffer(byte[] data, int lengthInSamples) throws UnsupportedAudioFileException, IOException {
        float[] buffer = new float[lengthInSamples];
        // final URL url = TestUtilities.class.getResource(file);
        AudioInputStream audioStream = AudioSystem
                .getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
        AudioFormat format = audioStream.getFormat();
        TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter
                .getConverter(JVMAudioInputStream.toTarsosDSPFormat(format));
        byte[] bytes = new byte[lengthInSamples * format.getSampleSizeInBits()];
        audioStream.read(bytes);
        converter.toFloatArray(bytes, buffer);
        return buffer;
    }

}
