$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$classes = Join-Path $root "build\classes"
if (-not (Test-Path -LiteralPath $classes)) {
    & (Join-Path $PSScriptRoot "build.ps1")
}
$output = if ($args.Count -ge 3) { $args[2] } else { Join-Path $root "build\performance\profile-methods.csv" }
& java -Xms256m -Xmx1g -cp $classes com.zyy.arithmetic.ProfilingRunner $args[0] $args[1] $output
if ($LASTEXITCODE -ne 0) { throw "profiling failed" }
