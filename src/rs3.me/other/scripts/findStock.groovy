#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {

    // Define Pipeline Variables
    def itemCheck = "https://www.costco.com/callaway-edge-10-piece-golf-club-set,-right-handed---graphite.product.100683849.html"

    // Define Environment Variables
    common_variables.itemChkVariables(itemCheck)

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Start Stock Stage
    stage ('Check Stock') {
        def stockResults = sh(returnStdout: true, script: """curl -Is -A "Datadog/Synthetics" "${itemCheck}" | head -1 | cut -c 8-""").trim()
        if (stockResults == "404") {
            echo "Item is out of stock."
        }
        else if (stockResults == "200") {
            echo "Item is in stock"
            common_helpers.notifySlackSuccess()
        }
        else {
           echo "Unknown result code. Result code is ${stockResults}. Will check again soon"
           common_helpers.notifySlackUnstable()
        }
    }
}