package com.reqres.tests.utils;

import org.apache.poi.ss.usermodel.Cell;

public class Utils {

    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return cell.getStringCellValue();
    }

    public static int getCellValueAsInt(Cell cell) {
        if (cell == null) {
            return 0;
        }
        return (int) cell.getNumericCellValue();
    }
}
