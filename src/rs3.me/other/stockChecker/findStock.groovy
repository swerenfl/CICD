#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */

// TODO
// Can I check multiple items at once? Would require a FOR loop / array
// Do I want to continue to use "common_helpers". Could I pass a value into 
// common_stages.notifySlack to denote if success or unstable and have logic
// to call the neccesary helper. Just a thought.

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
        echo "The return code for this item is ${stockResults}."
        
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