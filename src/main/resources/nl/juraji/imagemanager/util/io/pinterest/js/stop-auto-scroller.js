(function () {
    if (window.IMAGE_MANAGER_AUTO_SCROLL !== undefined) {
        console.info("Stopping previous AutoScroll with timerId: " + window.IMAGE_MANAGER_AUTO_SCROLL);
        clearInterval(window.IMAGE_MANAGER_AUTO_SCROLL);
        window.IMAGE_MANAGER_AUTO_SCROLL = undefined;
    } else {
        console.warn("AutoScroll stop requested, but none were active")
    }
})();
