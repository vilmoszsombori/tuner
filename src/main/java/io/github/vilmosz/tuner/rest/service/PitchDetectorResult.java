package io.github.vilmosz.tuner.rest.service;

import java.util.Map;

import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import io.github.vilmosz.tuner.rest.model.AudioFragment;

public class PitchDetectorResult {

	private AudioFragment fragment;
	private Map<PitchEstimationAlgorithm, Float> result;
	
	public PitchDetectorResult(AudioFragment fragment, Map<PitchEstimationAlgorithm, Float> result) {
		super();
		this.fragment = fragment;
		this.result = result;
	}	
	
	@Override
	public String toString() {
		return "PitchDetectorResult [fragment=" + fragment + ", result=" + result + "]";
	}

	public AudioFragment getFragment() {
		return fragment;
	}

	public void setFragment(AudioFragment fragment) {
		this.fragment = fragment;
	}

	public Map<PitchEstimationAlgorithm, Float> getResult() {
		return result;
	}

	public void setResult(Map<PitchEstimationAlgorithm, Float> result) {
		this.result = result;
	}
	
}
