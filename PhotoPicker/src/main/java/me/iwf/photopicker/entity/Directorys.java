package me.iwf.photopicker.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by LockyLuo on 2018/8/12.
 */

public class Directorys implements Serializable{

    private List<String> paths;

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public String toString() {
        return "Directorys{" +
                "paths=" + paths +
                '}';
    }
}
