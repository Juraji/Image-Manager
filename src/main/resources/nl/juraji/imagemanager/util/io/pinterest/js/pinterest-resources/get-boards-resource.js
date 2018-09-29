/**
 * Args[]:
 * 0: profile username
 */
// noinspection JSReferencingArgumentsOutsideOfFunction,JSAnnotator
return (function (args) {
    var resourceUri = "/resource/BoardsResource/get/";
    var resourceQueryParameters = {
        source_url: "/" + args[0] + "/",
        data: JSON.stringify({
            options: {
                filter: "public",
                limit: 500,
                sort: "alphabetical",
                field_set_key: "profile_grid_item",
                skip_board_create_rep: true,
                username: args[0]
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
            if (responseData.resource_response.hasOwnProperty("data")) {
                return responseData.resource_response.data;
            }
        }
    }

    return [];
})(arguments);