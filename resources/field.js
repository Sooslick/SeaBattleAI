function updateSelector(shipArray) {
    let shipCounts = {};
    shipArray.forEach(function (x) {
        shipCounts[x] = (shipCounts[x] || 0) + 1;
    });
    let selector = document.getElementById("shipSelector");
    selector.innerHTML = "";
    let selRequired = true;
    let lastAvailable = null;
    Object.entries(shipCounts).forEach(entry => {
        lastAvailable = entry[0];
        let span = document.createElement('span');
        span.setAttribute("id", "ship" + entry[0]);
        span.setAttribute("onclick", "select(" + entry[0] + ")");
        let className = "clickable";
        if (entry[0] == storedSelector) {
            className+= " selected";
            selRequired = false;
        }
        span.className = className;
        span.innerHTML = "&#18;" + entry[0] + " x" + entry[1];
        selector.appendChild(span);
    });
    if (selRequired && lastAvailable != null) {
        selDesel(() => select(lastAvailable));
    }
}

function select(newval) {
    let oldSpan = document.getElementById("ship" + storedSelector);
    if (oldSpan != undefined)
        oldSpan.className = "clickable";
    storedSelector = newval;
    document.getElementById("ship" + newval).className = "clickable selected";
}

function rotate() {
    selDesel(() => {
        storedDirection = !storedDirection;
        document.getElementById("shipRotate").innerHTML = storedDirection ? "|" : "-";
    });
}

function hover(enable, x, y) {
    // x - rows, y - cols
    let xmult = storedDirection ? 1 : 0;
    let ymult = storedDirection ? 0 : 1;
    let xlimiter = x + (storedSelector - 1) * xmult;
    let ylimiter = y + (storedSelector - 1) * ymult;
    if (xlimiter > 9) xlimiter = 9;
    if (ylimiter > 9) ylimiter = 9;
    for (i = x; i <= xlimiter; i++) {
        for (j = y; j <= ylimiter; j++) {
            let cellObj = document.getElementById(i + "-" + j);
            cellObj.className = enable ? cellObj.className + " aim" : cellObj.className.replace(" aim", "");
        }
    }
    if (enable) {
        storedX = x;
        storedY = y;
    } else {
        storedX = -1;
        storedY = -1;
    }
}

function selDesel(action) {
    let x = storedX;
    let y = storedY;
    if (x >= 0)
        hover(false, x, y);
    action();
    if (x >= 0)
        hover(true, x, y);
}

function clickOwn(x, y) {
    if (storedPhase != "PREPARE")
        return;
    let pos = String.fromCharCode(y + 97) + (x+1);
    placeShip(pos, storedSelector, storedDirection);
    let xmult = storedDirection ? 1 : 0;
    let ymult = storedDirection ? 0 : 1;
    let xlimiter = x + (storedSelector - 1) * xmult;
    let ylimiter = y + (storedSelector - 1) * ymult;
    if (xlimiter > 9) return;
    if (ylimiter > 9) return;
    for (i = x; i <= xlimiter; i++) {
        for (j = y; j <= ylimiter; j++) {
            let cellObj = document.getElementById(i + "-" + j);
            cellObj.className = "cell place";
        }
    }
}

function clickEnemy(x, y) {
    if (!storedPhase.includes("TURN"))
        return;
    let pos = String.fromCharCode(y + 97) + (x+1);
    tryShoot(pos);
    let cellObj = document.getElementById(x + ", " + y);
    cellObj.className = cellObj.className + " shot";
}

function updateField(my, field) {
    let separator = my ? "-" : ", ";
    for (i = 0; i < 10; i++) {
        let row = field.rows[i];
        for (j = 0; j < 10; j++) {
            let val = row.cols[j];
            let place = val > 1;
            let shoot = val % 2 == 1;
            let cell = document.getElementById(i + separator + j);
            cell.className = cell.className.replace(" place", "").replace(" shot", "")
                    + (place ? " place" : "")
                    + (shoot ? " shot" : "");
        }
    }
}

storedSelector = 4;
storedDirection = false;
storedX = -1;
storedY = -1;
reqUpdate = false;
document.getElementById("myField").addEventListener('contextmenu', e => {
    e.preventDefault();
    rotate();
});