var imagePath = 'images/';
var selectedElement=null;

var firstChange;
var lastChange;

dojo.event.connect(document, "onkeydown", handleShortcut);
dojo.addOnLoad(init_diff);

function init_diff(){
    connectEvents();
    addTopBar();
    updateOverlays();
}

function addTopBar(){
    topBar = document.createElement("div");
    topBar.className= "diff-topbar";
    topBar.style.width = "100%";
    topBar.innerHTML = " <table class='diffpage-html-firstlast'><tr><td style='text-align: left;'>"+
                "<a onclick='scrollToEvent(event)' id='first-diff' href='#"+firstChange.getAttribute("id")+"' next='"+firstChange.getAttribute("id")+"'>"+
                  "<img class='diff-icon' src='"+imagePath+"diff-first.gif' title='Go to first change.'/>"+
                "</a>"+
                "<a onclick='scrollToEvent(event)' href='#"+firstChange.getAttribute("id")+"'>&#160;first"+
                "</a>"+
            "</td>"+
            "<td style='text-align: center; font-size: 140%;'>"+
                "<a href='http://code.google.com/p/daisydiff/'>Daisy Diff</a> compare report.<br/>"+
                "<span style='font-style: italic; font-size: 70%;'>Click on the changed parts for a detailed description. Use the left and right arrow keys to walk through the modifications.</span>"+
            "</td>"+
            "<td style='text-align: right;'>"+
                "<a onclick='scrollToEvent(event)' href='#"+lastChange.getAttribute("id")+"'>"+
                  "last&#160;"+
                "</a>"+
                "<a onclick='scrollToEvent(event)' id='last-diff' href='#"+lastChange.getAttribute("id")+"' previous='"+lastChange.getAttribute("id")+"'>"+
                  "<img class='diff-icon' src='"+imagePath+"diff-last.gif' title='Go to last change.'/>"+
                "</a>"+
             "</td></tr></table>";
    document.body.insertBefore(topBar, document.body.firstChild);
    selectedElement = document.getElementById("first-diff");
}

function connectEvents(){
    var allSpans = document.getElementsByTagName('span');
    // Walk through the list
    for (var i=0;i<allSpans.length;i++) {
        var span = allSpans[i];
        if (span.className == "diff-html-changed" || span.className == "diff-html-added" || span.className == "diff-html-removed") {
            dojo.event.connect(span, "onclick", "clickHandler");
            if(span.getAttribute("id"))
                setFirstIfEmpty(span);
        }  
    }
    var allImgs = document.getElementsByTagName('img');
    // Walk through the list
    for (var i=0;i<allImgs.length;i++) {
        var img = allImgs[i];
        if (img.className == "diff-removed-image" || img.className == "diff-added-image") {
            dojo.event.connect(img, "onload", "updateOverlays");
            dojo.event.connect(img, "onerror", "updateOverlays");
            dojo.event.connect(img, "onabort", "updateOverlays");
        }  
    }
}

function setFirstIfEmpty(node){
    if(!firstChange){
        firstChange=node;
    }
    lastChange=node;
}

function updateOverlays(){
    
    var images = document.getElementsByTagName("img");
    
    for (var i = 0; i < images.length; i++) {
        var image = images [i];
        if (image.getAttribute('changeType') == "diff-removed-image" || image.getAttribute('changeType') == "diff-added-image") {
            var filter;
            var existingDivs = image.parentNode.getElementsByTagName('div');
            if(existingDivs.length > 0 && existingDivs[0].className==image.getAttribute("changeType")){
                filter = existingDivs[0];
            }else {
                filter = document.createElement("div");
                filter.className= image.getAttribute("changeType");
            }
            filter.style.width = image.offsetWidth-4 + "px";
            filter.style.height = image.offsetHeight-4 + "px";
            if (image.y && image.x) { // this check is needed for IE
                filter.style.top = image.y + "px";
                filter.style.left = image.x-1 + "px";
            }


            if(existingDivs.length == 0 ){
                image.parentNode.insertBefore(filter, image);
            }
        }
    }
}

function clickHandler(e){

    if (window.event) {
        target = window.event.srcElement;
    } else if (e) {
        target = e.target;
    } else {
        throw("unsupported browser");
        return true;
    }
    
    // Make sure that the target is an element, not a text node
    // within an element
    while (target && (target.nodeType == 3 || target.nodeName.toLowerCase()!="span")) {
        target = target.parentNode;
    }
    
    // And stop the actual click happening
    if (window.event) {
        window.event.cancelBubble = true;
        window.event.returnValue = false;
    }
    if (e && e.preventDefault && e.stopPropagation) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    return tip1(target);
    
}

function tip1(destinationLink){
    if(destinationLink.className =='diff-html-changed'){
        return tipC1(constructToolTipC(destinationLink));
    }else if(destinationLink.className =='diff-html-added'){
        return tipA1(constructToolTipA(destinationLink));
    }else if(destinationLink.className =='diff-html-removed'){
        return tipR1(constructToolTipR(destinationLink));
    }
}



