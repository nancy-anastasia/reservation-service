CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    reserved_by VARCHAR(150) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_reservations_resource
        FOREIGN KEY (resource_id)
        REFERENCES resources(id),

    CONSTRAINT chk_reservation_time
        CHECK (ends_at > starts_at)
);

CREATE INDEX idx_reservations_resource_time
    ON reservations(resource_id, starts_at, ends_at);