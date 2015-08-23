load("/build_tools/java_test_suite", "java_test_suite")

java_test_suite(
    name = "AllTests",
    srcs = glob(["*.java"]),
    resources = glob(["resources/**"]),
    deps = [
        "//lib/maven:closure_compiler",
        "//lib/maven:gson",
        "//lib/maven:guava",
        "//lib/maven:guice",
        "//lib/maven:inject",
        "//lib/maven:jimfs",
        "//lib/maven:jsoup",
        "//lib/maven:junit",
        "//lib/maven:mockito",
        "//lib/maven:truth",
        "//src/java/com/github/jsdossier",
        "//src/java/com/github/jsdossier/annotations",
        "//src/java/com/github/jsdossier/jscomp",
        "//src/java/com/github/jsdossier/proto",
        "//test/java/com/github/jsdossier/testing",
    ],
)