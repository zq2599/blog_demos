/*
 * (C) Copyright 2015 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var ws = new WebSocket('ws://' + location.host + '/player');
var video;
var webRtcPeer;
var state = null;
var isSeekable = false;

var I_CAN_START = 0;
var I_CAN_STOP = 1;
var I_AM_STARTING = 2;

window.onload = function() {
	console = new Console();
	video = document.getElementById('video');
	setState(I_CAN_START);
}

window.onbeforeunload = function() {
	ws.close();
}

ws.onmessage = function(message) {
	var parsedMessage = JSON.parse(message.data);
	console.info('Received message: ' + message.data);

	switch (parsedMessage.id) {
	case 'startResponse':
		startResponse(parsedMessage);
		break;
	case 'error':
		if (state == I_AM_STARTING) {
			setState(I_CAN_START);
		}
		onError('Error message from server: ' + parsedMessage.message);
		break;
	case 'playEnd':
		playEnd();
		break;
	case 'videoInfo':
		showVideoData(parsedMessage);
		break;
	case 'iceCandidate':
		webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
			if (error)
				return console.error('Error adding candidate: ' + error);
		});
		break;
	case 'seek':
		console.log (parsedMessage.message);
		break;
	case 'position':
		document.getElementById("videoPosition").value = parsedMessage.position;
		break;
	case 'iceCandidate':
		break;
	default:
		if (state == I_AM_STARTING) {
			setState(I_CAN_START);
		}
		onError('Unrecognized message', parsedMessage);
	}
}

function start() {
	// Disable start button
	setState(I_AM_STARTING);
	showSpinner(video);

	var mode = $('input[name="mode"]:checked').val();
	console.log('Creating WebRtcPeer in ' + mode + ' mode and generating local sdp offer ...');

	// Video and audio by default
	var userMediaConstraints = {
		audio : true,
		video : true
	}

	if (mode == 'video-only') {
		userMediaConstraints.audio = false;
	} else if (mode == 'audio-only') {
		userMediaConstraints.video = false;
	}

	var options = {
		remoteVideo : video,
		mediaConstraints : userMediaConstraints,
		onicecandidate : onIceCandidate
	}

	console.info('User media constraints' + userMediaConstraints);

	webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
			function(error) {
				if (error)
					return console.error(error);
				webRtcPeer.generateOffer(onOffer);
			});
}

function onOffer(error, offerSdp) {
	if (error)
		return console.error('Error generating the offer');
	console.info('Invoking SDP offer callback function ' + location.host);

	var message = {
		id : 'start',
		sdpOffer : offerSdp,
		videourl : document.getElementById('videourl').value
	}
	sendMessage(message);
}

function onError(error) {
	console.error(error);
}

function onIceCandidate(candidate) {
	console.log('Local candidate' + JSON.stringify(candidate));

	var message = {
		id : 'onIceCandidate',
		candidate : candidate
	}
	sendMessage(message);
}

function startResponse(message) {
	setState(I_CAN_STOP);
	console.log('SDP answer received from server. Processing ...');

	webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
		if (error)
			return console.error(error);
	});
}

function pause() {
	togglePause()
	console.log('Pausing video ...');
	var message = {
		id : 'pause'
	}
	sendMessage(message);
}

function resume() {
	togglePause()
	console.log('Resuming video ...');
	var message = {
		id : 'resume'
	}
	sendMessage(message);
}

function stop() {
	console.log('Stopping video ...');
	setState(I_CAN_START);
	if (webRtcPeer) {
		webRtcPeer.dispose();
		webRtcPeer = null;

		var message = {
			id : 'stop'
		}
		sendMessage(message);
	}
	hideSpinner(video);
}

function debugDot() {
	console.log('Generate debug DOT file ...');
	sendMessage({
		id: 'debugDot'
	});
}

function playEnd() {
	setState(I_CAN_START);
	hideSpinner(video);
}

function doSeek() {
	var message = {
		id : 'doSeek',
		position: document.getElementById("seekPosition").value
	}
	sendMessage(message);
}

function getPosition() {
	var message = {
		id : 'getPosition'
	}
	sendMessage(message);
}

function showVideoData(parsedMessage) {
	//Show video info
	isSeekable = parsedMessage.isSeekable;
	if (isSeekable) {
		document.getElementById('isSeekable').value = "true";
		enableButton('#doSeek', 'doSeek()');
	} else {
		document.getElementById('isSeekable').value = "false";
	}

	document.getElementById('initSeek').value = parsedMessage.initSeekable;
	document.getElementById('endSeek').value = parsedMessage.endSeekable;
	document.getElementById('duration').value = parsedMessage.videoDuration;

	enableButton('#getPosition', 'getPosition()');
}

function setState(nextState) {
	switch (nextState) {
	case I_CAN_START:
		enableButton('#start', 'start()');
		disableButton('#pause');
		disableButton('#stop');
		disableButton('#debugDot');
		enableButton('#videourl');
		enableButton("[name='mode']");
		disableButton('#getPosition');
		disableButton('#doSeek');
		break;

	case I_CAN_STOP:
		disableButton('#start');
		enableButton('#pause', 'pause()');
		enableButton('#stop', 'stop()');
		enableButton('#debugDot', 'debugDot()');
		disableButton('#videourl');
		disableButton("[name='mode']");
		break;

	case I_AM_STARTING:
		disableButton('#start');
		disableButton('#pause');
		disableButton('#stop');
		disableButton('#debugDot');
		disableButton('#videourl');
		disableButton('#getPosition');
		disableButton('#doSeek');
		disableButton("[name='mode']");
		break;

	default:
		onError('Unknown state ' + nextState);
		return;
	}
	state = nextState;
}

function sendMessage(message) {
	var jsonMessage = JSON.stringify(message);
	console.log('Sending message: ' + jsonMessage);
	ws.send(jsonMessage);
}

function togglePause() {
	var pauseText = $("#pause-text").text();
	if (pauseText == " Resume ") {
		$("#pause-text").text(" Pause ");
		$("#pause-icon").attr('class', 'glyphicon glyphicon-pause');
		$("#pause").attr('onclick', "pause()");
	} else {
		$("#pause-text").text(" Resume ");
		$("#pause-icon").attr('class', 'glyphicon glyphicon-play');
		$("#pause").attr('onclick', "resume()");
	}
}

function disableButton(id) {
	$(id).attr('disabled', true);
	$(id).removeAttr('onclick');
}

function enableButton(id, functionName) {
	$(id).attr('disabled', false);
	if (functionName) {
		$(id).attr('onclick', functionName);
	}
}

function showSpinner() {
	for (var i = 0; i < arguments.length; i++) {
		arguments[i].poster = './img/transparent-1px.png';
		arguments[i].style.background = "center transparent url('./img/spinner.gif') no-repeat";
	}
}

function hideSpinner() {
	for (var i = 0; i < arguments.length; i++) {
		arguments[i].src = '';
		arguments[i].poster = './img/webrtc.png';
		arguments[i].style.background = '';
	}
}

/**
 * Lightbox utility (to display media pipeline image in a modal dialog)
 */
$(document).delegate('*[data-toggle="lightbox"]', 'click', function(event) {
	event.preventDefault();
	$(this).ekkoLightbox();
});
