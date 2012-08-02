package ioio.examples.hello;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ioio.examples.hello.R;
import ioio.lib.api.DigitalInput.Spec;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import ioio.lib.bluetooth.*;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link AbstractIOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends AbstractIOIOActivity {
	private ToggleButton button_;
	
	/*Radio Control Variable Init*/
	private Button leftButton;
	private Button rightButton;
	private Button enterButton;
	
	/*Window Control Variable Init*/
	private Button winDown;
	
	/*Radio Volume Control Variable Init*/
	private Button volUp;
	private Button volDown;
	
	/*For testing purposes because I'm a noob*/
	private EditText test;
	
	/*IOIO Status variable*/
	private TextView status;
	
	/*UART communication to Arduino*/
	int rxPin;
	int txPin;
	int baud;
	Parity parity;
	StopBits stopBits;
	
	/*Uart Streams*/
	InputStream in;
	OutputStream out;
	
	/*UART Commands*/
	byte[] leftBuff,rightBuff,entBuff,winBuff,upBuff,downBuff,messBuff;
	
	/*LED*/
	private DigitalOutput led_;
	private boolean ledStatus;
	
	/*BlueTooth*/
	static private int REQUEST_ENABLE_BT = 1;

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/*button_ = (ToggleButton) findViewById(R.id.button);*/
		
		/*Link Button variables to main.xml instances*/
		leftButton = (Button) findViewById(R.id.leftButton);
		rightButton = (Button) findViewById(R.id.rightButton);
		enterButton = (Button) findViewById(R.id.enterButton);
		winDown = (Button) findViewById(R.id.windowsButton);
		volUp = (Button) findViewById(R.id.volUp);
		volDown = (Button) findViewById(R.id.volDown);
		test = (EditText)findViewById(R.id.test);
		test.setText("Button not clicked");
		status = (TextView)findViewById(R.id.status);
		status.setText("IOIO Not Connected");
		
		/*Set UART parameters*/
		rxPin = 6;
		txPin = 7;
		baud = 9600;
		parity = Uart.Parity.NONE;
		stopBits = Uart.StopBits.ONE;
		
		/*Initialize Array Size*/
		leftBuff = new byte[1];
		rightBuff = new byte[1];
		entBuff = new byte[1];
		winBuff = new byte[1];
		upBuff = new byte[1];
		downBuff = new byte[1];
		messBuff = new byte[1];
		
		/*Set UART Commands*/
		leftBuff [0] = '0';
		rightBuff [0] = '1';
		entBuff [0] = '2';
		winBuff [0] = '3';
		upBuff [0] = '4';
		downBuff [0] = '5';
		messBuff [0] = 0;
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

	
	}
	
	public void leftButtonClick(View view) throws ConnectionLostException {    // Do something when the button is clicked    
		test.setText("Left Button Clicked");
		ledStatus = !ledStatus;
		/*write left button command to UART*/
		messBuff = leftBuff;
	}
	
	public void rightButtonClick(View view) throws ConnectionLostException {    // Do something when the button is clicked    
		test.setText("Right Button Clicked");
		ledStatus = !ledStatus;
		/*write right button command to UART*/
		messBuff = rightBuff;
	}
	
	public void enterButtonClick(View view) throws ConnectionLostException {    // Do something when the button is clicked    
		test.setText("Enter Button Clicked");
		ledStatus = !ledStatus;
		/*write enter button command to UART*/
		messBuff = entBuff;
	}
	
	public void winDownButtonClick(View view) throws ConnectionLostException {    // Do something when the button is clicked    
		test.setText("Window Button Clicked");
		ledStatus = !ledStatus;
		/*write window button command to UART*/
		messBuff = winBuff;
	}
	
	public void volUpButtonClick(View view) throws ConnectionLostException {    // Do something when the button is clicked    
		test.setText("Volume up Button Clicked");
		ledStatus = !ledStatus;
		/*write volume up button command to UART*/
		messBuff = upBuff;
	}
	
	public void volDownButtonClick(View view) throws ConnectionLostException {    // Do something when the button is clicked    
		test.setText("Volume Down Button Clicked");
		ledStatus = !ledStatus;
		/*write volume down button command to UART*/
		messBuff = downBuff;
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		/** The on-board LED. */
		//private DigitalOutput led_;
		

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {

			
			led_ = ioio_.openDigitalOutput(0, true);
			ledStatus = false;
			//status.setText("IOIO Connected");
			//test.setText("IOIO Connected");
			try
			{
				/* Attempt to open UART for talking to Arduino */
			Uart uart = ioio_.openUart(rxPin, txPin, baud, parity, stopBits);
			in = uart.getInputStream();
			out = uart.getOutputStream();
			}
			catch(ConnectionLostException e)
			{
			//	test.setText(e.getMessage()+Log.getStackTraceString(e));
				//status.setText("IOIO Not Connected");
				//test.setText("IOIO Not Connected");
			}
			
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		protected void loop() throws ConnectionLostException {
//			led_.write(true);
		    led_.write(ledStatus);
		    if(messBuff[0] != 0)
		    {
		    	try {
					out.write(messBuff);
					messBuff[0] = 0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			try {
				sleep(100);
			} catch (InterruptedException e) {
				status.setText("IOIO Not Connected");
				//test.setText("IOIO Not Connected");
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}

}