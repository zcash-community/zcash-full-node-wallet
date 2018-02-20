#!bin/sh
# ------------------------------------------------------------------
# [Niels Buekers]	Clean blockchain folders
#          			Deletes existing blockchain from user's default
#					installation directory. Should only be used in
#					case of a corrupted chainstate. User will need
#					to redownload entire blockchain if he proceeds.
#					Wallet, peers and config remains untouched.
# ------------------------------------------------------------------

echo "This operation will remove your current blockchain."
echo "You will need to redownload it completely."
echo "Only use this in case of a currupted chainstate."

read -p "Are you sure you want to delete your entire ZCL blockchain? (y/N) " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
	echo "deleting ~/Library/Application Support/Zclassic/blocks"
    rm -rf ~/Library/Application Support/Zclassic/blocks
    echo "deleting ~/Library/Application Support/Zclassic/chainstate"
    rm -rf ~/Library/Application Support/Zclassic/chainstate
    echo "success."
else
	echo "Aborted."
fi