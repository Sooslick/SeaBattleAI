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
        if (queuedAction == "gs") {
            getSessions(storedToken);
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
            queuedAction = "gs";
            getToken();
            return;
        }
        let listGames = document.getElementById("listGames");
        listGames.innerHTML = "";
        if (obj.sessionInfos != null) {
            obj.sessionInfos.forEach(info => {
                let divSes = listGames.appendChild(document.createElement('div'));
                divSes.className = "clickable";
                let prefix = info.lookup ? "Join " : "Watch ";
                let postfix = info.passworded ? " <img src='lock-icon.png'/>" : "";
                divSes.innerHTML = prefix + info.sessionId + postfix;
                divSes.setAttribute("onclick", "loadGame(" + info.sessionId + ")");
            })
        }
        document.getElementById("loading").hidden = true;
        return;
    }
    httpFault();
}

function createSession(token) {
    pwParam = "";
    pwValue = document.getElementById("pwdContent").value;
    if (pwdEnable && pwValue != null)
        pwParam = "&pw=" + pwValue;
    xhr = new XMLHttpRequest();
	xhr.onload = createSessionHandler;
	xhr.open('GET', '/api/registerSession?token=' + token + pwParam, true);
	xhr.send();
}

function createSessionHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
            return;
        }
        storedSessionId = obj.sessionInfos[0].sessionId;
        startSessionInterval(storedSessionId);
        return;
    }
    httpFault();
}

function joinSession(token, sid) {
    pwParam = "";
    pwValue = document.getElementById("pwdContent").value;
    if (pwdEnable && pwValue != null)
        pwParam = "&pw=" + pwValue;
    xhr = new XMLHttpRequest();
	xhr.onload = joinSessionHandler;
	xhr.open('GET', '/api/joinSession?token=' + token + "&sessionId=" + sid + pwParam, true);
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
    getSessionStatus(token, false);
}

function getSessionStatus(token, scheduleNext) {
    xhr = new XMLHttpRequest();
    xhr.onload = getStatusHandler;
    xhr.scheduleNext = scheduleNext;
    xhr.open('GET', '/api/getSessionStatus?token=' + token, true);
    xhr.send();
}

function getLongpollSessionStatus(token) {
    xhr = new XMLHttpRequest();
    xhr.onload = getStatusHandler;
    xhr.scheduleNext = true;
    xhr.open('GET', '/api/longpoll/getSessionStatus?token=' + token, true);
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
            if (!obj.gameResult.myTurn && this.scheduleNext) {
                getLongpollSessionStatus(storedToken);
            }
        } else if (obj.gameResult.phase.includes("TURN")) {
            let my = obj.gameResult.myTurn;
            if (storedPhase != obj.gameResult.phase) {
                phaseTurn(my);
            }
            if (!my && this.scheduleNext) {
                getLongpollSessionStatus(storedToken);
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
        getSessionStatus(storedToken, true);
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
        getSessionStatus(storedToken, true);
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