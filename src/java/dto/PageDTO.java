package dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageDTO<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final int totalItems;

    public PageDTO(List<T> items, int page, int pageSize, int totalItems) {
        this.items = items == null ? Collections.emptyList() : items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public static int pageCount(int totalItems, int pageSize) {
        return Math.max(1, (totalItems + pageSize - 1) / pageSize);
    }

    public static int clampPage(int page, int totalItems, int pageSize) {
        return Math.min(Math.max(1, page), pageCount(totalItems, pageSize));
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return pageCount(totalItems, pageSize);
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public int getFrom() {
        return totalItems == 0 ? 0 : (page - 1) * pageSize + 1;
    }

    public int getTo() {
        return Math.min(page * pageSize, totalItems);
    }

    public List<Integer> getPageNumbers() {
        int total = getTotalPages();
        List<Integer> nums = new ArrayList<>();
        int last = 0;
        for (int p = 1; p <= total; p++) {
            boolean show = p <= 2 || p > total - 2 || Math.abs(p - page) <= 1;
            if (!show) {
                continue;
            }
            if (last != 0 && p - last > 1) {
                nums.add(0);
            }
            nums.add(p);
            last = p;
        }
        return nums;
    }
}
