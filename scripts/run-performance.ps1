$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot "build.ps1")
$classes = Join-Path $root "build\classes"
& java -Xms256m -Xmx1g -cp $classes com.zyy.arithmetic.PerformanceRunner @args
