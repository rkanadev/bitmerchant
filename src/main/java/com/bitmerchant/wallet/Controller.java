package com.bitmerchant.wallet;


import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DownloadListener;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
public class Controller {

	static final Logger log = LoggerFactory.getLogger(Controller.class);
	
	public String balance;
	public String address;
	public String statusText; 
	public Double statusProgress;
	private ProgressBarUpdater syncProgressUpdater = new ProgressBarUpdater();
	private Address addressA = null;
	private Coin balanceC = Coin.ZERO;




	public void onBitcoinSetup() {
		setWallet(bitcoin.wallet());
		address = getAddressA().toString();
		balance = MonetaryFormat.BTC.noCode().format(getBalanceC()).toString();


		TorClient torClient = bitcoin.peerGroup().getTorClient();
		if (torClient != null) {
			
			String torMsg = "Initialising Tor";

			torClient.addInitializationListener(new TorInitializationListener() {
				@Override
				public void initializationProgress(String message, int percent) {

					statusText = torMsg + ": " + message;
					statusProgress = percent / 100.0;

				}

				@Override
				public void initializationCompleted() {

					statusProgress = -1d;
					statusText = "Synchronising with the Bitcoin network";

				}
			});
		} else {
			statusText = "Synchronising with the Bitcoin network";
		}
		
		
	
	
	}
	
	private class ProgressBarUpdater extends DownloadListener {
		@Override
		protected void progress(double pct, int blocksLeft, Date date) {
			super.progress(pct, blocksLeft, date);
			
			statusProgress = pct / 100.0;
			log.info("Progress = " + statusProgress);
			System.out.println("DERP = " + statusProgress);
		}

		@Override
		protected void doneDownload() {
			super.doneDownload();
			System.out.println("DERP DONE");
			statusProgress = 1.0;
			statusText = "";
			
		}
	}

	public DownloadListener getDownloadListener() { return syncProgressUpdater; }
	
    public DownloadListener progressBarUpdater() {
        return getDownloadListener();
    }
    
	public String getBalance() {
		return balance;
	}

	public String getAddress() {
		return address;
	}

	public String getStatusText() {
		return statusText;
	}
	public ProgressBarUpdater getSyncProgressUpdater() {
		return syncProgressUpdater;
	}
	

	public void setWallet(Wallet wallet) {
		wallet.addEventListener(new AbstractWalletEventListener() {
			@Override
			public void onWalletChanged(Wallet wallet) {
				super.onWalletChanged(wallet);
				update(wallet);
			}
		});
		update(wallet);
	}

	private void update(Wallet wallet) {
		balanceC = wallet.getBalance();
		addressA = wallet.currentReceiveAddress();
	}

	

	public Address getAddressA() {
		return addressA;
	}

	public Coin getBalanceC() {
		return balanceC;
	}

	
	public Double getStatusProgress() {
		return statusProgress;
	}

}