package co.edu.udistrital.mdp.back.entities;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAID,
    CANCELLED,
    SHIPPED,
    DELIVERED;


// --- Regla: estados inmutables ---
public static boolean isImmutable(OrderStatus status) {
    return status == PAID
        || status == SHIPPED
        || status == DELIVERED
        || status == CANCELLED;
}

}