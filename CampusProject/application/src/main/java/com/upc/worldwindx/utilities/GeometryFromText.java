package com.upc.worldwindx.utilities;

/**
 * Created by Lenovo on 2018/4/6.
 */

public class GeometryFromText {

    public static Double[] pointZFromText(String pointZText){
         //pointText example  : POINT Z(x y z)
        Double [] point = new Double[3];
        point[0] = Double.parseDouble(pointZText.split(" ")[1].split("\\(")[1]);
        point[1] = Double.parseDouble(pointZText.split(" ")[2]);
        point[2] = Double.parseDouble(pointZText.split(" ")[3].split("\\)")[0]);
        return point;
    }
    public static Double[] pointFromText(String pointText){
        //pointText example : POINT(x y z)
        Double [] point = new Double[2];
        point[0] = Double.parseDouble(pointText.split(" ")[0].split("\\(")[1]);
        point[1] = Double.parseDouble(pointText.split(" ")[1].split("\\)")[0]);
        return point;
    }
}
