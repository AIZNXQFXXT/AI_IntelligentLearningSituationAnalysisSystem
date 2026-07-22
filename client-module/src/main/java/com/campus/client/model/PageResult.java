package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PageResult<T> {
    @JsonProperty("records")
    private List<T> records;
    @JsonProperty("total")
    private int total;
    @JsonProperty("page")
    private int page;
    @JsonProperty("size")
    private int size;
    @JsonProperty("pages")
    private int pages;

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
}
