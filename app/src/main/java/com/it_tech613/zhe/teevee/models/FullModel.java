package com.it_tech613.zhe.teevee.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class FullModel implements Serializable {
    private String category,category_name;
    private List<ChannelModel> channels;

    public FullModel(String category, List<ChannelModel> channels, String category_name) {
        this.category = category;
        this.channels = channels;
        this.category_name = category_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<ChannelModel> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelModel> channels) {
        this.channels = channels;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }
}
