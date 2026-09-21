$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$classes = Join-Path $root "build\classes"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(
    Get-ChildItem -LiteralPath (Join-Path $root "src\main\java") -Recurse -Filter *.java
    Get-ChildItem -LiteralPath (Join-Path $root "src\test\java") -Recurse -Filter *.java
) | ForEach-Object FullName
& javac -encoding UTF-8 --release 17 -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw "test compilation failed" }
& java -ea -cp $classes com.zyy.arithmetic.TestRunner
if ($LASTEXITCODE -ne 0) { throw "tests failed" }
