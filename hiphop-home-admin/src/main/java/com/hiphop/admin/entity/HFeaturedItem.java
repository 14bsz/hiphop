package com.hiphop.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

@TableName("h_featured_items")
public class HFeaturedItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("position")
    private String position; // center, left_top, left_bottom
    @TableField("title")
    private String title;
    @TableField("img_url")
    private String imgUrl;
    @TableField("link_url")
    private String linkUrl;
    @TableField("tag")
    private String tag; // NEWS, FEATURES, BEEF, MUSIC等（用于热门趋势）
    @TableField("views")
    private String views; // 浏览量显示，如: 2.2K, 61.4K（用于热门趋势）
    @TableField("status")
    private Integer status; // 1-启用, 0-禁用
    @TableField("sort_no")
    private Integer sortNo;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    public String getViews() { return views; }
    public void setViews(String views) { this.views = views; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}

