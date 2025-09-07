rootProject.name = "tia-etl-system"

// Core modules
include("core:parser-api")

// Parser implementations
include("parsers:zte-asn1-parser")

// Service modules
include("services:file-scanner")

// Service modules to be added as they are created
// include("services:parser-orchestrator")
// include("services:job-manager")
// include("services:monitoring")