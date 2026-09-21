$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot "build.ps1")
$classes = Join-Path $root "build\classes"
$sources = Get-ChildItem -LiteralPath (Join-Path $root "src\test\java") -Recurse -Filter *.java | ForEach-Object FullName
& javac -encoding UTF-8 --release 17 -cp $classes -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw "test compilation failed" }
& java -ea -cp $classes com.zyy.arithmetic.TestRunner
if ($LASTEXITCODE -ne 0) { throw "tests failed" }
