$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$build = Join-Path $root "build"
$classes = Join-Path $build "classes"
$dist = Join-Path $root "dist"
Remove-Item -LiteralPath $build -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classes, $dist | Out-Null
$sources = Get-ChildItem -LiteralPath (Join-Path $root "src\main\java") -Recurse -Filter *.java | ForEach-Object FullName
& javac -encoding UTF-8 --release 17 -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed" }
& jar --create --file (Join-Path $dist "Myapp.jar") --main-class com.zyy.arithmetic.Main -C $classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed" }
Write-Host "Built: $(Join-Path $dist 'Myapp.jar')"
