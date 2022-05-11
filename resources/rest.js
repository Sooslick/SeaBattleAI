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
            getSessions();
        } else if (queuedAction == "jg") {
            joinSession(storedSessionId);
        } else if (queuedAction == "gr") {
            getRules();
        }
        queuedAction = null;
        return;
    }
    httpFault();
}

function getSessions() {
    xhr = new XMLHttpRequest();
	xhr.onload = getSessionsHandler;
	xhr.open('GET', '/api/getSessions?token=' + storedToken, true);
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

function createSession() {
    pwParam = "";
    pwValue = document.getElementById("pwdContent").value;
    if (pwdEnable && pwValue != null)
        pwParam = "&pw=" + pwValue;
    xhr = new XMLHttpRequest();
	xhr.onload = createSessionHandler;
	xhr.open('GET', '/api/registerSession?token=' + storedToken + pwParam, true);
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
        if (queuedAction != null) {
            initAi(queuedAction);
            queuedAction = null;
        }
        startSessionInterval(storedSessionId);
        return;
    }
    httpFault();
}

function joinSession(sid) {
    pwParam = "";
    if (pwdEnable && storedRpw != null)
        pwParam = "&pw=" + storedRpw;
    xhr = new XMLHttpRequest();
	xhr.onload = joinSessionHandler;
	xhr.open('GET', '/api/joinSession?token=' + storedToken + "&sessionId=" + sid + pwParam, true);
	xhr.send();
	storedSessionId = sid;
}

function joinSessionHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
            getLongpollSessionStatus(storedSessionId);
            return;
        }
        startSessionInterval(storedSessionId);
        return;
    }
    storedSessionId = null;
    httpFault();
}

function getSessionStatus() {
    getSessionStatus(false);
}

function getSessionStatus(scheduleNext) {
    xhr = new XMLHttpRequest();
    xhr.onload = getStatusHandler;
    xhr.scheduleNext = scheduleNext;
    xhr.spectateMode = false;
    xhr.open('GET', '/api/getSessionStatus?token=' + storedToken, true);
    xhr.send();
}

function getLongpollSessionStatus() {
    getLongpollSessionStatus(null);
}

function getLongpollSessionStatus(sessionId) {
    let sidParam = sessionId == null ? "" : "&sessionId=" + sessionId;
    xhr = new XMLHttpRequest();
    xhr.onload = getStatusHandler;
    xhr.scheduleNext = true;
    xhr.spectateMode = sessionId != null;
    xhr.open('GET', '/api/longpoll/getSessionStatus?token=' + storedToken + sidParam, true);
    xhr.send();
}

function getStatusHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
            return;
        }
        document.getElementById("matchLog").innerHTML = obj.gameResult.matchLog.replaceAll("\n", "<br>");
        if (obj.gameResult.phase == "PREPARE") {
            if (storedPhase != "PREPARE")
                phasePrepare();
            if (this.spectateMode) {
                getLongpollSessionStatus(storedSessionId);
                return;
            }
            updateSelector(obj.gameResult.ships);
            if (reqUpdate) {
                updateField(true, obj.gameResult.myField);
                reqUpdate = false;
            }
            if (!obj.gameResult.myTurn && this.scheduleNext) {
                getLongpollSessionStatus();
            }
        } else if (obj.gameResult.phase.includes("TURN")) {
            let my = obj.gameResult.myTurn;
            if (storedPhase != obj.gameResult.phase) {
                phaseTurn(my);
            }
            if (!my && this.scheduleNext) {
                getLongpollSessionStatus(this.spectateMode ? storedSessionId : null);
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

function placeShip(pos, size, vert) {
    xhr = new XMLHttpRequest();
    xhr.onload = placeShipHandler;
    xhr.open('GET', '/api/placeShip?token=' + storedToken
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
        getSessionStatus(true);
        return;
    }
    httpFault();
}

function tryShoot(pos) {
    xhr = new XMLHttpRequest();
    xhr.onload = shootHandler;
    xhr.open('GET', '/api/shoot?token=' + storedToken
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
        getSessionStatus(true);
        return;
    }
    httpFault();
}

function initAi(aiType) {
    xhr = new XMLHttpRequest();
    xhr.onload = aiHandler;
    xhr.open('GET', '/api/initAI?token=' + storedToken
            + "&sessionId=" + storedSessionId
            + "&sessionPw=" + storedRpw
            + (aiType == "ah" ? "&skill=true" : ""), true);
    xhr.send();
}

// todo handler duplications
function aiHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
        }
        return;
    }
    httpFault();
}

function getRules() {
    xhr = new XMLHttpRequest();
    xhr.onload = rulesHandler;
    xhr.open('GET', '/api/getRules?token=' + storedToken, true);
    xhr.send();
}

function rulesHandler() {
    if (this.status == 200) {
        obj = JSON.parse(this.responseText);
        if (!obj.success) {
            handleFault(obj.info)
        }
        document.getElementById("debugRules").innerHTML = obj.info.replaceAll("\n", "<br>");
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