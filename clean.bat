:: some commands found that can be a start for the clean.bat

:: ------------------------------------------------------------------
:: [Niels Buekers] 	Clean blockchain folders
::          		Deletes existing blockchain from user's default
::					installation directory. Should only be used in
::					case of a corrupted chainstate. User will need
::					to redownload entire blockchain if he proceeds.
::					Wallet, peers and config remains untouched.
:: ------------------------------------------------------------------
@echo off
setlocal
:PROMPT
ECHO "This operation will remove your current blockchain."
ECHO "You will need to redownload it completely."
ECHO "Only use this in case of a currupted chainstate."
SET /P AREYOUSURE=Are you sure (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

 @RD /S /Q "C:\Users\%USERNAME%\AppData\Roaming\Zclassic\blocks"
 @RD /S /Q "C:\Users\%USERNAME%\AppData\Roaming\Zclassic\chainstate"

:END
endlocal