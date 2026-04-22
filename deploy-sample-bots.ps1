param(
    [string]$TargetDir = "C:\Code\bots"
)

# Convert Windows backslash path to forward slashes for bash
$BashTarget = $TargetDir -replace '\\', '/'

bash ./deploy-sample-bots.sh $BashTarget
exit $LASTEXITCODE
