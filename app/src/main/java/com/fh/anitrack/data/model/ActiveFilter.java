package com.fh.anitrack.data.model;

/**
 * Represents an active filter applied by the user.
 */
public class ActiveFilter {
    private String filterId;
    private String filterType;
    private String filterTypeDisplay;
    private String filterValue;
    private String filterValueDisplay;

    public ActiveFilter(String filterId, String filterType, String filterTypeDisplay,
                        String filterValue, String filterValueDisplay) {
        this.filterId = filterId;
        this.filterType = filterType;
        this.filterTypeDisplay = filterTypeDisplay;
        this.filterValue = filterValue;
        this.filterValueDisplay = filterValueDisplay;
    }

    public String getFilterId() {
        return filterId;
    }

    public String getFilterType() {
        return filterType;
    }

    public String getFilterTypeDisplay() {
        return filterTypeDisplay;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public String getFilterValueDisplay() {
        return filterValueDisplay;
    }

    public String getDisplayText() {
        return filterTypeDisplay + ": " + filterValueDisplay;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActiveFilter that = (ActiveFilter) obj;
        return filterId.equals(that.filterId);
    }

    @Override
    public int hashCode() {
        return filterId.hashCode();
    }
}
