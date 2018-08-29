(function (args) {
    var intervalTime = args[0];

    var scrollingElement = (document.scrollingElement || document.body);
    scrollingElement.scrollTop = scrollingElement.scrollHeight;

    console.info("Setting up auto-scroller at once every " + args[0] + "ms");
    setInterval(function () {
        if (scrollingElement.scrollTop != scrollingElement.scrollHeight) {
            scrollingElement.scrollTop = scrollingElement.scrollHeight;
        }
    }, intervalTime);
})(arguments);
