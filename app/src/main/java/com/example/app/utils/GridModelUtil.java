package com.example.app.utils;

import com.example.app.bean.GridModel;

import java.util.ArrayList;
import java.util.List;

public class GridModelUtil {

    public static List<GridModel> getData(Object[][] array) {
        List<GridModel> list = new ArrayList<>();
        for (Object[] objects : array) {
            GridModel gridModel = new GridModel((String) objects[0], (Class<?>) objects[1]);
            if (objects.length > 2) {
                gridModel.setMdName((String) objects[2]);
            }
            if (objects.length > 3) {
                gridModel.setCode((int) objects[3]);
            }
            list.add(gridModel);
        }
        return list;
    }

    public static List<GridModel> getData(String[] array) {
        List<GridModel> list = new ArrayList<>();
        for (String str : array) {
            GridModel gridModel = new GridModel(str, null);
            list.add(gridModel);
        }
        return list;
    }
}
