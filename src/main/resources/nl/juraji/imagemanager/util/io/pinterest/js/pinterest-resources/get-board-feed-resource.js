/**
 * Args[]:
 * 0: board id
 * 1: bookmark
 */
// noinspection JSReferencingArgumentsOutsideOfFunction, JSAnnotator
return (function (args) {
    var pageSize = 100;
    var resourceUri = "/resource/BoardFeedResource/get/";
    var resourceQueryParameters = {
        source_url: args[1],
        data: JSON.stringify({
            options: {
                bookmarks: args[1] ? [args[1]] : undefined,
                board_id: args[0],
                board_url: "",
                field_set_key: "react_grid_pin",
                filter_section_pins: true,
                layout: "default",
                page_size: pageSize,
                redux_normalize_feed: true
            },
            context: {}
        }),
        _: (new Date()).getTime()
    };

    var queryParameterString = "?" + Object.keys(resourceQueryParameters)
        .map(function (key) {
            return encodeURIComponent(key) + "=" + encodeURIComponent(resourceQueryParameters[key]);
        })
        .join("&");

    var request = new XMLHttpRequest();
    request.open('GET', resourceUri + queryParameterString, false); // `false` makes the request synchronous
    request.setRequestHeader("X-APP-VERSION", "d9882d5");
    request.setRequestHeader("X-Pinterest-AppState", "active");
    request.setRequestHeader("X-Requested-With", "XMLHttpRequest");

    request.send();

    if (request.status === 200) {
        var responseData = JSON.parse(request.responseText);

        if (responseData.hasOwnProperty("resource_response")) {
            return {
                bookmark: responseData.resource.options.bookmarks[0],
                pins: responseData.resource_response.data
            };
        }
    }

    return {};
})(arguments);