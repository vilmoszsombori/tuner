package io.github.vilmosz.tuner.rest.model;

public class AudioFragment {
    
    private String id;
    private int sequence;
    private long samples;
    
    public AudioFragment() {}
    
	public AudioFragment(String id, int sequence, long samples) {
		super();
		this.id = id;
		this.sequence = sequence;
		this.samples = samples;
	}

	public String getId() {
		return id;
	}

	public void setId(String sessionId) {
		this.id = sessionId;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public long getSamples() {
		return samples;
	}

	public void setSamples(long samples) {
		this.samples = samples;
	}

	@Override
	public String toString() {
		return "AudioFragment [Id=" + id + ", sequence=" + sequence + ", samples=" + samples + "]";
	}

}
