load("/build_tools/js_binary", "js_binary")

js_binary(
    name = "dossier_bin",
    srcs = [
        "dossier.js",
        "keyhandler.js",
        "nav.js",
        "search.js",
    ],
    externs = ["externs.js"],
    flags = [
        "--charset=UTF-8",
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--define=goog.DEBUG=false",
        "--manage_closure_dependencies",
        "--closure_entry_point=dossier",
        "--jscomp_error=accessControls",
        "--jscomp_error=ambiguousFunctionDecl",
        "--jscomp_error=checkRegExp",
        "--jscomp_error=checkTypes",
        "--jscomp_error=checkVars",
        "--jscomp_error=constantProperty",
        "--jscomp_error=deprecated",
        "--jscomp_error=duplicateMessage",
        "--jscomp_error=es5Strict",
        "--jscomp_error=externsValidation",
        "--jscomp_error=fileoverviewTags",
        "--jscomp_error=globalThis",
        "--jscomp_error=invalidCasts",
        "--jscomp_error=missingProperties",
        "--jscomp_error=nonStandardJsDocs",
        "--jscomp_error=strictModuleDepCheck",
        "--jscomp_error=typeInvalidation",
        "--jscomp_error=undefinedVars",
        "--jscomp_error=unknownDefines",
        "--jscomp_error=uselessCode",
        "--jscomp_error=visibility",
        "--language_in=ECMASCRIPT6_STRICT",
        "--language_out=ECMASCRIPT5_STRICT",
        "--third_party=false",
        "--output_wrapper=\\\"(function(){%output%;init();}).call(this);\\\"",
    ],
    visibility = ["//src/java/com/github/jsdossier:__pkg__"],
)
