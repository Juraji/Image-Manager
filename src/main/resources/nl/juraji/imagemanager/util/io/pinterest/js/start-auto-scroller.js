// noinspection JSReferencingArgumentsOutsideOfFunction
(function (args) {
    var intervalTime = args[0];

    var scrollingElement = (document.scrollingElement || document.body);
    scrollingElement.scrollTop = scrollingElement.scrollHeight;

    if (window.IMAGE_MANAGER_AUTO_SCROLL !== undefined) {
        console.info("Stopping previous AutoScroll with timerId: " + window.IMAGE_MANAGER_AUTO_SCROLL);
        clearInterval(window.IMAGE_MANAGER_AUTO_SCROLL);
    }

    console.info("Setting up AutoScroll at once every " + intervalTime + "ms");
    window.IMAGE_MANAGER_AUTO_SCROLL = setInterval(function () {
        if (scrollingElement.scrollTop !== scrollingElement.scrollHeight) {
            scrollingElement.scrollTop = scrollingElement.scrollHeight;
        }
    }, intervalTime);

    console.info("Scrolling to bottom every " + intervalTime + "ms (timerId: " + window.IMAGE_MANAGER_AUTO_SCROLL + ")")
})(arguments);
