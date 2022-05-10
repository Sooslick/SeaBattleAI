function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// fill fields with init values and show entry point menu
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

// hide previous menu and show new menu
function loadMenu(newMenuId) {
    document.getElementById(currentMenu).hidden = true;
    currentMenu = newMenuId;
    document.getElementById(currentMenu).hidden = false;
}

// call session list menu
function loadMainMenu() {
    loadMenu('mainmenu');
    document.getElementById("loading").hidden = false;
    getSessions();
}

// switch to ai game menu
function loadAi(skill) {
    loadMenu("game");
    document.getElementById("loading").hidden = false;
    document.getElementById("bRefreshToken").hidden = true; // todo loadgame dublicates
    storedRpw = makeId(4);
    queuedAction = skill ? "ah" : "as";
    createSession();
}

// switch to game menu
function loadGame(sid) {
    loadMenu("game");
    document.getElementById("loading").hidden = false;
    document.getElementById("bRefreshToken").hidden = true;
    if (sid < 0) {
        storedRpw = document.getElementById("pwdContent").value;
        document.cookie = "rpw=" + storedRpw + "; max-age=600";
        createSession();
    } else {
        storedRpw = document.getElementById("pwdContent").value;
        joinSession(sid);
    }
}

// init game menu and init status requests
function startSessionInterval(sid) {
    document.cookie = "sessionId=" + storedSessionId + "; max-age=3000";
    document.getElementById("debugSid").innerText = storedSessionId;
    getLongpollSessionStatus();
    document.getElementById("loading").hidden = true;
}

function phasePrepare() {
    phase("PREPARE", "Prepare your ships");
    document.getElementById("myField").className = "field highlight";
}

function phaseTurn(myTurn) {
    phase("TURN", myTurn ? "Your turn: guess opponent's ship" : "Wait for opponent's turn");
    document.getElementById("shipSelector").innerHTML = "";
    document.getElementById("myField").className = "field";
    document.getElementById("enemyField").className = myTurn ? "field highlight" : "field";
}

function phaseEnd() {
    phase("ENDGAME", "Game over");
    clearInterval(storedTimer);
}

function phase(phaseName, phaseText) {
    document.getElementById("bInvite").hidden = true;
    document.getElementById("lobbyStatus").innerText = phaseText;
    storedPhase = phaseName;
}

function leave() {
    document.cookie = "au=; max-age=0";
    document.cookie = "sessionId=; max-age=0";
    document.cookie = "rpw=; max-age=0";
    window.location = window.location.href.split("?")[0];
}

// switch animation from themegen
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

// clear generated pwd for user prompt
function pwdInputClck() {
    if (pwdFirstClick) {
        pwdFirstClick = false;
        document.getElementById("pwdContent").value = "";
    }
}

// custom event handler for copy event
function inviteLink(e) {
    let link = window.location.href + "?invite=" + storedSessionId + (pwdEnable ? "&rpw=" + storedRpw : "");
    e.preventDefault();
    if (e.clipboardData) {
        e.clipboardData.setData("text/plain", link);
        document.getElementById("bInvite").innerHTML = "Copied!";
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

// init variables
storedToken = getCookie('au');
storedSessionId = getCookie('sessionId');
storedRpw = null;
storedPhase = "LOOKUP";
queuedAction = null;
pwdEnable = false;
pwdFirstClick = true;
reqUpdate = false;

// read query
document.getElementById("bInvite").oncopy = inviteLink;
params = new Proxy(new URLSearchParams(window.location.search), {
    get: (searchParams, prop) => searchParams.get(prop),
});

// join by invite
if (params.invite) {
    init("game");
    if (params.rpw) {
        storedRpw = params.rpw;
        pwdEnable = true;
    }
    storedSessionId = params.invite;
    queuedAction = "jg";
    getToken();
}
// fresh load (no token cookie)
else if (storedToken == undefined) {
    getToken();
    storedSessionId = null;
    init("start");
}
// token & session cookies present
else if (storedSessionId != undefined) {
    init("game");
    storedRpw = getCookie('rpw')
    if (storedRpw != null)
        pwdEnable = true;
    reqUpdate = true;
    startSessionInterval(storedSessionId);
}
// only token cookie presents
else
    init("start");