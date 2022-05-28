function getToken() {
    let xhr = new XMLHttpRequest();
    xhr.onload = getTokenHandler;
    xhr.open('GET', '/api/getToken', true);
    xhr.send();
}

function getTokenHandler() {
    handleApi(this, obj => {
        storedToken = obj.token;
        document.cookie = "au=" + storedToken + "; max-age=3000";
        document.getElementById("debugToken").innerText = storedToken;
        if (queuedAction == "gs") {
            getSessions();
        } else if (queuedAction == "jg") {
            queuedAction = "rl";
            joinSession(storedSessionId);
            return;
        } else if (queuedAction == "gr") {
            getRules();
        }
        queuedAction = null;
    }, () => {});
}

function getSessions() {
    let xhr = new XMLHttpRequest();
	xhr.onload = getSessionsHandler;
	xhr.open('GET', '/api/getSessions?token=' + storedToken, true);
	xhr.send();
}

function getSessionsHandler() {
    handleApi(this, obj => {
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
    }, () => {
        queuedAction = "gs";
        getToken();
    });
}

function createSession() {
    let pwParam = "";
    let pwValue = document.getElementById("pwdContent").value;
    if (pwdEnable && pwValue != null)
        pwParam = "&pw=" + pwValue;
    let xhr = new XMLHttpRequest();
	xhr.onload = createSessionHandler;
	xhr.open('GET', '/api/registerSession?token=' + storedToken + pwParam, true);
	xhr.send();
}

function createSessionHandler() {
    handleApi(this, obj => {
        storedSessionId = obj.sessionInfos[0].sessionId;
        if (queuedAction != null) {
            initAi(queuedAction);
            queuedAction = null;
        }
        startSessionInterval(storedSessionId);
    }, () => {});
}

function joinSession(sid) {
    let pwParam = "";
    if (pwdEnable && storedRpw != null)
        pwParam = "&pw=" + storedRpw;
    let xhr = new XMLHttpRequest();
	xhr.onload = joinSessionHandler;
	xhr.open('GET', '/api/joinSession?token=' + storedToken + "&sessionId=" + sid + pwParam, true);
	xhr.send();
	storedSessionId = sid;
}

function joinSessionHandler() {
    handleApi(this, obj => {
        startSessionInterval(storedSessionId);
        return;
    }, () => {
        getLongpollSessionStatus(storedSessionId);
    })
}

function getSessionStatus() {
    getSessionStatus(false);
}

function getSessionStatus(scheduleNext) {
    let xhr = new XMLHttpRequest();
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
    let xhr = new XMLHttpRequest();
    xhr.onload = getStatusHandler;
    xhr.scheduleNext = true;
    xhr.spectateMode = sessionId != null;
    xhr.open('GET', '/api/longpoll/getSessionStatus?token=' + storedToken + sidParam, true);
    xhr.send();
}

function getStatusHandler() {
    if (this.status == 200) {
        let obj = JSON.parse(this.responseText);
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
    let xhr = new XMLHttpRequest();
    xhr.onload = placeShipHandler;
    xhr.open('GET', '/api/placeShip?token=' + storedToken
            + "&position=" + pos
            + "&size=" + size
            + "&vertical=" + vert, true);
    xhr.send();
}

function placeShipHandler() {
    let scheduleNext = false;
    handleApi(this, obj => {
        scheduleNext = true;
        return;
    }, () => {});
    reqUpdate = true;
    getSessionStatus(scheduleNext);
}

function tryShoot(pos) {
    let xhr = new XMLHttpRequest();
    xhr.onload = shootHandler;
    xhr.open('GET', '/api/shoot?token=' + storedToken
            + "&position=" + pos, true);
    xhr.send();
}

function shootHandler() {
    handleApi(this, obj => {
        reqUpdate = true;
        document.getElementById('shipRotate').innerHTML = obj.info;
        getSessionStatus(true);
    }, () => {})
}

function initAi(aiType) {
    let xhr = new XMLHttpRequest();
    xhr.onload = aiHandler;
    xhr.open('GET', '/api/initAI?token=' + storedToken
            + "&sessionId=" + storedSessionId
            + "&sessionPw=" + storedRpw
            + (aiType == "ah" ? "&skill=true" : ""), true);
    xhr.send();
}

function aiHandler() {
    handleApi(this, obj => {}, () => {});
}

function getRules() {
    let xhr = new XMLHttpRequest();
    xhr.onload = rulesHandler;
    xhr.open('GET', '/api/getRules?token=' + storedToken, true);
    xhr.send();
}

function rulesHandler() {
    handleApi(this, obj => {
        document.getElementById("debugRules").innerHTML = obj.info.replaceAll("\n", "<br>");
    }, () => {
        queuedAction = "gr";
        getToken();
    });
}

function handleFault(msg) {
    document.getElementById("debugStatus").innerHTML = msg;
}

function httpFault() {
    document.getElementById("debugStatus").innerHTML = "Service error. Page reloading probably help to fix this issue.";
}

function handleApi(response, main, fault) {
    if (response.status == 200) {
        let obj = JSON.parse(response.responseText);
        if (!obj.success) {
            fault();
            handleFault(obj.info);
            return;
        }
        main(obj);
        return;
    }
    httpFault();
}