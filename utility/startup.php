<?php
// checks if application is running, then redirects to index.page
$curl = curl_init("http://localhost:65535");
curl_setopt($curl, CURLOPT_TIMEOUT, 1);
curl_setopt($curl, CURLOPT_CONNECTTIMEOUT, 1);
curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
curl_setopt($curl, CURLOPT_FOLLOWLOCATION, 0);	// current nginx setup redirects this script to itself (?!)
curl_exec($curl);
$code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
curl_close($curl);
if ($code == 200) {
	header("Location: index.html");
	exit;
}
chdir("/work/SbWorkingDir");
exec("sh phpStartup.sh > /dev/null &");
?>
<html>
<head>
    <title>Battleships</title>
	<meta http-equiv="Content-Security-Policy" content="upgrade-insecure-requests">
</head>
<body>
<div class="captionText">Battleships</div>
<div id="loading">App loading<span id="dots">.</span></div>

<!-- svg animated background -->
<div class="bgWrapper">
    <div class="bg" id="bg1">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 300">
            <path fill="#101010" fill-opacity="1" d="
M0,32
C50,32,100,48,150,48
C200,48,250,32,300,32
C350,32,400,48,450,48
C500,48,550,32,600,32
C650,32,700,48,750,48
C800,48,850,32,900,32
C950,32,1000,48,1050,48
C1100,48,1150,32,1200,32
L1200,300
L0,300
Z
"></path></svg>
    </div>
    <div class="bg" id="bg2">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 300">
            <path fill="#181818" fill-opacity="1" d="
M0,64
C50,64,100,80,150,80
C200,80,250,64,300,64
C350,64,400,80,450,80
C500,80,550,64,600,64
C650,64,700,80,750,80
C800,80,850,64,900,64
C950,64,1000,80,1050,80
C1100,80,1150,64,1200,64
L1200,300
L0,300
Z
"></path></svg>
    </div>
    <div class="bg" id="bg3">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 300">
            <path fill="#202020" fill-opacity="1" d="
M0,96
C50,96,100,112,150,112
C200,112,250,96,300,96
C350,96,400,112,450,112
C500,112,550,96,600,96
C650,96,700,112,750,112
C800,112,850,96,900,96
C950,96,1000,112,1050,112
C1100,112,1150,96,1200,96
L1200,300
L0,300
Z
"></path></svg>
    </div>
    <div class="bg" id="bg4">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 300">
            <path fill="#282828" fill-opacity="1" d="
M0,128
C50,128,100,144,150,144
C200,144,250,128,300,128
C350,128,400,144,450,144
C500,144,550,128,600,128
C650,128,700,144,750,144
C800,144,850,128,900,128
C950,128,1000,144,1050,144
C1100,144,1150,128,1200,128
L1200,300
L0,300
Z
"></path></svg>
    </div>
    <div id="bgShip">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
            <path fill="#0C0C10" fill-opacity="1" d="
M0,60
L70,60
L75,35
L90,35
L90,30
L75,30
L75,25
L85,20
L85,0
L86,0
L86,20
L92,20
L92,5
L93,5
L93,20
L100,20
L100,100
L20,100Z
"></path>
        </svg>
    </div>
</div>
<style>
body {
	background-color: black;
	color: white;
}
.captionText {
    font-size: 200%;
    text-align: center;
    margin-top: 15px;
    margin-bottom: 35px;
}
#loading {
	margin-left: 15%;
}
.bgWrapper {
	position: absolute;
	left: 0;
	bottom: 0;
	margin: 0;
	width: 100%;
	overflow: hidden;
}
.bg {
	position: fixed;
	overflow: hidden;
	width: 133.33%;
}
#bg1 {
	z-index: -50;
	animation: wave 15s infinite;
	animation-timing-function: linear;
	animation-timing-function: linear;
}
#bg2 {
	z-index: -40;
	animation: wave 14s infinite;
	animation-delay: -3.5s;
	animation-timing-function: linear;
}
#bg3 {
	z-index: -20;
	animation: wave 13s infinite;
	animation-delay: -6.5s;
	animation-timing-function: linear;
}
#bg4 {
	z-index: -10;
	animation: wave 12s infinite;
	animation-delay: -9s;
	animation-timing-function: linear;
}
#bgShip {
	position: fixed;
	overflow: hidden;
	width: 50%;
	animation: drift 18s infinite;
	z-index: -30;
}
@keyframes wave {
	from {
		bottom: -10;
		left: 0;
	}
	33% {
		bottom: 0;
		left: -11.11vw;
	}
	67% {
		bottom: -20;
		left: -22.22vw;
	}
	to {
		bottom: -10;
		left: -33.33vw;
	}
}
@keyframes drift {
	from {
		bottom: 140;
		right: -10;
	}
	25% {
		bottom: 170;
		right: -5;
	}
	50% {
		bottom: 130;
		right: -1;
	}
	75% {
		bottom: 120;
		right: -4;
	}
	to {
		bottom: 140;
		right: -10;
	}
}
</style>
<script>
let dotsInterval = setInterval(updateDots, 400);
let pingInterval = setInterval(ping, 1000);
let attempts = 0;
let attemptsMax = 5;

function updateDots() {
	let dots = document.getElementById("dots");
	if (dots.innerHTML.length < 3)
		dots.innerHTML+= ".";
	else
		dots.innerHTML = "";
}

function ping() {
	let xhr = new XMLHttpRequest();
    xhr.onload = pong;
    xhr.open('GET', 'index.html', true);
    xhr.send();
}

function pong() {
	if (this.status == 200) {
		location.replace("index.html");
	} else if (++attempts >= attemptsMax) {
		clearInterval(dotsInterval);
		clearInterval(pingInterval);
		document.getElementById("dots").innerHTML = " :( ";
		document.getElementById("loading").innerHTML = "Unable to load app.";
	}
}
</script>
</body>
</html>