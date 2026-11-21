package com.collegecart.dto;

import com.collegecart.entity.StudentListing;
import java.util.List;
import org.springframework.data.domain.Page;

public class ListingBrowseResponse {

    private List<ListingResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public ListingBrowseResponse(Page<StudentListing> listingPage) {
        this.content = listingPage.getContent().stream()
                .map(ListingResponse::new)
                .toList();
        this.page = listingPage.getNumber();
        this.size = listingPage.getSize();
        this.totalElements = listingPage.getTotalElements();
        this.totalPages = listingPage.getTotalPages();
        this.first = listingPage.isFirst();
        this.last = listingPage.isLast();
    }

    public List<ListingResponse> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}
