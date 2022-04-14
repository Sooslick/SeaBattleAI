function getToken() {
    xhr = new XMLHttpRequest();
    xhr.onload = getTokenHandler;
    xhr.open('GET', '/api/getToken', true);
    xhr.send();
}

function getTokenHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        storedToken = obj.token;
        document.cookie = "au=" + storedToken + "; max-age=3000";
        document.getElementById("debugToken").innerText = storedToken;
        if (queuedAction != undefined) {
            queuedAction.call();
            queuedAction = null;
        }
        return;
    }
    httpFault();
}

function getSessions(token) {
    xhr = new XMLHttpRequest();
	xhr.onload = getSessionsHandler;
	xhr.open('GET', '/api/getSessions?token=' + token, true);
	xhr.send();
}

function getSessionsHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
            queuedAction = getSessions;
            getToken();
            return;
        }
        let listGames = document.getElementById("listGames");
        listGames.innerHTML = "";
        if (obj.session != null) {
            obj.session.forEach(sid => {
                let divSes = listGames.appendChild(document.createElement('div'));
                divSes.className = "clickable";
                divSes.innerHTML = "Join " + sid;
                divSes.setAttribute("onclick", "loadGame(" + sid + ")");
            })
        }
        document.getElementById("loading").hidden = true;
        return;
    }
    httpFault();
}

function createSession(token) {
    xhr = new XMLHttpRequest();
	xhr.onload = createSessionHandler;
	xhr.open('GET', '/api/registerSession?token=' + token, true);
	xhr.send();
}

function createSessionHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
            return;
        }
        storedSessionId = obj.session[0];
        startSessionInterval(storedSessionId);
        return;
    }
    httpFault();
}

function joinSession(token, sid) {
    xhr = new XMLHttpRequest();
	xhr.onload = joinSessionHandler;
	xhr.open('GET', '/api/joinSession?token=' + token + "&sessionId=" + sid, true);
	xhr.send();
	storedSessionId = sid;
}

function joinSessionHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            storedSessionId = null;
            handleFault(obj.info)
            return;
        }
        startSessionInterval(storedSessionId);
        return;
    }
    storedSessionId = null;
    httpFault();
}

function getSessionStatus(token) {
    xhr = new XMLHttpRequest();
    xhr.onload = getStatusHandler;
    xhr.open('GET', '/api/getSessionStatus?token=' + token, true);
    xhr.send();
}

function getStatusHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
            return;
        }
        if (obj.gameResult.phase == "PREPARE") {
            if (storedPhase != "PREPARE")
                phasePrepare();
            updateSelector(obj.gameResult.ships);
            if (reqUpdate) {
                updateField(true, obj.gameResult.myField);
                reqUpdate = false;
            }
        } else if (obj.gameResult.phase.includes("TURN")) {
            let my = obj.gameResult.myTurn;
            if (storedPhase != obj.gameResult.phase) {
                phaseTurn(my);
            }
            updateField(true, obj.gameResult.myField);
            updateField(false, obj.gameResult.enemyField);
            storedPhase = obj.gameResult.phase;
        } else if (obj.gameResult.phase == "ENDGAME" && storedPhase != "ENDGAME") {
            updateField(true, obj.gameResult.myField);
            updateField(false, obj.gameResult.enemyField);
            phaseEnd();
        }
        return;
    }
    httpFault();
}

function placeShip(token, pos, size, vert) {
    xhr = new XMLHttpRequest();
    xhr.onload = placeShipHandler;
    xhr.open('GET', '/api/placeShip?token=' + token
            + "&position=" + pos
            + "&size=" + size
            + "&vertical=" + vert, true);
    xhr.send();
}

function placeShipHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        reqUpdate = true;
        if (!obj.success) {
            handleFault(obj.info)
            return;
        }
        return;
    }
    httpFault();
}

function tryShoot(token, pos) {
    xhr = new XMLHttpRequest();
    xhr.onload = shootHandler;
    xhr.open('GET', '/api/shoot?token=' + token
            + "&position=" + pos, true);
    xhr.send();
}

function shootHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        reqUpdate = true;
        if (!obj.success) {
            handleFault(obj.info)
            return;
        }
        document.getElementById('shipRotate').innerHTML = obj.info;
        return;
    }
    httpFault();
}

function handleFault(msg) {
    document.getElementById("debugStatus").innerHTML = msg;
}

function httpFault() {
    document.getElementById("debugStatus").innerHTML = "Service error. Page reloading probably help to fix this issue.";
}

reqUpdate = false;