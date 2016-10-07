var sessionId;
var seq = 0;

//variables
var leftchannel = [];
var rightchannel = [];
var recorder = null;
var recording = false;
var recordingLength = 0;
var volume = null;
var audioInput = null;
var sampleRate = null;
var audioContext = null;
var context = null;
var outputElement = document.getElementById('output');
var outputString;
var timer;
var UPLOAD_INTERVAL = 1024;

var pitch = [];

getSessionId();


function getSessionId() {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/api/getuuid', true);
	xhr.onload = function(e) {
		console.log(xhr.responseText + ' : ' + e);
		sessionId = xhr.responseText;
		outputString = xhr.responseText;
	};
	xhr.send();
}

// feature detection 
if (!navigator.getUserMedia)
	navigator.getUserMedia = navigator.getUserMedia
			|| navigator.webkitGetUserMedia || navigator.mozGetUserMedia
			|| navigator.msGetUserMedia;

if (navigator.getUserMedia) {
	navigator.getUserMedia({
		audio : true
	}, success, function(e) {
		alert('Error capturing audio.');
	});
} else
	alert('getUserMedia not supported in this browser.');


//when key is down
window.onkeydown = function(e) {
	
	// if R is pressed, we start recording and uploading
	if (e.keyCode == 82) {
		// stop previous timer
		startRecording();
		window.clearInterval(timer);
		timer = window.setInterval(uploadTimer, UPLOAD_INTERVAL);
	// if S is pressed, we stop the recording and the WAV uploads
	} else if (e.keyCode == 83) {
		stopRecording();		
		window.clearInterval(timer);		
	}
}

// stop the recording and package the WAV file
function uploadTimer() {
	stopRecording();
	var wave = createWave();
	// post it
	upload(wave, function(result) {
		for (var key in result) {
		    var value = result[key];
		    if (typeof pitch[key] === "undefined") {
		    	pitch[key] = [];
		    	pitch[key].series = new TimelineDataSeries();
		    	var graph = d3.select("#graphContainer")
		    		.append("div").attr("class", "graph-container").attr("id", key + "-graph");
		    	graph.append("div").text(key);
		    	graph.append("canvas").attr("id", key + "-canvas");
		    	pitch[key].graph  = new TimelineGraphView('pitch.' + key + '.series', key + "-canvas");
		    	pitch[key].graph.updateEndDate();		
		    } else {
		        // append to chart
				var now = new Date();
		    	pitch[key].series.addPoint(now, result[key]);		    	
		    	pitch[key].graph.setDataSeries([pitch[key].series]);;		
		    	pitch[key].graph.updateEndDate();		
		    }
		}
		startRecording();
	});		
}

function startRecording() {
	recording = true;
	// reset the buffers for the new recording
	leftchannel.length = rightchannel.length = 0;
	recordingLength = 0;
	outputElement.innerHTML = 'Recording now...';
}

function stopRecording() {
	recording = false;	
}

function createWave() {
	outputElement.innerHTML = 'Building wav file...';

	// we flat the left and right channels down
	var leftBuffer = mergeBuffers(leftchannel, recordingLength);
	var rightBuffer = mergeBuffers(rightchannel, recordingLength);
	// we interleave both channels together
	var interleaved = interleave(leftBuffer, rightBuffer);

	// we create our wav file
	var buffer = new ArrayBuffer(44 + interleaved.length * 2);
	var view = new DataView(buffer);

	// RIFF chunk descriptor
	writeUTFBytes(view, 0, 'RIFF');
	view.setUint32(4, 44 + interleaved.length * 2, true);
	writeUTFBytes(view, 8, 'WAVE');
	// FMT sub-chunk
	writeUTFBytes(view, 12, 'fmt ');
	view.setUint32(16, 16, true);
	view.setUint16(20, 1, true);
	// stereo (2 channels)
	view.setUint16(22, 2, true);
	view.setUint32(24, sampleRate, true);
	view.setUint32(28, sampleRate * 4, true);
	view.setUint16(32, 4, true);
	view.setUint16(34, 16, true);
	// data sub-chunk
	writeUTFBytes(view, 36, 'data');
	view.setUint32(40, interleaved.length * 2, true);

	// write the PCM samples
	var lng = interleaved.length;
	var index = 44;
	var volume = 1;
	for (var i = 0; i < lng; i++) {
		view.setInt16(index, interleaved[i] * (0x7FFF * volume), true);
		index += 2;
	}

	// our final binary blob
	var blob = new Blob([ view ], {
		type : 'audio/wav'
	});
	
	return blob;
}

