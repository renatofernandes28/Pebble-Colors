package com.example.renatofernandes.colors.objects;

/**
 * Created by renatofernandes on 15-06-06.
 */
public class RGBColor {

    //Variables to store each component
    private int _redValue;
    private int _greenValue;
    private int _blueValue;

    //Variables to check if command is absolute/relative/selected/not-selected
    private boolean _absolute;
    private boolean _selected;


    //Getters and setters for the above variables
    public void setRed(int value){
        _redValue = value;
    }

    public int getRed(){
        return _redValue;
    }

    public void setGreen(int value){
        _greenValue = value;
    }

    public int getGreen(){
        return _greenValue;
    }

    public void setBlue(int value){
        _blueValue = value;
    }

    public int getBlue(){
        return _blueValue;
    }

    public void setAbsolute(boolean isAbsolute){
        _absolute = isAbsolute;
    }

    public boolean isAbsolute(){
        return _absolute;
    }

    public void setSelected(boolean isSelected){
        _selected = isSelected;
    }

    public boolean isSelected(){
        return _selected;
    }
}
