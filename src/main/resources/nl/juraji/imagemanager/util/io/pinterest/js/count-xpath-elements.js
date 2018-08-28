// noinspection JSAnnotator, JSReferencingArgumentsOutsideOfFunction
return document.evaluate('count(' + arguments[0] + ')', document, null, XPathResult.ANY_TYPE, null).numberValue;