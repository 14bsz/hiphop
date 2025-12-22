package com.hiphop.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 高分音乐榜表实体
 */
@TableName("h_top_rated_items")
public class HTopRatedItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 歌曲标题 */
    @TableField("title")
    private String title;

    /** 艺人 / 歌手 */
    @TableField("artist")
    private String artist;

    /** 封面图片 URL */
    @TableField("cover")
    private String cover;

    /** 原歌曲链接（外链） */
    @TableField("link_url")
    private String linkUrl;

    /** 类型：1-单曲，2-专辑 */
    @TableField("type")
    private Integer type;

    /** 评分（0-5） */
    @TableField("rating")
    private Double rating;

    /** 收藏数（来自网易云） */
    @TableField("favorite_count")
    private Long favoriteCount;

    /** 排序号 */
    @TableField("sort_no")
    private Integer sortNo;

    /** 状态：1-启用，0-禁用 */
    @TableField("status")
    private Integer status;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}




