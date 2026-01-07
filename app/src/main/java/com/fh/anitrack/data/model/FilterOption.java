package com.fh.anitrack.data.model;

/**
 * Data model representing a filter option in dropdowns.
 */
public class FilterOption {
    private String id;
    private String displayName;
    private String category;    // For tags: "Technical", "Theme", etc.
    private boolean isAdult;    // For tags
    private boolean isSelected;

    public FilterOption() {
    }

    public FilterOption(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.isSelected = false;
    }

    public FilterOption(String id, String displayName, String category, boolean isAdult) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.isAdult = isAdult;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAdult() {
        return isAdult;
    }

    public void setAdult(boolean adult) {
        isAdult = adult;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterOption that = (FilterOption) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