function tipA1(content){
    Tip(content, DURATION, 6000, CLICKCLOSE, true, FOLLOWMOUSE, false, ABOVE, true, OFFSETX , 1, STICKY, true, FADEIN, 100, FADEOUT, 100, PADDING, 5);
    return false;
}

function tipR1(content){
    Tip(content, DURATION, 6000, CLICKCLOSE, true, FOLLOWMOUSE, false, ABOVE, true, OFFSETX , 1, STICKY, true, FADEIN, 100, FADEOUT, 100, PADDING, 5);
    return false;
}

function tipC1(content){
    Tip(content, DURATION, 8000, CLICKCLOSE, true, FOLLOWMOUSE, false, ABOVE, true, STICKY, true, OFFSETX, 1, FADEIN, 100, FADEOUT, 100, PADDING, 5);
    return false;
}

function constructToolTipC(elem){
    
   //constructing the tooltip, so this must be the new selected element!
   selectedElement=elem;

   var changes_html = elem.getAttribute("changes");
   var previous_id = elem.getAttribute("previous");
   var next_id = elem.getAttribute("next");
   var change_id = elem.getAttribute("changeId");
   return changes_html+
          "<table class='diff-tooltip-link-changed'>"+
          "  <tr>"+
          "    <td class='diff-tooltip-prev'>"+
          "      <a href=#"+previous_id+" onClick='scrollToEvent(event)'><img class='diff-icon' src='"+imagePath+"diff-previous.gif' title='Go to previous.'/></a>"+
          "    </td>"+
          "    <td>"+
          "      &#160;<a href='#"+change_id+"'>#"+change_id+"</a>&#160;"+
          "    </td>"+
          "    <td class='diff-tooltip-next'>"+
          "      <a href='#"+next_id+"' onClick='scrollToEvent(event)'><img class='diff-icon' src='"+imagePath+"diff-next.gif' title='Go to next.'/></a>"+
          "    </td>"+
          "  </tr>"+
          "</table>";
}

function constructToolTipA(elem){
   
   //constructing the tooltip, so this must be the new selected element!
   selectedElement=elem;
   
   var previous_id = elem.getAttribute("previous");
   var next_id = elem.getAttribute("next");
   var change_id = elem.getAttribute("changeId");
   return "<table class='diff-tooltip-link'>"+
          "  <tr>"+
          "    <td class='diff-tooltip-prev'>"+
          "      <a href=#"+previous_id+" onClick='scrollToEvent(event)'><img class='diff-icon' src='"+imagePath+"diff-previous.gif' title='Go to previous.'/></a>"+
          "    </td>"+
          "    <td>"+
          "      &#160;<a href='#"+change_id+"'>#"+change_id+"</a>&#160;"+
          "    </td>"+
          "    <td class='diff-tooltip-next'>"+
          "      <a href='#"+next_id+"' onClick='scrollToEvent(event)'><img class='diff-icon' src='"+imagePath+"diff-next.gif' title='Go to next.'/></a>"+
          "    </td>"+
          "  </tr>"+
          "</table>";
}

function constructToolTipR(elem){
   
   //constructing the tooltip, so this must be the new selected element!
   selectedElement=elem;
   
   var previous_id = elem.getAttribute("previous");
   var next_id = elem.getAttribute("next");
   var change_id = elem.getAttribute("changeId");
   return "<table class='diff-tooltip-link'>"+
          "  <tr>"+
          "    <td class='diff-tooltip-prev'>"+
          "      <a href=#"+previous_id+" onClick='scrollToEvent(event)'><img class='diff-icon' src='"+imagePath+"diff-previous.gif' title='Go to previous.'/></a>"+
          "    </td>"+
          "    <td>"+
          "      &#160;<a href='#"+change_id+"'>#"+change_id+"</a>&#160;"+
          "    </td>"+
          "    <td class='diff-tooltip-next'>"+
          "      <a href='#"+next_id+"' onClick='scrollToEvent(event)'><img class='diff-icon' src='"+imagePath+"diff-next.gif' title='Go to next.'/></a>"+
          "    </td>"+
          "  </tr>"+
          "</table>";
}

function tip2(anchor){
    var destinationLink = document.getElementById(anchor);
    if(destinationLink.className =='diff-html-changed'){
        tipC2(destinationLink, constructToolTipC(destinationLink));
    }else if(destinationLink.className =='diff-html-added'){
        tipA2(destinationLink, constructToolTipA(destinationLink));
    }else if(destinationLink.className =='diff-html-removed'){
        tipR2(destinationLink, constructToolTipR(destinationLink));
    }else{
        //the anchor points to a 'first-...' or 'last-...' link; make it selected
        selectedElement=destinationLink;
    }
}

function tipC2(destinationLink, content){
    Tip(content, FIX, FixCalcXY(destinationLink, 2,20), BALLOONSTEMWIDTH, 0, BALLOONSTEMHEIGHT, 0, DURATION, 20000, CLICKCLOSE, true, FOLLOWMOUSE, false, STICKY, true, OFFSETX, 1, FADEIN, 100, FADEOUT, 100, PADDING, 5);
}
    
