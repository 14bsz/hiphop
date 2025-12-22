package com.hiphop.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 每日推荐表实体
 */
@TableName("h_daily_recommend")
public class HDailyRecommend implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 歌曲名称 */
    @TableField("title")
    private String title;

    /** 歌手名称 */
    @TableField("artist")
    private String artist;

    /** 封面图片 URL */
    @TableField("cover")
    private String cover;

    /** 歌曲链接 */
    @TableField("link_url")
    private String linkUrl;

    /** 标签（如：热度爆表） */
    @TableField("badge")
    private String badge;

    /** 推荐日期 */
    @TableField("recommend_date")
    private Date recommendDate;

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

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Date getRecommendDate() {
        return recommendDate;
    }

    public void setRecommendDate(Date recommendDate) {
        this.recommendDate = recommendDate;
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
