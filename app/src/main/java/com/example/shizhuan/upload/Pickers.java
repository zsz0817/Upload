package com.example.shizhuan.upload;

import java.io.Serializable;

/**
 *
 * @author zengtao 2015年5月20日下午7:18:14
 *
 */
public class Pickers implements Serializable {

    private static final long serialVersionUID = 1L;

    private String showConetnt;
    private int showId;

    public String getShowConetnt() {
        return showConetnt;
    }

    public int getShowId() {
        return showId;
    }

    public Pickers(String showConetnt, int showId) {
        super();
        this.showConetnt = showConetnt;
        this.showId = showId;
    }

    public Pickers() {
        super();
    }

}
