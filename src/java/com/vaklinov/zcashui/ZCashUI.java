/************************************************************************************************
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


import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vaklinov.zcashui.OSUtil.OS_TYPE;
import com.vaklinov.zcashui.ZCashClientCaller.NetworkAndBlockchainInfo;
import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;
import com.vaklinov.zcashui.ZCashInstallationObserver.DAEMON_STATUS;
import com.vaklinov.zcashui.ZCashInstallationObserver.DaemonInfo;
import com.vaklinov.zcashui.ZCashInstallationObserver.InstallationDetectionException;
import com.vaklinov.zcashui.msg.MessagingPanel;


/**
 * Main Zclassic Window.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class ZCashUI
extends JFrame
{
	private ZCashInstallationObserver installationObserver;
	private ZCashClientCaller         clientCaller;
	private StatusUpdateErrorReporter errorReporter;

	private WalletOperations walletOps;

	private JMenuItem menuItemExit;
	private JMenuItem menuItemAbout;
	//private JMenuItem menuItemEncrypt;
	//private JMenuItem menuItemBackup;
	//private JMenuItem menuItemExportKeys;
	//private JMenuItem menuItemImportKeys;
	private JMenuItem menuItemShowPrivateKey;
	private JMenuItem menuItemImportOnePrivateKey;
	private JMenuItem menuItemOwnIdentity;
	private JMenuItem menuItemExportOwnIdentity;
	private JMenuItem menuItemImportContactIdentity;
	private JMenuItem menuItemAddMessagingGroup;
	private JMenuItem menuItemRemoveContactIdentity;
	private JMenuItem menuItemMessagingOptions;
	private JMenuItem menuItemShareFileViaIPFS;
	//private JMenuItem menuItemExportToArizen;

	private DashboardPanel   dashboard;
	private AddressesPanel   addresses;
	private SendCashPanel    sendPanel;
	private AddressBookPanel addressBookPanel;
	private MessagingPanel   messagingPanel;

	JTabbedPane tabs;

	public ZCashUI(StartupProgressDialog progressDialog)
			throws IOException, InterruptedException, WalletCallException
	{
		super("Zclassic Desktop GUI Wallet 1.0.0");

		if (progressDialog != null)
		{
			progressDialog.setProgressText("Starting wallet GUI...");
		}

		ClassLoader cl = this.getClass().getClassLoader();

		this.setIconImage(new ImageIcon(cl.getResource("images/zclassic-logo.png")).getImage());
		System.out.println(cl.getResource("config/addnodes.txt").getPath());

		Container contentPane = this.getContentPane();

		errorReporter = new StatusUpdateErrorReporter(this);
		installationObserver = new ZCashInstallationObserver(OSUtil.getProgramDirectory());
		clientCaller = new ZCashClientCaller(OSUtil.getProgramDirectory());

		if (installationObserver.isOnTestNet())
		{
			this.setTitle(this.getTitle() + " [TESTNET]");
		}

		// Build content
		tabs = new JTabbedPane();
		Font oldTabFont = tabs.getFont();
		Font newTabFont  = new Font(oldTabFont.getName(), Font.BOLD | Font.ITALIC, oldTabFont.getSize() * 57 / 50);
		tabs.setFont(newTabFont);
		BackupTracker backupTracker = new BackupTracker(this);

		tabs.addTab("Transactions ",
				new ImageIcon(cl.getResource("images/overview.png")),
				dashboard = new DashboardPanel(this, installationObserver, clientCaller,
						errorReporter, backupTracker));
		tabs.addTab("My Addresses ",
				new ImageIcon(cl.getResource("images/own-addresses.png")),
				addresses = new AddressesPanel(this, clientCaller, errorReporter));
		tabs.addTab("Send ZCL ",
				new ImageIcon(cl.getResource("images/send.png")),
				sendPanel = new SendCashPanel(clientCaller, errorReporter, installationObserver, backupTracker));
		tabs.addTab("Address Book ",
				new ImageIcon(cl.getResource("images/address-book.png")),
				addressBookPanel = new AddressBookPanel(sendPanel, tabs));
		tabs.addTab("Messaging ",
				new ImageIcon(cl.getResource("images/messaging.png")),
				messagingPanel = new MessagingPanel(this, sendPanel, tabs, clientCaller, errorReporter));
		contentPane.add(tabs);

		this.walletOps = new WalletOperations(
				this, tabs, dashboard, addresses, sendPanel,
				installationObserver, clientCaller, errorReporter, backupTracker);

		int width = 870;

		OS_TYPE os = OSUtil.getOSType();

		// Window needs to be larger on Mac/Windows - typically
		if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS))
		{
			width += 100;
		}

		this.setSize(new Dimension(width, 440));

		// Build menu
		JMenuBar mb = new JMenuBar();
		JMenu file = new JMenu("Main");
		file.setMnemonic(KeyEvent.VK_M);
		int accelaratorKeyMask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask();
		file.add(menuItemAbout = new JMenuItem("About", KeyEvent.VK_T));
		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, accelaratorKeyMask));
		file.addSeparator();
		file.add(menuItemExit = new JMenuItem("Quit", KeyEvent.VK_Q));
		menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelaratorKeyMask));
		mb.add(file);

		JMenu wallet = new JMenu("Wallet");
		wallet.setMnemonic(KeyEvent.VK_W);

		/*
		 * Disabled since there is no way of importing the wallet so does not make sense for Avg. Joe to export
		 */
		/*
		 * wallet.add(menuItemBackup = new JMenuItem("Backup", KeyEvent.VK_B));
		 * menuItemBackup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, accelaratorKeyMask));
		 */

		//comment out since was disabled in the first place
		//wallet.add(menuItemEncrypt = new JMenuItem("Encrypt", KeyEvent.VK_E));
		//menuItemEncrypt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelaratorKeyMask));
		/*
		 * disable since importing in BULK gives these issues:
		 * https://github.com/zcash/zcash/issues/2486
		 * https://github.com/zcash/zcash/issues/2524
		 */ 

		/*
		wallet.add(menuItemExportKeys = new JMenuItem("Export Private Keys", KeyEvent.VK_K));
		menuItemExportKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, accelaratorKeyMask));
		wallet.add(menuItemImportKeys = new JMenuItem("Import Private Keys", KeyEvent.VK_I));
		menuItemImportKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, accelaratorKeyMask));
		 */
		wallet.add(menuItemShowPrivateKey = new JMenuItem("View One Private Key", KeyEvent.VK_P));
		menuItemShowPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelaratorKeyMask));
		wallet.add(menuItemImportOnePrivateKey = new JMenuItem("Import One Private Key", KeyEvent.VK_N));
		menuItemImportOnePrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelaratorKeyMask));

		/*
		 * wallet.add(menuItemExportToArizen = new JMenuItem("Export to Arizen wallet...", KeyEvent.VK_A));
		 * menuItemExportToArizen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelaratorKeyMask));
		 */
		mb.add(wallet);

		JMenu messaging = new JMenu("Messaging");
		messaging.setMnemonic(KeyEvent.VK_S);
		messaging.add(menuItemOwnIdentity = new JMenuItem("My Identity", KeyEvent.VK_D));
		menuItemOwnIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, accelaratorKeyMask));
		messaging.add(menuItemExportOwnIdentity = new JMenuItem("Export My Identity", KeyEvent.VK_X));
		menuItemExportOwnIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelaratorKeyMask));

		messaging.add(menuItemImportContactIdentity = new JMenuItem("Import Contact", KeyEvent.VK_Y));
		menuItemImportContactIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, accelaratorKeyMask));
		messaging.add(menuItemRemoveContactIdentity = new JMenuItem("Remove Contact", KeyEvent.VK_R));
		menuItemRemoveContactIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelaratorKeyMask));

		messaging.add(menuItemAddMessagingGroup = new JMenuItem("Create Group", KeyEvent.VK_G));
		menuItemAddMessagingGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, accelaratorKeyMask));

		messaging.add(menuItemMessagingOptions = new JMenuItem("Options", KeyEvent.VK_O));
		menuItemMessagingOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));

		JMenu shareFileVia = new JMenu("Share file via:");
		shareFileVia.setMnemonic(KeyEvent.VK_V);
		// TODO: uncomment this for IPFS integration
		//messaging.add(shareFileVia);
		shareFileVia.add(menuItemShareFileViaIPFS = new JMenuItem("IPFS", KeyEvent.VK_F));
		menuItemShareFileViaIPFS.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, accelaratorKeyMask));

		mb.add(messaging);

		// TODO: Temporarily disable encryption until further notice - Oct 24 2016
		//menuItemEncrypt.setEnabled(false);

		this.setJMenuBar(mb);

		// Add listeners etc.
		menuItemExit.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.exitProgram();
					}
				}
				);

		menuItemAbout.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							AboutDialog ad = new AboutDialog(ZCashUI.this);
							ad.setVisible(true);
						} catch (UnsupportedEncodingException uee)
						{
							Log.error("Unexpected error: ", uee);
							ZCashUI.this.errorReporter.reportError(uee);
						}
					}
				}
				);
		/*
		menuItemBackup.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.backupWallet();
					}
				}
				);
		 */
		/*
		menuItemEncrypt.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.encryptWallet();
					}
				}
				);
		 */
		/*
		menuItemExportKeys.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.exportWalletPrivateKeys();
					}
				}
				);

		menuItemImportKeys.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.importWalletPrivateKeys();
					}
				}
				);
		 */
		menuItemShowPrivateKey.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.showPrivateKey();
					}
				}
				);

		menuItemImportOnePrivateKey.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.importSinglePrivateKey();
					}
				}
				);

		menuItemOwnIdentity.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.openOwnIdentityDialog();
					}
				}
				);

		menuItemExportOwnIdentity.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.exportOwnIdentity();
					}
				}
				);

		menuItemImportContactIdentity.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.importContactIdentity();
					}
				}
				);

		menuItemAddMessagingGroup.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.addMessagingGroup();
					}
				}
				);

		menuItemRemoveContactIdentity.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.removeSelectedContact();
					}
				}
				);

		menuItemMessagingOptions.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.openOptionsDialog();
					}
				}
				);

		menuItemShareFileViaIPFS.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.messagingPanel.shareFileViaIPFS();
					}
				}
				);

		/*
		 * menuItemExportToArizen.addActionListener(

				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ZCashUI.this.walletOps.exportToArizenWallet();
					}
				}
				);
		 */

		// Close operation
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				ZCashUI.this.exitProgram();
			}
		});

		// Show initial message
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					String userDir = OSUtil.getSettingsDirectory();
					File warningFlagFile = new File(userDir + File.separator + "initialInfoShown_0.75.flag");
					if (warningFlagFile.exists())
					{
						return;
					} else
					{
						warningFlagFile.createNewFile();
					}

				} catch (IOException ioe)
				{
					/* TODO: report exceptions to the user */
					Log.error("Unexpected error: ", ioe);
				}

				JOptionPane.showMessageDialog(
						ZCashUI.this.getRootPane().getParent(),
						"The Zclassic Full-Node Desktop Wallet is currently considered experimental. Use of this software\n" +
								"comes at your own risk! Be sure to read the list of known issues and limitations\n" +
								"at this page: https://github.com/z-classic/zclassic-full-node-wallet\n\n" +
								"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
								"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
								"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
								"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
								"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
								"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n" +
								"THE SOFTWARE.\n\n" +
								"(This message will only be shown once per release)",
								"Disclaimer", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Finally dispose of the progress dialog
		if (progressDialog != null)
		{
			progressDialog.doDispose();
		}

		// Notify the messaging TAB that it is being selected - every time
		tabs.addChangeListener(
				new ChangeListener()
				{
					@Override
					public void stateChanged(ChangeEvent e)
					{
						JTabbedPane tabs = (JTabbedPane)e.getSource();
						if (tabs.getSelectedIndex() == 4)
						{
							ZCashUI.this.messagingPanel.tabSelected();
						}
					}
				}
				);

	}

	public void exitProgram()
	{
		Log.info("Exiting...");

		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		this.dashboard.stopThreadsAndTimers();
		this.addresses.stopThreadsAndTimers();
		this.sendPanel.stopThreadsAndTimers();
		this.messagingPanel.stopThreadsAndTimers();

		ZCashUI.this.setVisible(false);
		ZCashUI.this.dispose();

		System.exit(0);
	}

	public static void main(String argv[])
			throws IOException
	{
		try
		{
			OS_TYPE os = OSUtil.getOSType();

			if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS))
			{
				possiblyCreateZENConfigFile();
			}

			Log.info("Zclassic Full-Node Desktop Wallet (GUI, made in Java & Swing)");
			Log.info("OS: " + System.getProperty("os.name") + " = " + os);
			Log.info("Current directory: " + new File(".").getCanonicalPath());
			Log.info("Class path: " + System.getProperty("java.class.path"));
			Log.info("Environment PATH: " + System.getenv("PATH"));

			// Look and feel settings - a custom OS-look and feel is set for Windows
			if (os == OS_TYPE.WINDOWS)
			{
				// Custom Windows L&F and font settings
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

				// This font looks good but on Windows 7 it misses some chars like the stars...
				//FontUIResource font = new FontUIResource("Lucida Sans Unicode", Font.PLAIN, 11);
				//UIManager.put("Table.font", font);
			} else if (os == OS_TYPE.MAC_OS)
			{
				// The MacOS L&F is active by default - the property sets the menu bar Mac style
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
			else
			{
				for (LookAndFeelInfo ui : UIManager.getInstalledLookAndFeels())
				{
					Log.info("Available look and feel: " + ui.getName() + " " + ui.getClassName());
					if (ui.getName().equals("Nimbus"))
					{
						Log.info("Setting look and feel: {0}", ui.getClassName());
						UIManager.setLookAndFeel(ui.getClassName());
						break;
					};
				}
			}

			// If zcld is currently not running, do a startup of the daemon as a child process
			// It may be started but not ready - then also show dialog
			ZCashInstallationObserver initialInstallationObserver =
					new ZCashInstallationObserver(OSUtil.getProgramDirectory());
			DaemonInfo zcashdInfo = initialInstallationObserver.getDaemonInfo();
			initialInstallationObserver = null;

			ZCashClientCaller initialClientCaller = new ZCashClientCaller(OSUtil.getProgramDirectory());
			boolean daemonStartInProgress = false;
			try
			{
				if (zcashdInfo.status == DAEMON_STATUS.RUNNING)
				{
					NetworkAndBlockchainInfo info = initialClientCaller.getNetworkAndBlockchainInfo();
					// If more than 20 minutes behind in the blockchain - startup in progress
					if ((System.currentTimeMillis() - info.lastBlockDate.getTime()) > (20 * 60 * 1000))
					{
						Log.info("Current blockchain synchronization date is "  +
								new Date(info.lastBlockDate.getTime()));
						daemonStartInProgress = true;
					}
				}
			} catch (WalletCallException wce)
			{
				if ((wce.getMessage().indexOf("{\"code\":-28") != -1) || // Started but not Ready
						(wce.getMessage().indexOf("error code: -28") != -1))
				{
					Log.info("zcld is currently starting...");
					daemonStartInProgress = true;
				}
			}

			StartupProgressDialog startupBar = null;
			if ((zcashdInfo.status != DAEMON_STATUS.RUNNING) || (daemonStartInProgress))
			{
				Log.info(
						"zcld is not running at the moment or has not started/synchronized 100% - showing splash...");
				startupBar = new StartupProgressDialog(initialClientCaller);
				startupBar.setVisible(true);
				startupBar.waitForStartup();
			}
			initialClientCaller = null;

			// Main GUI is created here
			ZCashUI ui = new ZCashUI(startupBar);
			ui.setVisible(true);

		} catch (InstallationDetectionException ide)
		{
			Log.error("Installation Error: ", ide);
			JOptionPane.showMessageDialog(
					null,
					"This program was started in directory: " + OSUtil.getProgramDirectory() + "\n" +
							ide.getMessage() + "\n" +
							"See the console/logfile output for more detailed error information!",
							"Installation Error",
							JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (WalletCallException wce)
		{
			Log.error("WalletCall Error: ", wce);

			if ((wce.getMessage().indexOf("{\"code\":-28,\"message\"") != -1) ||
					(wce.getMessage().indexOf("error code: -28") != -1))
			{
				JOptionPane.showMessageDialog(
						null,
						"It appears that zcld has been started but is not ready to accept wallet\n" +
								"connections. It is still loading the wallet and blockchain. Please try\n" +
								"restarting this program.",
								"Daemon Error",
								JOptionPane.ERROR_MESSAGE);
			} else
			{
				JOptionPane.showMessageDialog(
						null,
						"There was a problem communicating with the Zclassic daemon/wallet. \n" +
								"Please ensure that the Zclassic server zcld is started (e.g. via \n" +
								"command  \"zcld --daemon\"). Error Message: \n" +
								wce.getMessage() +
								"See the console/logfile output for more detailed error information!",
								"Daemon Error",
								JOptionPane.ERROR_MESSAGE);
			}

			System.exit(2);
		} catch (Exception e)
		{
			Log.error("Unexpected error: ", e);
			JOptionPane.showMessageDialog(
					null,
					"An unexpected error (Exception) has occurred: \n" + e.getMessage() + "\n" +
							"See the console/logfile output for more detailed error information!",
							"Unexpected Exception",
							JOptionPane.ERROR_MESSAGE);
			System.exit(3);
		} catch (Error err)
		{
			// Last resort catch for unexpected problems - just to inform the user
			err.printStackTrace();
			JOptionPane.showMessageDialog(
					null,
					"An unexpected error has occurred: \n" + err.getMessage() + "\n" +
							"See the console/logfile output for more detailed error information!",
							"Unexpected Error",
							JOptionPane.ERROR_MESSAGE);
			System.exit(4);
		}
	}


	public static void possiblyCreateZENConfigFile()
			throws IOException
	{
		String blockchainDir = OSUtil.getBlockchainDirectory();
		File dir = new File(blockchainDir);

		if (!dir.exists())
		{
			if (!dir.mkdirs())
			{
				Log.error("ERROR: Could not create settings directory: " + dir.getCanonicalPath());
				throw new IOException("Could not create settings directory: " + dir.getCanonicalPath());
			}
		}

		File zenConfigFile = new File(dir, "zclassic.conf");

		if (zenConfigFile.exists())
		{

			Scanner scanner = new Scanner(zenConfigFile);
			boolean rewrite;

			while (scanner.hasNextLine())
			{

				String line = scanner.nextLine();
			  if (line.indexOf("rpcport=8232") > -1)
				{
					// RPC Port was 8232 for pre-2018 versions - Reset zclassic.conf
					Log.info("DELETING zclassic.conf (" + zenConfigFile.getCanonicalPath() +
							")");

					zenConfigFile.delete();
			  }
			}
		}

		if (!zenConfigFile.exists())
		{

			Log.info("zclassic.conf (" + zenConfigFile.getCanonicalPath() +
					") does not exist. It will be created with default settings.");

			Random r = new Random(System.currentTimeMillis());

			PrintStream configOut = new PrintStream(new FileOutputStream(zenConfigFile));

			configOut.println("#############################################################################");
			configOut.println("#                         ZCL Configuration File                            #");
			configOut.println("#############################################################################");
			configOut.println("# This file has been automatically generated by the ZCL Full-Node Wallet    #");
			configOut.println("# with default settings. It may be further customized.                      #");
			configOut.println("#############################################################################");
			configOut.println("# Creation Date: " + new Date().toString());
			configOut.println("#############################################################################");
			configOut.println("");
			configOut.println("# The rpcuser/rpcpassword are used for the local call to zcld");
			configOut.println("rpcuser=User" + Math.abs(r.nextInt()));
			configOut.println("rpcpassword=Pass" + Math.abs(r.nextInt()) + "" +
					Math.abs(r.nextInt()) + "" +
					Math.abs(r.nextInt()));
			configOut.println("");
			configOut.println("rpcallowip=127.0.0.1");
			configOut.println("rpcport=8023");
			configOut.println("# Well-known nodes to connect to (speeds up acquiring additional peers)");

			for(String node : getAddNodes()) {
				configOut.println(node);
			}

			configOut.close();
		}

	}

	private static List<String> getAddNodes() {
		BufferedReader br = null;
		InputStream is = ZCashUI.class.getResourceAsStream("/config/addnodes.txt");
		br = new BufferedReader(new InputStreamReader(is));

		List <String> nodes = new ArrayList<>();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				nodes.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return nodes;
	}
}
