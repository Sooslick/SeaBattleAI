function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
}

function init(startMenu) {
    currentMenu = "loading";
    loadMenu(startMenu);
    document.getElementById("debugToken").innerText = storedToken;
    if (storedSessionId != null) {
        document.getElementById("debugSid").innerText = storedSessionId;
        document.getElementById("bRefreshToken").hidden = true;
    }
    document.getElementById("pwdContent").value = makeId(4);
}

function loadMenu(newMenuId) {
    document.getElementById(currentMenu).hidden = true;
    currentMenu = newMenuId;
    document.getElementById(currentMenu).hidden = false;
}

function loadMainMenu() {
    loadMenu('mainmenu');
    document.getElementById("loading").hidden = false;
    getSessions(storedToken);
}

function loadGame(sid) {
    loadMenu("game");
    document.getElementById("bRefreshToken").hidden = true;
    if (sid < 0)
        createSession(storedToken);
    else
        joinSession(storedToken, sid);
}

function startSessionInterval(sid) {
    document.cookie = "sessionId=" + storedSessionId + "; max-age=3000";
    document.getElementById("debugSid").innerText = storedSessionId;
    getSessionStatus(storedToken);
    storedTimer = setInterval(getSessionStatus, 5000, storedToken);
}

function phasePrepare() {
    document.getElementById("lobbyStatus").innerText = "Prepare your ships";
    document.getElementById("myField").hidden = false;
    storedPhase = "PREPARE";
}

function phaseTurn(myTurn) {
    document.getElementById("shipSelector").innerHTML = "";
    document.getElementById("lobbyStatus").innerText = myTurn ? "Your turn: guess opponent's ship" : "Wait for opponent's turn";
    document.getElementById("myField").className = myTurn ? "field" : "field highlight";
    document.getElementById("enemyField").className = myTurn ? "field highlight" : "field";
    document.getElementById("myField").hidden = false;
    document.getElementById("enemyField").hidden = false;
}

function phaseEnd() {
    document.getElementById("lobbyStatus").innerText = "Game over";
    document.getElementById("myField").hidden = false;
    document.getElementById("enemyField").hidden = false;
    storedPhase = "ENDGAME";
    clearInterval(storedTimer);
}

function leave() {
    document.cookie = "au=; max-age=0";
    document.cookie = "sessionId=; max-age=0";
    document.location.reload();
}

function pwdSelClck() {
    document.getElementById("pwdContent").hidden = pwdEnable;
    pwdEnable = !pwdEnable;
    if (pwdEnable) {
        document.getElementById("pwdSelector").style.backgroundColor = "#242";
        document.getElementById("pwdPosition").style.margin = "0 0 0 27";
        document.getElementById("pwdLabel").innerHTML = "Disable password";
    } else {
        document.getElementById("pwdSelector").style.backgroundColor = "#222";
        document.getElementById("pwdPosition").style.margin = "0";
        document.getElementById("pwdLabel").innerHTML = "Enable password";
    }
}

function pwdInputClck() {
    if (pwdFirstClick) {
        pwdFirstClick = false;
        document.getElementById("pwdContent").value = "";
    }
}

function makeId(length) {
    let result = '';
    let characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let charactersLength = characters.length;
    for (let i = 0; i < length; i++)
        result+= characters.charAt(Math.floor(Math.random() * charactersLength));
    return result;
}

// onload /////

storedToken = getCookie('au');
storedSessionId = getCookie('sessionId');
storedPhase = "LOOKUP";
queuedAction = null;
pwdEnable = false;
pwdFirstClick = true;
if (storedToken == undefined) {
    getToken();
    storedSessionId = null;
}
if (storedSessionId != undefined) {
    init("game");
    reqUpdate = true;
    startSessionInterval(storedSessionId);
} else
    init("start");