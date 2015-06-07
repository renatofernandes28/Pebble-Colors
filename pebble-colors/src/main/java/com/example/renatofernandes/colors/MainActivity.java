package com.example.renatofernandes.colors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.renatofernandes.colors.adapters.RGBColorAdapter;
import com.example.renatofernandes.colors.objects.RGBColor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


public class MainActivity extends Activity {

    private EditText _ipField;
    private Button _connectButton;
    private ListView _colorList;
    private TextView _currentColor;
    private String _ipAddress = "";
    private boolean _isConnected = false;

    private static final int SERVER_PORT = 1234;
    private static final int DEFAULT_COLOR_VALUE = 127;
    private static final int MAX_COLOR_VALUE = 256;

    private ArrayList<RGBColor> _rgbColorArray;
    private RGBColorAdapter _rgbAdapter;

    private Handler _uiHandler = new Handler();
    private Thread _clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Views and Class Variables
        _ipField = (EditText) findViewById(R.id.ip_field);
        _connectButton = (Button) findViewById(R.id.connect_button);
        _colorList = (ListView) findViewById(R.id.color_list);
        _currentColor = (TextView) findViewById(R.id.current_command);
        _connectButton.setOnClickListener(_onConnectListener);

        _rgbColorArray = new ArrayList<RGBColor>();
        _rgbAdapter = new RGBColorAdapter(this, R.id.color_list, _rgbColorArray);
        _colorList.setAdapter(_rgbAdapter);
        _colorList.setOnItemClickListener(_itemClick);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // Force Thread to stop and hence exit the while loop
            _clientThread.interrupt();
            _isConnected = false;
        } catch (Exception e) {
            Log.e("Client", "Error when interrupting Thread");
        }
    }

    private View.OnClickListener _onConnectListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!_isConnected) {
                _ipAddress = _ipField.getText().toString();
                if (!_ipAddress.equals("")) {

                    //Hide KeyBoard
                    InputMethodManager iMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    iMM.hideSoftInputFromWindow(_ipField.getWindowToken(), 0);

                    _clientThread = new Thread(_clientRunnable);
                    _clientThread.start();
                }
                else{
                    //If ipField is empty, advise user to enter an ip address
                    Toast.makeText(getApplicationContext(), R.string.error_blank_ip_address, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private AdapterView.OnItemClickListener _itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){

            //Toggle select/ deselect based on the current selection of the selected command
            if (_rgbAdapter.getItem(position).isSelected()) {
                _rgbAdapter.getItem(position).setSelected(false);
            }
            else {
                _rgbAdapter.getItem(position).setSelected(true);
            }

            //Update list and current color
            _rgbAdapter.notifyDataSetChanged();
            updateColorValues();
        }
    };

    private void showCommands(){
        //Show the list of commands and the current component, and remove the other views
        _connectButton.setVisibility(View.GONE);
        _ipField.setVisibility(View.GONE);

        _currentColor.setVisibility(View.VISIBLE);
        _colorList.setVisibility(View.VISIBLE);

        //Upon connect set to default color value
        _currentColor.setText(getCurrentText(DEFAULT_COLOR_VALUE, DEFAULT_COLOR_VALUE, DEFAULT_COLOR_VALUE));
    }


    //Method to get the correct string to update the current color
    private String getCurrentText(int red, int green, int blue){
        return getResources().getString(R.string.current_color_string, red, green, blue);
    }

    private Runnable _clientRunnable = new Runnable() {

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(_ipAddress);
                Socket socket = new Socket(serverAddr, SERVER_PORT);
                DataInputStream input;
                _isConnected = true;

                //Show listView since we are connected
                _uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                       showCommands();
                    }
                });

                //Run until we are not connected or we are interrupted
                while (_isConnected || !Thread.currentThread().isInterrupted()) {
                    try {
                        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                        RGBColor rgbColor = new RGBColor();

                        //If we read 0x02, this is an absolute command otherwise relative
                        rgbColor.setAbsolute(input.readByte() == 2);

                        //If absolute we read in the full bytes
                        if(rgbColor.isAbsolute()){
                            rgbColor.setRed(input.readUnsignedByte());
                            rgbColor.setGreen(input.readUnsignedByte());
                            rgbColor.setBlue(input.readUnsignedByte());
                        }
                        //Otherwise we read as an offset
                        else{
                            rgbColor.setRed(input.readByte() << 8 | input.readByte());
                            rgbColor.setGreen(input.readByte() << 8 | input.readByte());
                            rgbColor.setBlue(input.readByte() << 8 | input.readByte());
                        }

                        _uiHandler.post(new UIRunnable(rgbColor));

                    } catch (Exception e) {
                        Log.e("Client", "Error while fetching commands", e);
                    }
                }

                //We are done so close socket, and reset isConnected variable
                socket.close();
                _isConnected = false;

            } catch (Exception e) {
                Log.e("Client", "Error while connecting", e);
                _uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //If we reached here, we failed to connect to the ip address
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_fail_to_connect, _ipAddress), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    //Thread responsible for updating UI from the Client Runnable
    private class UIRunnable implements Runnable{
        private RGBColor _rgbColor;

        public UIRunnable(RGBColor color){
            _rgbColor = color;
        }

        @Override
        public void run() {
            if (_rgbColor == null){
                 Log.e("UI", "Null RGBColor");
                 return;
            }

            if(_rgbColor.isAbsolute()){
                //Deselect all previous commands
                for (RGBColor rgbColor : _rgbColorArray) {
                    rgbColor.setSelected(false);
                }
            }

            //Add new color to list, and update current color
            _rgbColor.setSelected(true);

            _rgbColorArray.add(0, _rgbColor);
            _rgbAdapter.notifyDataSetChanged();
            updateColorValues();
        }
    }

    private void updateColorValues(){
        int currRed = DEFAULT_COLOR_VALUE;
        int currGreen = DEFAULT_COLOR_VALUE;
        int currBlue = DEFAULT_COLOR_VALUE;

        //We want to go through the array in reverse order and start summing selected relative values
        //We reset values when we reach an absolute command
        for(int i = _rgbColorArray.size() - 1; i >= 0; i--){
            RGBColor rgbColor = _rgbColorArray.get(i);

            if(rgbColor.isSelected()){
                if(rgbColor.isAbsolute()){
                    currRed = rgbColor.getRed();
                    currGreen = rgbColor.getGreen();
                    currBlue = rgbColor.getBlue();
                }
                else{
                   //Add up relative commands, if we calculated a negative number we modularize
                   currRed = (currRed + rgbColor.getRed()) % MAX_COLOR_VALUE;
                   currRed = currRed < 0 ? currRed + MAX_COLOR_VALUE : currRed;

                   currGreen = (currGreen + rgbColor.getGreen()) % MAX_COLOR_VALUE;
                   currGreen = currGreen < 0 ? currGreen + MAX_COLOR_VALUE : currGreen;

                   currBlue = (currBlue + rgbColor.getBlue()) % MAX_COLOR_VALUE;
                   currBlue = currBlue < 0 ? currBlue + MAX_COLOR_VALUE : currBlue;
                }
            }

        }
        //Update the CurrentColor
        _currentColor.setText(getCurrentText(currRed, currGreen, currBlue));
    }

}
