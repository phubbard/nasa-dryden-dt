
@echo off

REM
REM This script prepares an archive for recovery using the following steps:
REM
REM 1. Remove *.rbn files from the Source level (top-level *.rbn files)
REM 2. Remove *.rbn files from each Ring Buffer level (RB*\*.rbn files)
REM 3. Remove fsreghdr.rbn and seal.rbn files from each FileSet (RB*\FS*\fsreghdr.rbn and RB*\FS*\seal.rbn)
REM
REM NOTE: On a UNIX machine, this can be accomplished by doing the following:
REM     In the Source directory, execute the following:
REM     rm *.rbn
REM     rm RB*/*.rbn
REM     rm RB*/FS*/seal.rbn
REM     rm RB*/FS*/fsreghdr.rbn
REM

if (%1)==() goto PRINT_USAGE

echo.
echo. This script will delete the following files:
echo. %1\*.rbn
echo. %1\RB*\*.rbn
echo. %1\RB*\FS*\fsreghdr.rbn
echo. %1\RB*\FS*\seal.rbn
echo.
echo. If you would like to continue, hit any key.  Otherwise, hit Ctrl-C to quit this script.

pause

pushd %1

for %%f in ("*.rbn") do (
    del %%f
    echo. Deleted %1\%%f
)

for /D %%r in ("RB*") do (
    pushd %%r
    for %%f in ("*.rbn") do (
        del %%f
        echo. Deleted %1\%%r\%%f
    )
    for /D %%s in ("FS*") do (
        pushd %%s
        if not exist fsreghdr.rbn goto DEL_SEAL
        del fsreghdr.rbn
        echo. Deleted %1\%%s\fsreghdr.rbn
:DEL_SEAL
        if not exist seal.rbn goto END_FS
        del seal.rbn
        echo. Deleted %1\%%s\seal.rbn
:END_FS
        popd
    )
    popd
)

popd

goto END

:PRINT_USAGE
echo.
echo. SYNTAX:
echo. "clean_archive <Source dir>"
echo.
goto END

:END
exit /B
