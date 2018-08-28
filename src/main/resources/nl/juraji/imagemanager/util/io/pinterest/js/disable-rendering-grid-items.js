var currentStyleEl = document.getElementById("disable-rendering-grid-items");
if (!currentStyleEl) {
    var content = 'div[data-grid-item=true] { display: none !important; }';
    var style = document.createElement('style');
    style.id = "disable-rendering-grid-items";
    style.type = 'text/css';
    style.appendChild(document.createTextNode(content));
    document.head.appendChild(style);
}