function interleave(leftChannel, rightChannel) {
	var length = leftChannel.length + rightChannel.length;
	var result = new Float32Array(length);

	var inputIndex = 0;

	for (var index = 0; index < length;) {
		result[index++] = leftChannel[inputIndex];
		result[index++] = rightChannel[inputIndex];
		inputIndex++;
	}
	return result;
}

function mergeBuffers(channelBuffer, recordingLength) {
	var result = new Float32Array(recordingLength);
	var offset = 0;
	var lng = channelBuffer.length;
	for (var i = 0; i < lng; i++) {
		var buffer = channelBuffer[i];
		result.set(buffer, offset);
		offset += buffer.length;
	}
	return result;
}

function writeUTFBytes(view, offset, string) {
	var lng = string.length;
	for (var i = 0; i < lng; i++) {
		view.setUint8(offset + i, string.charCodeAt(i));
	}
}

function success(e) {
	// creates the audio context
	audioContext = window.AudioContext || window.webkitAudioContext;
	context = new audioContext();

	// we query the context sample rate (varies depending on platforms)
	sampleRate = context.sampleRate;

	console.log('succcess');

	// creates a gain node
	volume = context.createGain();

	// creates an audio node from the microphone incoming stream
	audioInput = context.createMediaStreamSource(e);

	// connect the stream to the gain node
	audioInput.connect(volume);

	/* From the spec: This value controls how frequently the audioprocess event is 
	dispatched and how many sample-frames need to be processed each call. 
	Lower values for buffer size will result in a lower (better) latency. 
	Higher values will be necessary to avoid audio breakup and glitches */
	var bufferSize = 2048;
	recorder = context.createScriptProcessor(bufferSize, 2, 2);

	recorder.onaudioprocess = function(e) {
		if (!recording)
			return;
		var left = e.inputBuffer.getChannelData(0);
		var right = e.inputBuffer.getChannelData(1);
		// we clone the samples
		leftchannel.push(new Float32Array(left));
		rightchannel.push(new Float32Array(right));
		recordingLength += bufferSize;
		console.log('recording');
	}

	// we connect the recorder
	volume.connect(recorder);
	recorder.connect(context.destination);
}

function upload(blob, callback) {
	var xhr = new XMLHttpRequest();
	xhr.onerror = function transferFailed(evt) {
		console.log("An error occurred while transferring the file.");
	};
	xhr.onabort = function transferCanceled(evt) {
		console.log("The transfer has been canceled by the user.");
	};
	xhr.onload = function(e) {
		console.log("The transfer is complete:");
		console.log(xhr.responseText);
		if (typeof callback === "function") {
			try {
				var json = JSON.parse(xhr.responseText);
				if (typeof json === 'object' && json.result) {				
					callback(json.result);
				}
			} catch(error) {
				console.warn('Cannot parse as JSON [' + error + ']: ' + xhr.responseText);				
			}
		}
	};

	// Listen to the upload progress.
	// assuming you have a progress element on your dom
	var progressBar = document.querySelector('progress');
	xhr.upload.onprogress = function(e) {
		if (e.lengthComputable) {
			progressbar.value = (e.loaded / e.total) * 100;
			progressbar.textContent = progressbar.value; // Fallback for unsupported browsers.
		}
	};

	xhr.open('POST', '/api/dsp/pitch', true);

	var formData = new FormData();
	formData.append('sessionId', sessionId);
	formData.append('seq', seq);
	formData.append('samples', recordingLength);
	formData.append('file', blob);

	xhr.send(formData);
	seq = seq + 1;	
}
