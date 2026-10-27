package com.guilu.enums;

import com.guilu.DynamicThreadPool.FLDynamicReject.DynamicRejectHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.RejectedExecutionHandler;

@Getter
@AllArgsConstructor
public enum RejectHandlerType {
    CALLERRUNS("CALLERRUNS"),
    DISCARDOLDEST("DISCARDOLDEST"),
    DISCARD("DISCARD"),
    ABORT("ABORT"),
    DYNAMIC("DYNAMIC");

    private final String rejectType;

    /**
     * Resolves a reject policy by enum name or configured value, case-insensitively.
     */
    public static RejectHandlerType from(String rejectType) {
        if (rejectType == null || rejectType.isBlank()) {
            return null;
        }
        String normalized = rejectType.trim();
        for (RejectHandlerType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)
                    || type.rejectType.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Every configured policy is wrapped by a single counting handler so the
     * reporter can expose the cumulative rejection count.
     */
    public static RejectedExecutionHandler of(String rejectType) {
        RejectHandlerType type = from(rejectType);
        return type == null ? null : new DynamicRejectHandler(type);
    }
}
