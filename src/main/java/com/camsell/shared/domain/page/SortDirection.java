package com.camsell.shared.domain.page;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection fromOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return "asc".equalsIgnoreCase(raw) ? ASC : DESC;
    }
}
