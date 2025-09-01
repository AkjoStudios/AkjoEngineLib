param(
  [Parameter(Mandatory = $true, Position = 0)]
  [string]$Name
)

$ErrorActionPreference = 'Stop'
$OutputEncoding = [Console]::InputEncoding = [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding

& ./mvnw.cmd clean package -Papp
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

$JAR = Join-Path -Path 'target' -ChildPath "$Name-exec.jar"

if (-not (Test-Path -LiteralPath $JAR)) {
  exit 1
}

& java -D"spring.profiles.active=app" -jar $JAR
exit $LASTEXITCODE