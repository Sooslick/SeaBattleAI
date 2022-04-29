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
    storedTimer = setInterval(getSessionStatus, 5000, storedToken);
}

function phasePrepare() {
    document.getElementById("lobbyStatus").innerText = "Prepare your ships";
    document.getElementById("myField").hidden = false;
    storedPhase = "PREPARE";
}

function phaseTurn(myTurn) {
    document.getElementById("lobbyStatus").innerText = myTurn ? "Your turn: guess opponent's ship" : "Wait for opponent's turn";
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

// onload /////

storedToken = getCookie('au');
storedSessionId = getCookie('sessionId');
storedPhase = "LOOKUP";
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