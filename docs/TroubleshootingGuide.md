## Zcash Desktop GUI Wallet troubleshooting guide 

This document outlines possible solutions to several common problems that user might encounter while using the wallet.

### How to diagnose wallet problems

When wallet problems occur, the information about the errors that caused the issue is usually found in the log files written by the wallet itself and also by `zcashd`. The location of the wallet log files is:
```
Linux:    ~/.ZcashSwingWallet/ZcashGUIWallet_xxxx_xx_debug.log 
Windows:  %LOCALAPPDATA%/ZcashSwingWallet/ZcashSwingWallet_xxxx_xx_debug.log
Mac OS:   ~/Library/Application Support/ZcashSwingWallet/ZcashSwingWallet_xxxx_xx_debug.log
```
`zcashd` that is automatically started by the wallet, stores its logs at locations:
```
Linux:    ~/.zcash/debug.log, 
Windows:  %APPDATA%/Zcash/debug.log
Mac OS:   ~/Library/Application Support/Zcash/debug.log
 ```
The log files are the first place too look for clues as to the nature of problem.

### Common Problem 1 - wallet fails during start up

This kind of problem may have multiple causes but the most frequent one in practice is that `zcashd` fails to start properly. The latter is in turn most commonly caused by block-chain corruption. This could occur 
in rare cases when for instance machines are stopped due to power disruptions, while `zcashd` is writing data.

The reason is usually that `zcashd` has not started properly and the GUI wallet cannot connect to it. As a start
one may examine the `zcashd` logs to find the technical details of the problem. One way to fix this problem, that 
works in 90%+ of cases is to start `zcashd` manually with a `-reindex` option from a terminal and start the GUI wallet only after that. The command is:
```
zcashd -reindex
```
For non-technical users here is some information on how to open a terminal on [Windows](https://www.lifewire.com/how-to-open-command-prompt-2618089) and [Mac OS](https://www.wikihow.com/Open-a-Terminal-Window-in-Mac). The full command on Mac OS is:
```
/Applications/ZcashWallet.app/Contents/Java/zcashd -reindex
```
On Windows you first have to locate `zcashd.exe`. It is in the `/app` sub-directory of the wallet installation.

### Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
