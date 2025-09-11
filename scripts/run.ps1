param(
  [Parameter(Mandatory = $true, Position = 0)]
  [string]$Name,
  [Parameter(Mandatory = $true, Position = 1)]
  [string]$Platform
)

$ErrorActionPreference = 'Stop'
$OutputEncoding = [Console]::InputEncoding = [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding

$platformProfile = "platform-$Platform"

& ./mvnw.cmd 'clean' 'package' "-P=app-build,$platformProfile"
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

$JAR = Join-Path -Path 'target' -ChildPath "$Name-exec.jar"

if (-not (Test-Path -LiteralPath $JAR)) {
  exit 1
}

& java "-Dspring.profiles.active=app-build,$platformProfile" '-XX:+UseShenandoahGC' '-jar' "$JAR"
exit $LASTEXITCODE