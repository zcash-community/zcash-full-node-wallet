/************************************************************************************************
<<<<<<< HEAD
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;
import com.vaklinov.zcashui.arizen.models.Address;
import com.vaklinov.zcashui.arizen.repo.ArizenWallet;
import com.vaklinov.zcashui.arizen.repo.WalletRepo;


/**
 * Provides miscellaneous operations for the wallet file.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class WalletOperations
{
	private ZCashUI parent;
	private JTabbedPane tabs;
	private DashboardPanel dashboard;
	private SendCashPanel  sendCash;
	private AddressesPanel addresses;

	private ZCashInstallationObserver installationObserver;
	private ZCashClientCaller         clientCaller;
	private StatusUpdateErrorReporter errorReporter;
	private BackupTracker             backupTracker;


	public WalletOperations(ZCashUI parent,
			JTabbedPane tabs,
			DashboardPanel dashboard,
			AddressesPanel addresses,
			SendCashPanel  sendCash,

			ZCashInstallationObserver installationObserver, 
			ZCashClientCaller clientCaller,
			StatusUpdateErrorReporter errorReporter,
			BackupTracker             backupTracker) 
					throws IOException, InterruptedException, WalletCallException 
	{
		this.parent    = parent;
		this.tabs      = tabs;
		this.dashboard = dashboard;
		this.addresses = addresses;
		this.sendCash  = sendCash;

		this.installationObserver = installationObserver;
		this.clientCaller = clientCaller;
		this.errorReporter = errorReporter;

		this.backupTracker = backupTracker;
	}


	public void encryptWallet()
	{
		try
		{
			if (this.clientCaller.isWalletEncrypted())
			{
				JOptionPane.showMessageDialog(
						this.parent,
						"The wallet.dat file being used is already encrypted. " +
								"This \noperation may be performed only on a wallet that " +
								"is not\nyet encrypted!",
								"Wallet Is Already Encrypted",
								JOptionPane.ERROR_MESSAGE);
				return;

			}

			PasswordEncryptionDialog pd = new PasswordEncryptionDialog(this.parent);
			pd.setVisible(true);

			if (!pd.isOKPressed())
			{
				return;
			}

			Cursor oldCursor = this.parent.getCursor();
			try
			{

				this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				this.dashboard.stopThreadsAndTimers();
				this.sendCash.stopThreadsAndTimers();

				this.clientCaller.encryptWallet(pd.getPassword());

				this.parent.setCursor(oldCursor);
			} catch (WalletCallException wce)
			{
				this.parent.setCursor(oldCursor);
				Log.error("Unexpected error: ", wce);

				JOptionPane.showMessageDialog(
						this.parent,
						"An unexpected error occurred while encrypting the wallet!\n" +
								"It is recommended to stop and restart both zcld and the GUI wallet! \n" +
								"\n" + wce.getMessage().replace(",", ",\n"),
								"Error Encrypting Wallet", JOptionPane.ERROR_MESSAGE);
				return;
			}

			JOptionPane.showMessageDialog(
					this.parent,
					"The wallet has been encrypted sucessfully and zcld has stopped.\n" +
							"The GUI wallet will be stopped as well. Please restart the program.\n" +
							"Additionally, the internal wallet keypool has been flushed. You need\n" +
							"to make a new backup." +
							"\n",
							"Wallet Is Now Encrypted", JOptionPane.INFORMATION_MESSAGE);

			this.parent.exitProgram();

		} catch (Exception e)
		{
			this.errorReporter.reportError(e, false);
		}
	}


	public void backupWallet()
	{
		try
		{
			Cursor oldCursor = this.parent.getCursor();

			String path = null;
			try
			{
				this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				path = this.clientCaller.backupWallet("");
				this.backupTracker.handleBackup();
				this.parent.setCursor(oldCursor);

			} catch (WalletCallException wce)
			{
				this.parent.setCursor(oldCursor);

				Log.error("Unexpected error: ", wce);

				JOptionPane.showMessageDialog(

						this.parent,
						"An unexpected error occurred while backing up the wallet!" +
								"\n" + wce.getMessage().replace(",", ",\n"),
								"Error Backing Up Wallet", JOptionPane.ERROR_MESSAGE);
				return;
			}

			JOptionPane.showMessageDialog(
					this.parent, 
					"The wallet has been backed up successfully.\nFull path is: " + 
							path,
							"Successfully Backed Up Wallet", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e)
		{
			this.errorReporter.reportError(e, false);
		}
	}


	public void exportWalletPrivateKeys()
	{
		// TODO: Will need corrections once encryption is reenabled!!!

		try
		{
			Cursor oldCursor = this.parent.getCursor();
			String path=null;
			try
			{
				this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				path = this.clientCaller.exportWallet("zclprivatekeys");
				this.backupTracker.handleBackup();
				this.parent.setCursor(oldCursor);

			} catch (WalletCallException wce)
			{
				this.parent.setCursor(oldCursor);

				Log.error("Unexpected error: ", wce);

				JOptionPane.showMessageDialog(
						this.parent, 
						"An unexpected error occurred while exporting wallet private keys!" +
								"\n" + wce.getMessage().replace(",", ",\n"),
								"Error Exporting Private Keys", JOptionPane.ERROR_MESSAGE);
				return;
			}

			JOptionPane.showMessageDialog(
					this.parent, 
					"The wallet private keys have been exported successfully. Full path is: " + 
							path + "\n" +
							"You need to protect this file from unauthorized access. Anyone who\n" +
							"has access to the private keys can spend the Zclassic balance!",
							"Successfully Exported Private Keys", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e)
		{
			this.errorReporter.reportError(e, false);
		}
	}


	public void importWalletPrivateKeys()
	{
		// TODO: Will need corrections once encryption is re-enabled!!!

		int option = JOptionPane.showConfirmDialog(
				this.parent,
				"Importing private keys can be a slow operation. It may take\n" +
						"several minutes, during which the GUI will be non-responsive.\n" +
						"The data to import must be in the format used by \n" +
						"\"Wallet >> Export Private Keys\"\n\n" +
						"Continue?",
						"Private key import",
						JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.NO_OPTION)
		{
			return;
		}

		try
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Import Private Keys from File");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int result = fileChooser.showOpenDialog(this.parent);

			if (result != JFileChooser.APPROVE_OPTION)
			{
				return;
			}

			File f = fileChooser.getSelectedFile();

			Cursor oldCursor = this.parent.getCursor();
			try
			{
				this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				this.clientCaller.importWallet(f.getCanonicalPath());

				this.parent.setCursor(oldCursor);
			} catch (WalletCallException wce)
			{
				this.parent.setCursor(oldCursor);
				Log.error("Unexpected error: ", wce);

				JOptionPane.showMessageDialog(
						this.parent,
						"An unexpected error occurred while importing private keys!" +
								"\n" + wce.getMessage().replace(",", ",\n"),
								"Error Importing Private Keys", JOptionPane.ERROR_MESSAGE);

				return;
			}

			JOptionPane.showMessageDialog(
					this.parent,
					"Wallet private keys have been successfully imported from location:\n" +
							f.getCanonicalPath() + "\n\n",
							"Successfully Imported Private Keys", JOptionPane.INFORMATION_MESSAGE);


		} catch (Exception e)
		{
			this.errorReporter.reportError(e, false);
		}
	}


	public void showPrivateKey()
	{
		if (this.tabs.getSelectedIndex() != 1)
		{
			JOptionPane.showMessageDialog(
					this.parent,
					"Please select an address in the \"My Addresses\" tab " +
							"to view its private key.",
							"Select an Address", JOptionPane.INFORMATION_MESSAGE);
			this.tabs.setSelectedIndex(1);
			return;
		}

		String address = this.addresses.getSelectedAddress();

		if (address == null)
		{
			JOptionPane.showMessageDialog(
					this.parent,
					"Please select an address from the table " +
							"to view its private key.",
							"Select an Address", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try
		{
			// Check for encrypted wallet
			final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
			if (bEncryptedWallet)
			{
				PasswordDialog pd = new PasswordDialog((JFrame)(this.parent));
				pd.setVisible(true);

				if (!pd.isOKPressed())
				{
					return;
				}

				this.clientCaller.unlockWallet(pd.getPassword());
			}

			boolean isZAddress = Util.isZAddress(address);

			String privateKey = isZAddress ?
					this.clientCaller.getZPrivateKey(address) : this.clientCaller.getTPrivateKey(address);

					// Lock the wallet again
					if (bEncryptedWallet)
					{
						this.clientCaller.lockWallet();
					}

					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(new StringSelection(privateKey), null);

					JOptionPane.showMessageDialog(
							this.parent,
							(isZAddress ? "Z (Private)" : "T (Transparent)") +  " address:\n" +
									address + "\n" +
									"has private key:\n" +
									privateKey + "\n\n" +
									"The private key has also been copied to the clipboard.",
									"Private Key Info", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception ex)
		{
			this.errorReporter.reportError(ex, false);
		}
	}


	public void importSinglePrivateKey()
	{
		try
		{
			SingleKeyImportDialog kd = new SingleKeyImportDialog(this.parent, this.clientCaller);
			kd.setVisible(true);

		} catch (Exception ex)
		{
			this.errorReporter.reportError(ex, false);
		}
	}
}