function tipR2(destinationLink, content){
    Tip(content, FIX, FixCalcXY(destinationLink, 2,20), BALLOONSTEMWIDTH, 0, BALLOONSTEMHEIGHT, 0, DURATION, 20000, CLICKCLOSE, true, FOLLOWMOUSE, false, STICKY, true, OFFSETX, 1, FADEIN, 100, FADEOUT, 100, PADDING, 5);
}
    
function tipA2(destinationLink, content){
    Tip(content, FIX, FixCalcXY(destinationLink, 2,20), BALLOONSTEMWIDTH, 0, BALLOONSTEMHEIGHT, 0, DURATION, 20000, CLICKCLOSE, true, FOLLOWMOUSE, false, STICKY, true, OFFSETX, 1, FADEIN, 100, FADEOUT, 100, PADDING, 5);
}
    
function FixCalcXY(el, xoffset, yoffset){
    
    //fix for images inside the span
    var imagesContained = el.getElementsByTagName("img");
    
    var imageHeight=0;
    if(!window.event && imagesContained.length > 0){
    imageHeight=imagesContained[0].offsetHeight;
    }
    
    var xy = dojo.html.getAbsolutePosition(el, true);
    return [xy.x+xoffset, xy.y+yoffset-imageHeight];
}
    
function changeClass(changeId, newCl){
    var allSpans = document.getElementsByTagName('span');
    // Walk through the list
    for (var i=0;i<allSpans.length;i++) {
        var lnk = allSpans[i];
        if (lnk.getAttribute("changeId") == changeId) {
            lnk.className=newCl;
        }
    }
}

function handleShortcut(e){
    var keynum, target;
    
    if (window.event) {
        target = window.event.srcElement;
    } else if (e) {
        target = e.target;
    } else {
        return;
    }
    if(target.tagName.toLowerCase()=="input"){
        return;
    }
    
    if(window.event) // IE
    {
        keynum = e.keyCode;
    }
    else if(e.which) // Netscape/Firefox/Opera
    {
        keynum = e.which;
    }
    
    var isPrev=(keynum==83 || keynum==37 || keynum==80);// s, <- and p
    var isNext=(keynum==68 || keynum==39 || keynum==78);// d, -> and n
    
    var target;
    
    if(isPrev){
        target=selectedElement.getAttribute("previous");
    }else if(isNext){
        target=selectedElement.getAttribute("next");
    }
    
    //custom hack for span support
    var destinationLink = document.getElementById(target);
    
    // If we didn't find a destination, give up and let the browser do
    // its thing
    if (!destinationLink){
        return;
    }
    scrollToTarget(destinationLink);
}

function scrollToEvent(e) {
    // This is an event handler; get the clicked on element,
    // in a cross-browser fashion
    var target;
    
    if (window.event) {
    target = window.event.srcElement;
    } else if (e) {
    target = e.target;
    } else {
    throw("unsupported browser");
    return true;
    }
    // Make sure that the target is an element, not a text node
    // within an element
    if (target.nodeType == 3 || target.nodeName.toLowerCase()=="img") {
        target = target.parentNode;
    }
    
    // Paranoia; check this is an A tag
    if (target.nodeName.toLowerCase() != 'a'){
        throw("target is not an anchor");
        return true;
    }
    // Find the <span> tag corresponding to this href
    // First strip off the hash (first character)
    anchor = target.hash.substr(1);
    
    //custom hack for span support
    var destinationLink = document.getElementById(anchor);
    
    var continueEvent = scrollToTarget(destinationLink); 
    
    // And stop the actual click happening
    if (!continueEvent && window.event) {
    window.event.cancelBubble = true;
    window.event.returnValue = false;
    }
    if (!continueEvent && e && e.preventDefault && e.stopPropagation) {
    e.preventDefault();
    e.stopPropagation();
    }
    return continueEvent;
}
    
function scrollToTarget(destinationLink){
    
    // If we didn't find a destination, give up and let the browser do
    // its thing
    if (!destinationLink){
        throw("unknown destination");
        return true;
    }
    var xy = FixCalcXY(destinationLink, 0,-10);

    //scroll
    window.scrollTo( xy[0], xy[1]);

    setTimeout("tip2('"+destinationLink.id+"')", 0);
    
    //if, because otherwise it's not threadsafe-ish
    if(destinationLink.className != "diff-html-selected"){
    
        if(selectedElement.getAttribute("oldClass") && selectedElement.getAttribute("oldClass").length>0 && selectedElement.getAttribute("oldClass")!=selectedElement.className){
            changeClass(selectedElement.id, selectedElement.getAttribute("oldClass"));
        }
        
        setTimeout("changeClass('"+destinationLink.id+"', 'diff-html-selected')", 1);
        destinationLink.setAttribute("oldClass",destinationLink.className)
        setTimeout("changeClass('"+destinationLink.id+"', '"+destinationLink.className+"')", 2000);
    }
    return false;
}
